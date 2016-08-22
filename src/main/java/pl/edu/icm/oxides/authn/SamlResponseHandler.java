package pl.edu.icm.oxides.authn;

import eu.unicore.samly2.SAMLBindings;
import eu.unicore.samly2.exceptions.SAMLValidationException;
import eu.unicore.samly2.trust.SamlTrustChecker;
import eu.unicore.samly2.trust.TruststoreBasedSamlTrustChecker;
import eu.unicore.samly2.validators.AssertionValidator;
import eu.unicore.samly2.validators.ReplayAttackChecker;
import eu.unicore.samly2.validators.SSOAuthnResponseValidator;
import eu.unicore.security.etd.TrustDelegation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.edu.icm.oxides.config.GridConfig;
import pl.edu.icm.oxides.user.OxidesPortalGridSession;
import pl.edu.icm.unicore.spring.security.GridIdentityProvider;
import xmlbeans.org.oasis.saml2.assertion.AssertionDocument;
import xmlbeans.org.oasis.saml2.assertion.AuthnStatementType;
import xmlbeans.org.oasis.saml2.protocol.ResponseDocument;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static eu.unicore.samly2.trust.DsigSamlTrustCheckerBase.CheckingMode.REQUIRE_SIGNED_RESPONSE_OR_ASSERTION;

@Component
class SamlResponseHandler {
    private final GridConfig gridConfig;
    private final GridIdentityProvider idProvider;

    @Autowired
    SamlResponseHandler(GridConfig gridConfig, GridIdentityProvider idProvider) {
        this.gridConfig = gridConfig;
        this.idProvider = idProvider;
    }

    String processAuthenticationResponse(HttpServletRequest request, OxidesPortalGridSession oxidesPortalGridSession) {
        String samlResponse = request.getParameter("SAMLResponse");
        String returnUrl = "/";
        try {
            ResponseDocument responseDocument = Utils.decodeMessage(samlResponse, log);
            final SSOAuthnResponseValidator validator =
                    validateSamlResponse(responseDocument, oxidesPortalGridSession.getUuid());

            final String sessionIndex = extractSessionIndex(validator);
            log.trace(String.format("Authority session index: %s", sessionIndex));
            oxidesPortalGridSession.setSessionIndex(sessionIndex);

            log.debug("Response document: " + responseDocument.xmlText());
            EtdAssertionsWrapper etdAssertionsWrapper = new EtdAssertionsWrapper(responseDocument);
            if (oxidesPortalGridSession != null) {
                processAuthenticationResponseData(oxidesPortalGridSession, etdAssertionsWrapper);
                returnUrl = oxidesPortalGridSession.getReturnUrl();
            }
            return String.format("redirect:%s", returnUrl);
        } catch (Exception e) {
            String message = "Could not parse SAML authentication response!";
            log.error(message, e);
            throw new UnprocessableResponseException(message, e);
        }
    }

    private void processAuthenticationResponseData(OxidesPortalGridSession oxidesPortalGridSession,
                                                   EtdAssertionsWrapper etdAssertionsWrapper) {
        List<TrustDelegation> trustDelegationList = etdAssertionsWrapper
                .getEtdAssertions()
                .stream()
                .map(this::toTrustDelegation)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (trustDelegationList.isEmpty()) {
            throw new RuntimeException("Missing trust delegation data!");
        }

        oxidesPortalGridSession.setTrustDelegations(trustDelegationList);
        etdAssertionsWrapper.getAttributeData().getAttributes().forEach(
                (attributeKey, attributeValues) -> {
                    attributeValues
                            .forEach(value -> oxidesPortalGridSession.storeAttribute(attributeKey, value));
                }
        );
    }

    private SSOAuthnResponseValidator validateSamlResponse(ResponseDocument response, String requestId)
            throws URISyntaxException, SAMLValidationException {
        SamlTrustChecker trustChecker = new TruststoreBasedSamlTrustChecker(
                idProvider.getIdpValidator(),
                REQUIRE_SIGNED_RESPONSE_OR_ASSERTION
        );
        SSOAuthnResponseValidator validator = new SSOAuthnResponseValidator(
                idProvider.getGridCredential().getSubjectName(),
                new URI(gridConfig.getTargetUrl()).toASCIIString(),
                requestId,
                AssertionValidator.DEFAULT_VALIDITY_GRACE_PERIOD,
                trustChecker,
                new ReplayAttackChecker(),
                SAMLBindings.HTTP_POST
        );
        validator.validate(response);
        return validator;
    }

    private String extractSessionIndex(SSOAuthnResponseValidator validator) {
        AuthnStatementType[] statements = validator
                .getAuthNAssertions()
                .get(0)
                .getAssertion()
                .getAuthnStatementArray();
        return statements.length > 0 ? statements[0].getSessionIndex() : "";
    }

    private TrustDelegation toTrustDelegation(AssertionDocument assertionDocument) {
        try {
            return new TrustDelegation(assertionDocument);
        } catch (Exception e) {
            log.warn("Problem with generating ETD", e);
            return null;
        }
    }

    private Log log = LogFactory.getLog(SamlResponseHandler.class);
}

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
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.impl.util.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.edu.icm.oxides.config.GridConfig;
import pl.edu.icm.oxides.config.GridIdentityProvider;
import pl.edu.icm.oxides.user.AuthenticationSession;
import xmlbeans.org.oasis.saml2.assertion.AssertionDocument;
import xmlbeans.org.oasis.saml2.protocol.ResponseDocument;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
class SamlResponseHandler {
    private final GridConfig gridConfig;
    private final GridIdentityProvider idProvider;

    @Autowired
    SamlResponseHandler(GridConfig gridConfig, GridIdentityProvider idProvider) {
        this.gridConfig = gridConfig;
        this.idProvider = idProvider;
    }

    String processAuthenticationResponse(HttpServletRequest request, AuthenticationSession authenticationSession) {
        String samlResponse = request.getParameter("SAMLResponse");
        String returnUrl = "/";
        try {
            ResponseDocument responseDocument = decodeResponse(samlResponse);
            validateSamlResponse(responseDocument, authenticationSession.getUuid());

            EtdAssertionsWrapper etdAssertionsWrapper = new EtdAssertionsWrapper(responseDocument);
            if (authenticationSession != null) {
                processAuthenticationResponseData(authenticationSession, etdAssertionsWrapper);
                returnUrl = authenticationSession.getReturnUrl();
            }
            return String.format("redirect:%s", returnUrl);
        } catch (Exception e) {
            String message = "Could not parse SAML authentication response!";
            log.error(message, e);
            throw new UnprocessableResponseException(message, e);
        }
    }

    private void processAuthenticationResponseData(AuthenticationSession authenticationSession,
                                                   EtdAssertionsWrapper etdAssertionsWrapper) {
        authenticationSession.setTrustDelegations(
                etdAssertionsWrapper.getEtdAssertions().stream()
                        .map(this::toTrustDelegation)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList())
        );
        etdAssertionsWrapper.getAttributeData().getAttributes().forEach(
                (attributeKey, attributeValues) -> {
                    attributeValues
                            .forEach(value -> authenticationSession.storeAttribute(attributeKey, value));
                }
        );
    }

    private ResponseDocument decodeResponse(String response) throws SAMLValidationException {
        byte[] decoded = Base64.decode(response.getBytes());
        if (decoded == null) {
            throw new SAMLValidationException("The SAML response is not properly Base64 encoded");
        }
        String responseString = new String(decoded, StandardCharsets.UTF_8);
        log.trace(responseString);
        try {
            return ResponseDocument.Factory.parse(responseString);
        } catch (XmlException e) {
            throw new SAMLValidationException(e.getMessage());
        }
    }

    private SSOAuthnResponseValidator validateSamlResponse(ResponseDocument response, String requestId)
            throws URISyntaxException, SAMLValidationException {
        SamlTrustChecker trustChecker = new TruststoreBasedSamlTrustChecker(
                idProvider.getIdpValidator(),
                false
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

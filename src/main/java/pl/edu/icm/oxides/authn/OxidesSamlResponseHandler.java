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
import pl.edu.icm.oxides.config.GridIdProvider;
import xmlbeans.org.oasis.saml2.assertion.AssertionDocument;
import xmlbeans.org.oasis.saml2.protocol.ResponseDocument;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Component
public class OxidesSamlResponseHandler {
    private final GridConfig gridConfig;
    private final GridIdProvider idProvider;

    @Autowired
    public OxidesSamlResponseHandler(GridConfig gridConfig, GridIdProvider idProvider) {
        this.gridConfig = gridConfig;
        this.idProvider = idProvider;
    }

    public void processAuthenticationResponse(HttpServletRequest request, HttpServletResponse response, OxidesAuthenticationSession authenticationSession) {
        String samlResponse = request.getParameter("SAMLResponse");
        HttpSession session = request.getSession();

        String returnUrl = "/error";
        try {
            ResponseDocument responseDocument = decodeResponse(samlResponse);
            validateSamlResponse(responseDocument, authenticationSession.getUuid());

            SamlResponseWrapper samlResponseWrapper = new SamlResponseWrapper(responseDocument);

            if (authenticationSession != null) {
                authenticationSession.setTrustDelegations(
                        samlResponseWrapper.getEtdAssertions().stream()
                                .map(assertionDocument -> toTrustDelegation(assertionDocument))
                                .filter(trustDelegation -> trustDelegation != null)
                                .collect(Collectors.toList())
                );
                returnUrl = authenticationSession.getReturnUrl();
            }

            response.sendRedirect(returnUrl);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private ResponseDocument decodeResponse(String response) throws SAMLValidationException {
        byte[] decoded = Base64.decode(response.getBytes());
        if (decoded == null)
            throw new SAMLValidationException("The SAML response is not properly Base64 encoded");
        String respString = new String(decoded, StandardCharsets.UTF_8);
        try {
            return ResponseDocument.Factory.parse(respString);
        } catch (XmlException e) {
            throw new SAMLValidationException(e.getMessage());
        }
    }

    private SSOAuthnResponseValidator validateSamlResponse(ResponseDocument response, String requestId) throws URISyntaxException, SAMLValidationException {
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

    private Log log = LogFactory.getLog(OxidesSamlResponseHandler.class);
}

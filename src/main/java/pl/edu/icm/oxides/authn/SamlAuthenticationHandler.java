package pl.edu.icm.oxides.authn;

import eu.unicore.samly2.exceptions.SAMLValidationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.edu.icm.oxides.user.AuthenticationSession;
import xmlbeans.org.oasis.saml2.protocol.LogoutResponseDocument;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
public class SamlAuthenticationHandler {
    private final SamlRequestHandler samlRequestHandler;
    private final SamlResponseHandler samlResponseHandler;

    @Autowired
    public SamlAuthenticationHandler(SamlRequestHandler samlRequestHandler,
                                     SamlResponseHandler samlResponseHandler) {
        this.samlRequestHandler = samlRequestHandler;
        this.samlResponseHandler = samlResponseHandler;
    }

    public void performAuthenticationRequest(HttpServletResponse response,
                                             AuthenticationSession authenticationSession) {
        final String authenticationRequestId = authenticationSession.getUuid();
        samlRequestHandler.performAuthenticationRequest(response, authenticationRequestId);
    }

    public String processAuthenticationResponse(HttpServletRequest request,
                                                AuthenticationSession authenticationSession) {
        return samlResponseHandler.processAuthenticationResponse(request, authenticationSession);
    }

    public String processSingleLogoutResponse(HttpServletRequest request) {
        try {
            final String samlResponse = request.getParameter("SAMLResponse");
            final LogoutResponseDocument messageXml = Utils.decodeMessage(samlResponse, log);
            log.warn("SAML RESPONSE: " + messageXml.xmlText());
        } catch (SAMLValidationException e) {
            log.error("BLAD", e);
        }
        return "redirect:/";
    }

    private Log log = LogFactory.getLog(SamlAuthenticationHandler.class);
}

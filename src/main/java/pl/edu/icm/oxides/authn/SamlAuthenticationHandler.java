package pl.edu.icm.oxides.authn;

import eu.unicore.samly2.exceptions.SAMLValidationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.edu.icm.oxides.user.OxidesPortalGridSession;
import xmlbeans.org.oasis.saml2.protocol.LogoutResponseDocument;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static pl.edu.icm.oxides.authn.Utils.decodeMessage;

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
                                             OxidesPortalGridSession oxidesPortalGridSession) {
        final String authenticationRequestId = oxidesPortalGridSession.getUuid();
        samlRequestHandler.performAuthenticationRequest(response, authenticationRequestId);
    }

    public String processAuthenticationResponse(HttpServletRequest request,
                                                OxidesPortalGridSession oxidesPortalGridSession) {
        return samlResponseHandler.processAuthenticationResponse(request, oxidesPortalGridSession);
    }

    public String processSingleLogoutResponse(HttpServletRequest request) {
        try {
            final String samlResponse = request.getParameter("SAMLResponse");
            final LogoutResponseDocument messageXml = decodeMessage(samlResponse, log);
            log.info("Single logout response message: " + messageXml.xmlText());
        } catch (SAMLValidationException e) {
            log.error("Could not read single logout response message!", e);
        }
        return "redirect:/";
    }

    private Log log = LogFactory.getLog(SamlAuthenticationHandler.class);
}

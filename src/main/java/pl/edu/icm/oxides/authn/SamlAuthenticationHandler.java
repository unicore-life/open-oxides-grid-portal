package pl.edu.icm.oxides.authn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.edu.icm.oxides.user.AuthenticationSession;

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
        samlRequestHandler.performAuthenticationRequest(response, authenticationSession);
    }

    public String processAuthenticationResponse(HttpServletRequest request,
                                                AuthenticationSession authenticationSession) {
        return samlResponseHandler.processAuthenticationResponse(request, authenticationSession);
    }
}

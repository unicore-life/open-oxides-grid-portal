package pl.edu.icm.oxides.authn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.edu.icm.oxides.unicore.CachingResourcesManager;
import pl.edu.icm.oxides.unicore.central.factory.UnicoreFactoryStorage;
import pl.edu.icm.oxides.user.AuthenticationSession;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
public class SamlAuthenticationHandler {
    private final SamlRequestHandler samlRequestHandler;
    private final SamlResponseHandler samlResponseHandler;
    private final CachingResourcesManager cachingResourcesManager;
    private final UnicoreFactoryStorage unicoreFactoryStorage;

    @Autowired
    public SamlAuthenticationHandler(SamlRequestHandler samlRequestHandler,
                                     SamlResponseHandler samlResponseHandler,
                                     CachingResourcesManager cachingResourcesManager,
                                     UnicoreFactoryStorage unicoreFactoryStorage) {
        this.samlRequestHandler = samlRequestHandler;
        this.samlResponseHandler = samlResponseHandler;
        this.cachingResourcesManager = cachingResourcesManager;
        this.unicoreFactoryStorage = unicoreFactoryStorage;
    }

    public void performAuthenticationRequest(HttpServletResponse response,
                                             AuthenticationSession authenticationSession) {
        samlRequestHandler.performAuthenticationRequest(response, authenticationSession);
    }

    public String processAuthenticationResponse(HttpServletRequest request,
                                                AuthenticationSession authenticationSession) {
        String returnUrl = samlResponseHandler.processAuthenticationResponse(request, authenticationSession);
        cachingResourcesManager.initializeSignedInUserResources(authenticationSession);
        prepareStorageClient(authenticationSession);
        return returnUrl;
    }

    private void prepareStorageClient(AuthenticationSession authenticationSession) {
        authenticationSession.setStorageClient(
                unicoreFactoryStorage.getStorageClient(authenticationSession)
        );
    }
}

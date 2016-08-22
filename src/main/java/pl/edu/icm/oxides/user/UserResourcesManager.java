package pl.edu.icm.oxides.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.edu.icm.oxides.unicore.CachingResourcesManager;
import pl.edu.icm.oxides.unicore.SessionResourcesManager;

@Service
public class UserResourcesManager {
    private final CachingResourcesManager cachingResourcesManager;
    private final SessionResourcesManager sessionResourcesManager;

    @Autowired
    public UserResourcesManager(CachingResourcesManager cachingResourcesManager,
                                SessionResourcesManager sessionResourcesManager) {
        this.cachingResourcesManager = cachingResourcesManager;
        this.sessionResourcesManager = sessionResourcesManager;
    }

    public void initializeAfterSuccessfulSignIn(OxidesPortalGridSession oxidesPortalGridSession) {
        sessionResourcesManager.prepareStorageClient(oxidesPortalGridSession);
        cachingResourcesManager.initializeSignedInUserResources(
                oxidesPortalGridSession.getSelectedTrustDelegation()
        );
    }
}

package pl.edu.icm.oxides.unicore;

import eu.unicore.security.etd.TrustDelegation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import pl.edu.icm.oxides.unicore.central.EndpointReferenceTypeCache;
import pl.edu.icm.oxides.unicore.site.job.UnicoreJob;
import pl.edu.icm.oxides.user.AuthenticationSession;

@Service
public class CachingResourcesManager {
    private final UnicoreJob jobHandler;
    private final EndpointReferenceTypeCache eprCache;
    private final ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    public CachingResourcesManager(UnicoreJob jobHandler,
                                   EndpointReferenceTypeCache eprCache,
                                   ThreadPoolTaskExecutor taskExecutor) {
        this.jobHandler = jobHandler;
        this.eprCache = eprCache;
        this.taskExecutor = taskExecutor;
    }

    public void initializeSignedInUserResources(AuthenticationSession authenticationSession) {
        TrustDelegation trustDelegation = authenticationSession.getSelectedTrustDelegation();
        taskExecutor.execute(() -> {
            String custodianDN = trustDelegation.getCustodianDN();
            log.info("Staring caching resources calls for user <" + custodianDN + ">");

            jobHandler.retrieveSiteResourceList(trustDelegation)
                    .stream()
                    .forEach(jobEntity -> {
                        eprCache.update(jobEntity.getUuid(), jobEntity.getEpr());
                    });

            log.info("Finished caching resources calls for user <" + custodianDN + ">");
        });
    }

    private Log log = LogFactory.getLog(CachingResourcesManager.class);
}

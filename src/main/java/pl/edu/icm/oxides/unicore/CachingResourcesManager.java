package pl.edu.icm.oxides.unicore;

import eu.unicore.security.etd.TrustDelegation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import pl.edu.icm.oxides.unicore.site.job.UnicoreJob;

@Service
public class CachingResourcesManager {
    private final UnicoreJob jobHandler;
    private final CacheManager cacheManager;
    private final ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    public CachingResourcesManager(UnicoreJob jobHandler,
                                   CacheManager cacheManager,
                                   ThreadPoolTaskExecutor taskExecutor) {
        this.jobHandler = jobHandler;
        this.cacheManager = cacheManager;
        this.taskExecutor = taskExecutor;
    }

    public void initializeSignedInUserResources(TrustDelegation trustDelegation) {
        executeUserResourcesCacheInitialization(trustDelegation);
    }

    public void reinitializeAfterSubmission(TrustDelegation trustDelegation) {
        log.info("Cleaning cache with jobs list for user <" + trustDelegation.getCustodianDN() + ">");
        cacheManager
                .getCache("unicoreSessionJobList")
                .evict(trustDelegation.getCustodianDN());
        executeUserResourcesCacheInitialization(trustDelegation);
    }

    private void executeUserResourcesCacheInitialization(TrustDelegation trustDelegation) {
        taskExecutor.execute(() -> {
            String custodianDN = trustDelegation.getCustodianDN();
            log.info("Starting caching resources calls for user <" + custodianDN + ">");

            jobHandler.retrieveSiteResourceList(trustDelegation);

            log.info("Finished caching resources calls for user <" + custodianDN + ">");
        });
    }

    private Log log = LogFactory.getLog(CachingResourcesManager.class);
}

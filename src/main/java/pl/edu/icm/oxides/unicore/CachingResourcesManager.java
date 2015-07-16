package pl.edu.icm.oxides.unicore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import pl.edu.icm.oxides.unicore.site.job.UnicoreJob;
import pl.edu.icm.oxides.user.AuthenticationSession;

@Service
public class CachingResourcesManager {
    private final UnicoreJob jobHandler;
    private final ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    public CachingResourcesManager(UnicoreJob jobHandler, ThreadPoolTaskExecutor taskExecutor) {
        this.jobHandler = jobHandler;
        this.taskExecutor = taskExecutor;
    }

    public void initializeSignedInUserResources(AuthenticationSession authenticationSession) {
        log.info("Staring caching resources calls for user " + authenticationSession.getCommonName());
        taskExecutor.execute(() -> {
            // Of course can not use session scoped, so should remodelled caching:
            //jobHandler.retrieveSiteResourceList(authenticationSession);
            log.info("Finished caching resources calls for user");
        });
    }

    private Log log = LogFactory.getLog(CachingResourcesManager.class);
}

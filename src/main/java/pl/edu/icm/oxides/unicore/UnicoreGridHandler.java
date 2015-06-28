package pl.edu.icm.oxides.unicore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.edu.icm.oxides.unicore.central.tss.UnicoreSite;
import pl.edu.icm.oxides.unicore.central.tss.UnicoreSiteEntity;
import pl.edu.icm.oxides.unicore.site.job.UnicoreJob;
import pl.edu.icm.oxides.unicore.site.job.UnicoreJobEntity;
import pl.edu.icm.oxides.unicore.site.resource.UnicoreResource;
import pl.edu.icm.oxides.unicore.site.resource.UnicoreResourceEntity;
import pl.edu.icm.oxides.unicore.site.storage.UnicoreSiteStorage;
import pl.edu.icm.oxides.unicore.site.storage.UnicoreSiteStorageEntity;
import pl.edu.icm.oxides.user.AuthenticationSession;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Component
public class UnicoreGridHandler {
    private final UnicoreSite siteHandler;
    private final UnicoreSiteStorage storageHandler;
    private final UnicoreJob jobHandler;
    private final UnicoreResource resourceHandler;

    @Autowired
    public UnicoreGridHandler(UnicoreSite siteHandler,
                              UnicoreSiteStorage storageHandler,
                              UnicoreJob jobHandler,
                              UnicoreResource resourceHandler) {
        this.siteHandler = siteHandler;
        this.storageHandler = storageHandler;
        this.jobHandler = jobHandler;
        this.resourceHandler = resourceHandler;
    }

    public List<UnicoreSiteEntity> listUserSites(AuthenticationSession authenticationSession,
                                                 HttpServletResponse response) {
        if (isValidAuthenticationSession(authenticationSession)) {
            return siteHandler.retrieveServiceList(authenticationSession);
        }
        authenticationSession.setReturnUrl("/oxides/unicore-sites");
        return redirectToAuthentication(response);
    }

    public List<UnicoreSiteStorageEntity> listUserStorages(AuthenticationSession authenticationSession,
                                                           HttpServletResponse response) {
        if (isValidAuthenticationSession(authenticationSession)) {
            return storageHandler.retrieveSiteResourceList(authenticationSession);
        }
        authenticationSession.setReturnUrl("/oxides/unicore-storages");
        return redirectToAuthentication(response);
    }

    public List<UnicoreJobEntity> listUserJobs(AuthenticationSession authenticationSession,
                                               HttpServletResponse response) {
        if (isValidAuthenticationSession(authenticationSession)) {
            return jobHandler.retrieveSiteResourceList(authenticationSession);
        }
        authenticationSession.setReturnUrl("/oxides/unicore-jobs");
        return redirectToAuthentication(response);
    }

    public List<UnicoreResourceEntity> listUserResources(AuthenticationSession authenticationSession,
                                                         HttpServletResponse response) {
        if (isValidAuthenticationSession(authenticationSession)) {
            return resourceHandler.retrieveSiteResourceList(authenticationSession);
        }
        authenticationSession.setReturnUrl("/oxides/unicore-resources");
        return redirectToAuthentication(response);
    }

    private boolean isValidAuthenticationSession(AuthenticationSession authenticationSession) {
        return authenticationSession != null
                && authenticationSession.getTrustDelegations() != null
                && authenticationSession.getTrustDelegations().size() > 0;
    }

    private List redirectToAuthentication(HttpServletResponse response) {
        try {
            response.sendRedirect("/oxides/authn");
        } catch (IOException e) {
            log.error("Problem with redirection!", e);
        }
        return null;
    }

    private Log log = LogFactory.getLog(UnicoreGridHandler.class);
}

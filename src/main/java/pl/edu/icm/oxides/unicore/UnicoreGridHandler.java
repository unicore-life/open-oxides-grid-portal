package pl.edu.icm.oxides.unicore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.edu.icm.oxides.authn.OxidesAuthenticationSession;
import pl.edu.icm.oxides.unicore.job.UnicoreJobEntity;
import pl.edu.icm.oxides.unicore.job.UnicoreJobHandler;
import pl.edu.icm.oxides.unicore.site.UnicoreSiteEntity;
import pl.edu.icm.oxides.unicore.site.UnicoreSiteHandler;
import pl.edu.icm.oxides.unicore.storage.UnicoreStorageEntity;
import pl.edu.icm.oxides.unicore.storage.UnicoreStorageHandler;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Component
public class UnicoreGridHandler {
    private final UnicoreSiteHandler siteHandler;
    private final UnicoreStorageHandler storageHandler;
    private final UnicoreJobHandler jobHandler;

    @Autowired
    public UnicoreGridHandler(UnicoreSiteHandler siteHandler,
                              UnicoreStorageHandler storageHandler,
                              UnicoreJobHandler jobHandler) {
        this.siteHandler = siteHandler;
        this.storageHandler = storageHandler;
        this.jobHandler = jobHandler;
    }

    public List<UnicoreSiteEntity> listUserSites(OxidesAuthenticationSession authenticationSession,
                                                 HttpServletResponse response) {
        if (isValidAuthenticationSession(authenticationSession)) {
            return siteHandler.retrieveUserSiteList(authenticationSession);
        }
        authenticationSession.setReturnUrl("/oxides/unicore-sites");
        return redirectToAuthentication(response);
    }

    public List<UnicoreStorageEntity> listUserStorages(OxidesAuthenticationSession authenticationSession,
                                                       HttpServletResponse response) {
        if (isValidAuthenticationSession(authenticationSession)) {
            return storageHandler.retrieveUserStorageList(authenticationSession);
        }
        authenticationSession.setReturnUrl("/oxides/unicore-storages");
        return redirectToAuthentication(response);
    }

    public List<UnicoreJobEntity> listUserJobs(OxidesAuthenticationSession authenticationSession,
                                               HttpServletResponse response) {
        if (isValidAuthenticationSession(authenticationSession)) {
            return jobHandler.retrieveUserJobsList(authenticationSession);
        }
        authenticationSession.setReturnUrl("/oxides/unicore-jobs");
        return redirectToAuthentication(response);
    }

    private boolean isValidAuthenticationSession(OxidesAuthenticationSession authenticationSession) {
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

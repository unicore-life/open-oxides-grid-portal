package pl.edu.icm.oxides;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;
import pl.edu.icm.oxides.authn.SamlAuthenticationHandler;
import pl.edu.icm.oxides.unicore.UnicoreGridHandler;
import pl.edu.icm.oxides.unicore.central.tss.UnicoreSiteEntity;
import pl.edu.icm.oxides.unicore.site.job.UnicoreJobEntity;
import pl.edu.icm.oxides.unicore.site.resource.UnicoreResourceEntity;
import pl.edu.icm.oxides.unicore.site.storage.UnicoreSiteStorageEntity;
import pl.edu.icm.oxides.user.AuthenticationSession;
import pl.edu.icm.oxides.user.OxidesUsersHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;

import static java.util.Optional.ofNullable;

@Controller
@SessionAttributes("authenticationSession")
@RequestMapping(value = "/oxides")
public class OxidesController {
    private final SamlAuthenticationHandler samlAuthenticationHandler;
    private final UnicoreGridHandler unicoreGridHandler;
    private final OxidesPagesHandler oxidesPagesHandler;
    private final OxidesUsersHandler oxidesUsersHandler;
    private AuthenticationSession authenticationSession;

    @Autowired
    public OxidesController(SamlAuthenticationHandler samlAuthenticationHandler,
                            UnicoreGridHandler unicoreGridHandler, OxidesPagesHandler oxidesPagesHandler,
                            OxidesUsersHandler oxidesUsersHandler, AuthenticationSession authenticationSession) {
        this.samlAuthenticationHandler = samlAuthenticationHandler;
        this.unicoreGridHandler = unicoreGridHandler;
        this.oxidesPagesHandler = oxidesPagesHandler;
        this.oxidesUsersHandler = oxidesUsersHandler;
        this.authenticationSession = authenticationSession;
    }

    @RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
    public ModelAndView welcomePage() {
        return oxidesPagesHandler.modelWelcomePage(ofNullable(authenticationSession));
    }

    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public ModelAndView userPreferences() {
        return oxidesUsersHandler.modelPreferencesPage(ofNullable(authenticationSession));
    }

    @RequestMapping(value = "logout", method = RequestMethod.GET)
    public String signOut(HttpSession session) {
        return oxidesUsersHandler.signOut(session);
    }

    @RequestMapping(value = "/unicore-sites")
    @ResponseBody
    public List<UnicoreSiteEntity> listSites(HttpSession session, HttpServletResponse response) {
        logSessionData("SITES", session, authenticationSession);
        return unicoreGridHandler.listUserSites(authenticationSession, response);
    }

    @RequestMapping(value = "/unicore-storages")
    @ResponseBody
    public List<UnicoreSiteStorageEntity> listStorages(HttpSession session, HttpServletResponse response) {
        logSessionData("STORAGES", session, authenticationSession);
        return unicoreGridHandler.listUserStorages(authenticationSession, response);
    }

    @RequestMapping(value = "/unicore-jobs")
    @ResponseBody
    public List<UnicoreJobEntity> listJobs(HttpSession session, HttpServletResponse response) {
        logSessionData("JOBS", session, authenticationSession);
        return unicoreGridHandler.listUserJobs(authenticationSession, response);
    }

    @RequestMapping(value = "/unicore-resources")
    @ResponseBody
    public List<UnicoreResourceEntity> listResources(HttpSession session, HttpServletResponse response) {
        logSessionData("RESOURCES", session, authenticationSession);
        return unicoreGridHandler.listUserResources(authenticationSession, response);
    }

    @RequestMapping(value = "/authn", method = RequestMethod.GET)
    public void performAuthenticationRequest(HttpSession session, HttpServletResponse response,
                                             @RequestParam(value = "returnUrl", required = false) String returnUrl) {
        if (authenticationSession.getReturnUrl() == null && returnUrl != null) {
            authenticationSession.setReturnUrl(returnUrl);
        }
        logSessionData("SAML-G", session, authenticationSession);
        samlAuthenticationHandler.performAuthenticationRequest(response, authenticationSession);
    }

    @RequestMapping(value = "/authn", method = RequestMethod.POST)
    public void processAuthenticationResponse(HttpServletRequest request, HttpServletResponse response) {
        logSessionData("SAML-P", request.getSession(), authenticationSession);
        samlAuthenticationHandler.processAuthenticationResponse(request, response, authenticationSession);
    }

    private void logSessionData(String logPrefix, HttpSession session, AuthenticationSession authnSession) {
        log.info(String.format("%10s: %s", logPrefix, session.getId()));
        log.info(String.format("%10s: %s", logPrefix, authnSession));
    }

    private Log log = LogFactory.getLog(OxidesController.class);
}

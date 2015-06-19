package pl.edu.icm.oxides;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import pl.edu.icm.oxides.authn.OxidesAuthenticationSession;
import pl.edu.icm.oxides.authn.OxidesSamlRequestHandler;
import pl.edu.icm.oxides.authn.OxidesSamlResponseHandler;
import pl.edu.icm.oxides.unicore.UnicoreGridHandler;
import pl.edu.icm.oxides.unicore.job.UnicoreJobEntity;
import pl.edu.icm.oxides.unicore.site.UnicoreSiteEntity;
import pl.edu.icm.oxides.unicore.storage.UnicoreStorageEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@Controller
@SessionAttributes("authenticationSession")
@RequestMapping(value = "/oxides")
public class OxidesController {
    private final OxidesSamlRequestHandler samlRequestHandler;
    private final OxidesSamlResponseHandler samlResponseHandler;
    private final UnicoreGridHandler unicoreGridHandler;
    private OxidesAuthenticationSession authenticationSession;

    @Autowired
    public OxidesController(OxidesSamlRequestHandler samlRequestHandler, OxidesSamlResponseHandler samlResponseHandler,
                            UnicoreGridHandler unicoreGridHandler, OxidesAuthenticationSession authenticationSession) {
        this.samlRequestHandler = samlRequestHandler;
        this.samlResponseHandler = samlResponseHandler;
        this.unicoreGridHandler = unicoreGridHandler;
        this.authenticationSession = authenticationSession;
    }

    @RequestMapping(value = "/")
    public void mainView(HttpSession session, HttpServletResponse response) throws IOException {
        if (authenticationSession.getReturnUrl() == null)
            authenticationSession.setReturnUrl("/oxides/final");

        logSessionData("TEST-0", session, authenticationSession);
        response.sendRedirect("/oxides/authn");
    }

    @RequestMapping(value = "/final")
    @ResponseBody
    public String finalPage(HttpSession session, HttpServletResponse response) throws IOException {
        logSessionData("TEST-F", session, authenticationSession);
        return authenticationSession.toString() + "<p><a href=\"/oxides/linked\">link</a></p>";
    }

    @RequestMapping(value = "/linked")
    @ResponseBody
    public String linkedPage(HttpSession session, HttpServletResponse response) throws IOException {
        logSessionData("LINKED", session, authenticationSession);
        return authenticationSession.toString();
    }

    @RequestMapping(value = "/unicore-sites")
    @ResponseBody
    public List<UnicoreSiteEntity> listSites(HttpSession session, HttpServletResponse response) {
        logSessionData("SITES", session, authenticationSession);
        List<UnicoreSiteEntity> userSites = unicoreGridHandler.listUserSites(authenticationSession, response);
        logSessionData("SITES", session, authenticationSession);
        return userSites;
    }

    @RequestMapping(value = "/unicore-storages")
    @ResponseBody
    public List<UnicoreStorageEntity> listStorages(HttpSession session, HttpServletResponse response) {
        logSessionData("STORAGES", session, authenticationSession);
        return unicoreGridHandler.listUserStorages(authenticationSession, response);
    }

    @RequestMapping(value = "/unicore-jobs")
    @ResponseBody
    public List<UnicoreJobEntity> listJobs(HttpSession session, HttpServletResponse response) {
        logSessionData("JOBS", session, authenticationSession);
        return unicoreGridHandler.listUserJobs(authenticationSession, response);
    }

    @RequestMapping(value = "/authn", method = RequestMethod.GET)
    public void performAuthenticationRequest(HttpSession session, HttpServletResponse response) {
        logSessionData("SAML-G", session, authenticationSession);
        samlRequestHandler.performAuthenticationRequest(response, authenticationSession);
    }

    @RequestMapping(value = "/authn", method = RequestMethod.POST)
    public void processAuthenticationResponse(HttpServletRequest request, HttpServletResponse response) {
        logSessionData("SAML-P", request.getSession(), authenticationSession);
        samlResponseHandler.processAuthenticationResponse(request, response, authenticationSession);
    }

    private void logSessionData(String logPrefix, HttpSession session, OxidesAuthenticationSession authnSession) {
        log.info(String.format("%s: %s", logPrefix, session.getId()));
        log.info(String.format("%s: %s", logPrefix, authnSession));
    }

    private Log log = LogFactory.getLog(OxidesController.class);
}

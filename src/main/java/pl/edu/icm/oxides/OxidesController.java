package pl.edu.icm.oxides;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;
import pl.edu.icm.oxides.authn.SamlAuthenticationHandler;
import pl.edu.icm.oxides.portal.OxidesGridPortalPages;
import pl.edu.icm.oxides.simulation.model.OxidesSimulation;
import pl.edu.icm.oxides.unicore.UnicoreGridResources;
import pl.edu.icm.oxides.user.AuthenticationSession;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.UUID;

@Controller
@SessionAttributes("authenticationSession")
@RequestMapping(value = "/oxides")
public class OxidesController {
    private final OxidesGridPortalPages oxidesGridPortalPages;
    private final UnicoreGridResources unicoreGridResources;
    private final SamlAuthenticationHandler samlAuthenticationHandler;
    private AuthenticationSession authenticationSession;

    @Autowired
    public OxidesController(OxidesGridPortalPages oxidesGridPortalPages,
                            UnicoreGridResources unicoreGridResources,
                            SamlAuthenticationHandler samlAuthenticationHandler,
                            AuthenticationSession authenticationSession) {
        this.oxidesGridPortalPages = oxidesGridPortalPages;
        this.unicoreGridResources = unicoreGridResources;
        this.samlAuthenticationHandler = samlAuthenticationHandler;
        this.authenticationSession = authenticationSession;
    }

    @RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
    public ModelAndView welcomePage() {
        return oxidesGridPortalPages.modelWelcomePage(authenticationSession);
    }

    @RequestMapping(value = "/simulations", method = RequestMethod.GET)
    public ModelAndView simulationsPage() {
        return oxidesGridPortalPages.modelSimulationsPage(authenticationSession);
    }

    @RequestMapping(value = "/simulations/{uuid}", method = RequestMethod.GET)
    public ModelAndView oneSimulationPage(@PathVariable("uuid") UUID simulationUuid) {
        return oxidesGridPortalPages.modelOneSimulationPage(authenticationSession, simulationUuid);
    }

    @RequestMapping(value = "/simulations/submit", method = RequestMethod.GET)
    public ModelAndView submitSimulationPage() {
        return oxidesGridPortalPages.modelSubmitSimulationPage(authenticationSession);
    }

    @RequestMapping(value = "/preferences", method = RequestMethod.GET)
    public ModelAndView preferencesPage() {
        return oxidesGridPortalPages.modelPreferencesPage(authenticationSession);
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String signOut(HttpSession session) {
        return oxidesGridPortalPages.signOutAndRedirect(session);
    }

    /*
    ==========================================================================================================
     */

    @RequestMapping(value = "/unicore/submit", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Void> submitSimulation(@RequestBody OxidesSimulation simulation) {
        log.info("Submitted UNICORE Job: " + simulation);
        return unicoreGridResources.submitSimulation(simulation, authenticationSession);
    }

    @RequestMapping(value = "/unicore-sites", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List> listSites(HttpSession session) {
        logSessionData("SITES", session, authenticationSession);
        return unicoreGridResources.listUserSites(authenticationSession);
    }

    @RequestMapping(value = "/unicore-storages", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List> listStorages(HttpSession session) {
        logSessionData("STORAGES", session, authenticationSession);
        return unicoreGridResources.listUserStorages(authenticationSession);
    }

    @RequestMapping(value = "/unicore-jobs", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List> listJobs(HttpSession session) {
        logSessionData("JOBS", session, authenticationSession);
        return unicoreGridResources.listUserJobs(authenticationSession);
    }

    @RequestMapping(value = "/unicore-resources", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List> listResources(HttpSession session) {
        logSessionData("RESOURCES", session, authenticationSession);
        return unicoreGridResources.listUserResources(authenticationSession);
    }

    /*
    ==========================================================================================================
     */

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
    public String processAuthenticationResponse(HttpServletRequest request) {
        logSessionData("SAML-P", request.getSession(), authenticationSession);
        return samlAuthenticationHandler.processAuthenticationResponse(request, authenticationSession);
    }

    private void logSessionData(String logPrefix, HttpSession session, AuthenticationSession authnSession) {
        log.info(String.format("%10s: %s", logPrefix, session.getId()));
        log.info(String.format("%10s: %s", logPrefix, authnSession));
    }

    private Log log = LogFactory.getLog(OxidesController.class);
}

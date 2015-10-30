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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import pl.edu.icm.oxides.authn.SamlAuthenticationHandler;
import pl.edu.icm.oxides.open.OpenOxidesResources;
import pl.edu.icm.oxides.open.model.Oxide;
import pl.edu.icm.oxides.portal.OxidesGridPortalPages;
import pl.edu.icm.oxides.portal.model.OxidesSimulation;
import pl.edu.icm.oxides.unicore.UnicoreGridResources;
import pl.edu.icm.oxides.user.AuthenticationSession;
import pl.edu.icm.oxides.user.UserResourcesManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

import static pl.edu.icm.oxides.config.PortalConfig.QUANTUM_ESPRESSO_SUBMISSION_MAPPING;

@Controller
@SessionAttributes("authenticationSession")
public class OxidesEndpoints {
    private final OxidesGridPortalPages oxidesGridPortalPages;
    private final UnicoreGridResources unicoreGridResources;
    private final OpenOxidesResources openOxidesResources;
    private final SamlAuthenticationHandler samlAuthenticationHandler;
    private final UserResourcesManager userResourcesManager;
    private AuthenticationSession authenticationSession;

    @Autowired
    public OxidesEndpoints(OxidesGridPortalPages oxidesGridPortalPages,
                           UnicoreGridResources unicoreGridResources,
                           OpenOxidesResources openOxidesResources,
                           SamlAuthenticationHandler samlAuthenticationHandler,
                           UserResourcesManager userResourcesManager,
                           AuthenticationSession authenticationSession) {
        this.oxidesGridPortalPages = oxidesGridPortalPages;
        this.unicoreGridResources = unicoreGridResources;
        this.openOxidesResources = openOxidesResources;
        this.samlAuthenticationHandler = samlAuthenticationHandler;
        this.userResourcesManager = userResourcesManager;
        this.authenticationSession = authenticationSession;
    }


    /*
        MVC ENDPOINTS:
    ==========================================================================================================
    */
    @RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
    public ModelAndView welcomePage() {
        return oxidesGridPortalPages.modelWelcomePage(authenticationSession);
    }

    @RequestMapping(value = "/simulations", method = RequestMethod.GET)
    public ModelAndView simulationsPage() {
        return oxidesGridPortalPages.modelSimulationsPage(authenticationSession);
    }

    @RequestMapping(value = "/simulations/{uuid}/details", method = RequestMethod.GET)
    public ModelAndView simulationDetailsPage(@PathVariable("uuid") UUID simulationUuid) {
        return oxidesGridPortalPages.modelSimulationDetailsPage(authenticationSession, simulationUuid);
    }

    @RequestMapping(value = "/simulations/{uuid}/files", method = RequestMethod.GET)
    public ModelAndView oneSimulationPage(@PathVariable("uuid") UUID simulationUuid,
                                          @RequestParam(value = "path", required = false) String path) {
        return oxidesGridPortalPages.modelOneSimulationPage(authenticationSession, simulationUuid, path);
    }

    @RequestMapping(value = "/simulations/{uuid}/jsmol", method = RequestMethod.GET)
    public ModelAndView simulationJsmolViewerPage(@PathVariable("uuid") UUID simulationUuid,
                                                  @RequestParam(value = "path", required = true) String path) {
        return oxidesGridPortalPages.modelJsmolViewerPage(authenticationSession, simulationUuid, path);
    }

    @RequestMapping(value = "/simulations/submit", method = RequestMethod.GET)
    public ModelAndView submitSimulationPage() {
        return oxidesGridPortalPages.modelSubmitSimulationPage(authenticationSession);
    }

    @RequestMapping(value = QUANTUM_ESPRESSO_SUBMISSION_MAPPING, method = RequestMethod.GET)
    public ModelAndView submitQuantumEspressoSimulationPage() {
        return oxidesGridPortalPages.modelSubmitQuantumEspressoSimulationPage(authenticationSession);
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
            JSON ENDPOINTS:
    ==========================================================================================================
     */
    @RequestMapping(value = "/unicore/submit", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Void> submitSimulation(@RequestBody @Valid OxidesSimulation simulation) {
        log.info("Submitted UNICORE Job: " + simulation);
        return unicoreGridResources.submitWorkAssignment(simulation, authenticationSession);
    }

    @RequestMapping(value = "/unicore/submit/qe", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Void> submitQuantumEspressoSimulation(@RequestBody @Valid OxidesSimulation simulation) {
        log.info("Submitted QE Job: " + simulation);
        return unicoreGridResources.submitQEWorkAssignment(simulation, authenticationSession);
    }

    @RequestMapping(value = "/unicore/upload", method = RequestMethod.POST,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    @ResponseBody
    public ResponseEntity<String> uploadSimulationFile(@RequestParam("uploadFile") MultipartFile file,
                                                       HttpSession session) {
        logSessionData("UNICORE-UPLOAD", session, authenticationSession);
        return unicoreGridResources.uploadFile(file, authenticationSession);
    }

    @RequestMapping(value = "/unicore/jobs", method = RequestMethod.GET,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List> listSimulations(HttpSession session) {
        logSessionData("UNICORE-JOBS", session, authenticationSession);
        return unicoreGridResources.listUserJobs(authenticationSession);
    }

    @RequestMapping(value = "/unicore/jobs/{uuid}", method = RequestMethod.DELETE,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Void> destroySimulation(@PathVariable(value = "uuid") UUID simulationUuid,
                                                  HttpSession session) {
        logSessionData("DELETE-UNICORE-JOB", session, authenticationSession);
        return unicoreGridResources.destroyUserJob(simulationUuid, authenticationSession);
    }

    @RequestMapping(value = "/unicore/jobs/{uuid}/file", method = RequestMethod.GET)
    public ResponseEntity<Void> downloadSimulationFile(
            @PathVariable(value = "uuid") UUID simulationUuid,
            @RequestParam(value = "path", required = false) String path,
            HttpServletResponse response,
            HttpSession session) {
        logSessionData("UNICORE-JOB-DOWNLOAD", session, authenticationSession);
        return unicoreGridResources.downloadUserJobFile(simulationUuid, path, response, authenticationSession);
    }

    @RequestMapping(value = "/unicore/jobs/{uuid}/files", method = RequestMethod.GET,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List> listSimulationFiles(@PathVariable(value = "uuid") UUID simulationUuid,
                                                    @RequestParam(value = "path", required = false) String path,
                                                    HttpSession session) {
        logSessionData("UNICORE-JOB-FILES", session, authenticationSession);
        return unicoreGridResources.listUserJobFiles(simulationUuid, path, authenticationSession);
    }


    /*
            OPEN OXIDES PORTAL ENDPOINTS:
    ==========================================================================================================
     */
    @RequestMapping(value = "/oxides/data", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> retrieveStructureData(@RequestParam(value = "name") String name,
                                                        HttpSession session) {
        logSessionData("OPEN-OXIDES", session, authenticationSession);
        return openOxidesResources.getParticleParameters(name, authenticationSession);
    }

    @RequestMapping(value = "/oxides/login", method = RequestMethod.GET)
    public void performAuthenticationRequest(HttpServletResponse response) {
        authenticationSession.setReturnUrl("http://openoxides.icm.edu.pl");
        samlAuthenticationHandler.performAuthenticationRequest(response, authenticationSession);
    }

    @RequestMapping(value = "/oxides/results", method = RequestMethod.GET)
    @ResponseBody
    public List<Oxide> retrieveOpenOxidesResults() {
        return openOxidesResources.getOpenOxidesResults();
    }


    /*
            AUTHENTICATION ENDPOINTS:
    ==========================================================================================================
     */
    @RequestMapping(value = "/oxides/authn", method = RequestMethod.GET)
    public void performAuthenticationRequest(@RequestParam(value = "returnUrl", required = false) String returnUrl,
                                             HttpSession session,
                                             HttpServletResponse response) {
        if (authenticationSession.getReturnUrl() == null && returnUrl != null) {
            authenticationSession.setReturnUrl(returnUrl);
        }
        logSessionData("SAML-G", session, authenticationSession);
        samlAuthenticationHandler.performAuthenticationRequest(response, authenticationSession);
    }

    @RequestMapping(value = "/oxides/authn", method = RequestMethod.POST)
    public String processAuthenticationResponse(HttpServletRequest request) {
        logSessionData("SAML-P", request.getSession(), authenticationSession);
        return processResponseAndUserSessionInitialization(request, authenticationSession);
    }

    private String processResponseAndUserSessionInitialization(HttpServletRequest request,
                                                               AuthenticationSession authenticationSession) {
        String returnUrl = samlAuthenticationHandler.processAuthenticationResponse(request, authenticationSession);
        userResourcesManager.initializeAfterSuccessfulSignIn(authenticationSession);
        return returnUrl;
    }


    /*
            HELPER METHOD:
    ==========================================================================================================
     */
    private void logSessionData(String logPrefix, HttpSession session, AuthenticationSession authnSession) {
        log.info(String.format("%10s: %s", logPrefix, session.getId()));
        log.info(String.format("%10s: %s", logPrefix, authnSession));
    }


    private Log log = LogFactory.getLog(OxidesEndpoints.class);
}

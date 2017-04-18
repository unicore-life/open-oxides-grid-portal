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
import pl.edu.icm.oxides.authn.UnityAuthenticationHandler;
import pl.edu.icm.oxides.open.OpenOxidesResources;
import pl.edu.icm.oxides.open.model.Oxide;
import pl.edu.icm.oxides.portal.OxidesGridPortalPages;
import pl.edu.icm.oxides.portal.model.OxidesSimulation;
import pl.edu.icm.oxides.unicore.UnicoreGridResources;
import pl.edu.icm.oxides.user.OxidesPortalGridSession;
import pl.edu.icm.oxides.user.UserResourcesManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

import static pl.edu.icm.oxides.config.PortalConfig.QUANTUM_ESPRESSO_SUBMISSION_MAPPING;
import static pl.edu.icm.oxides.config.PortalConfig.SCRIPT_SUBMISSION_MAPPING;
import static pl.edu.icm.oxides.config.PortalConfig.SIMULATIONS_MAPPING;

@Controller
@SessionAttributes("authenticationSession")
public class OxidesEndpoints {
    private final OxidesGridPortalPages oxidesGridPortalPages;
    private final UnicoreGridResources unicoreGridResources;
    private final OpenOxidesResources openOxidesResources;
    private final UnityAuthenticationHandler unityAuthenticationHandler;
    private final UserResourcesManager userResourcesManager;
    private OxidesPortalGridSession oxidesPortalGridSession;

    @Autowired
    public OxidesEndpoints(OxidesGridPortalPages oxidesGridPortalPages,
                           UnicoreGridResources unicoreGridResources,
                           OpenOxidesResources openOxidesResources,
                           UnityAuthenticationHandler unityAuthenticationHandler,
                           UserResourcesManager userResourcesManager,
                           OxidesPortalGridSession oxidesPortalGridSession) {
        this.oxidesGridPortalPages = oxidesGridPortalPages;
        this.unicoreGridResources = unicoreGridResources;
        this.openOxidesResources = openOxidesResources;
        this.unityAuthenticationHandler = unityAuthenticationHandler;
        this.userResourcesManager = userResourcesManager;
        this.oxidesPortalGridSession = oxidesPortalGridSession;
    }


    /*
        MVC ENDPOINTS:
    ==========================================================================================================
    */
    @RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
    public ModelAndView welcomePage() {
        return oxidesGridPortalPages.modelWelcomePage(oxidesPortalGridSession);
    }

    @RequestMapping(value = SIMULATIONS_MAPPING, method = RequestMethod.GET)
    public ModelAndView simulationsPage() {
        return oxidesGridPortalPages.modelSimulationsPage(oxidesPortalGridSession);
    }

    @RequestMapping(value = "/simulations/{uuid}/details", method = RequestMethod.GET)
    public ModelAndView simulationDetailsPage(@PathVariable("uuid") UUID simulationUuid) {
        return oxidesGridPortalPages.modelSimulationDetailsPage(oxidesPortalGridSession, simulationUuid);
    }

    @RequestMapping(value = "/simulations/{uuid}/files", method = RequestMethod.GET)
    public ModelAndView oneSimulationPage(@PathVariable("uuid") UUID simulationUuid,
                                          @RequestParam(value = "path", required = false) String path) {
        return oxidesGridPortalPages.modelOneSimulationPage(oxidesPortalGridSession, simulationUuid, path);
    }

    @RequestMapping(value = "/simulations/{uuid}/jsmol", method = RequestMethod.GET)
    public ModelAndView simulationJsmolViewerPage(@PathVariable("uuid") UUID simulationUuid,
                                                  @RequestParam(value = "path", required = true) String path) {
        return oxidesGridPortalPages.modelJsmolViewerPage(oxidesPortalGridSession, simulationUuid, path);
    }

    @RequestMapping(value = SCRIPT_SUBMISSION_MAPPING, method = RequestMethod.GET)
    public ModelAndView submitSimulationPage() {
        return oxidesGridPortalPages.modelSubmitScriptSimulationPage(oxidesPortalGridSession);
    }

    @RequestMapping(value = QUANTUM_ESPRESSO_SUBMISSION_MAPPING, method = RequestMethod.GET)
    public ModelAndView submitQuantumEspressoSimulationPage() {
        return oxidesGridPortalPages.modelSubmitQuantumEspressoSimulationPage(oxidesPortalGridSession);
    }

    @RequestMapping(value = "/preferences", method = RequestMethod.GET)
    public ModelAndView preferencesPage() {
        return oxidesGridPortalPages.modelPreferencesPage(oxidesPortalGridSession);
    }

    @RequestMapping(value = "/error/forbidden", method = RequestMethod.GET)
    public ModelAndView errorForbidden(HttpSession session) {
        return oxidesGridPortalPages.modelForbiddenErrorPage(session);
    }

    @RequestMapping(value = "/error/no-etd", method = RequestMethod.GET)
    public ModelAndView errorNoTrustDelegation(HttpSession session) {
        return oxidesGridPortalPages.modelNoTrustDelegationErrorPage(session);
    }


    /*
            JSON ENDPOINTS:
    ==========================================================================================================
     */
    @RequestMapping(value = "/unicore/submit/script", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Void> submitScriptSimulation(@RequestBody @Valid OxidesSimulation simulation) {
        log.info("Submitted UNICORE Job: " + simulation);
        return unicoreGridResources.submitScriptWorkAssignment(simulation, oxidesPortalGridSession);
    }

    @RequestMapping(value = "/unicore/submit/qe", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Void> submitQuantumEspressoSimulation(@RequestBody @Valid OxidesSimulation simulation) {
        log.info("Submitted QE Job: " + simulation);
        return unicoreGridResources.submitQuantumEspressoWorkAssignment(simulation, oxidesPortalGridSession);
    }

    @RequestMapping(value = "/unicore/upload", method = RequestMethod.POST,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    @ResponseBody
    public ResponseEntity<String> uploadSimulationFile(@RequestParam("uploadFile") MultipartFile file,
                                                       HttpSession session) {
        logSessionData("UNICORE-UPLOAD", session, oxidesPortalGridSession);
        return unicoreGridResources.uploadFile(file, oxidesPortalGridSession);
    }

    @RequestMapping(value = "/unicore/jobs", method = RequestMethod.GET,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List> listSimulations(HttpSession session) {
        logSessionData("UNICORE-JOBS", session, oxidesPortalGridSession);
        return unicoreGridResources.listUserJobs(oxidesPortalGridSession);
    }

    @RequestMapping(value = "/unicore/jobs/{uuid}", method = RequestMethod.DELETE,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Void> destroySimulation(@PathVariable(value = "uuid") UUID simulationUuid,
                                                  HttpSession session) {
        logSessionData("DELETE-UNICORE-JOB", session, oxidesPortalGridSession);
        return unicoreGridResources.destroyUserJob(simulationUuid, oxidesPortalGridSession);
    }

    @RequestMapping(value = "/unicore/jobs/{uuid}/file", method = RequestMethod.GET)
    public ResponseEntity<Void> downloadSimulationFile(
            @PathVariable(value = "uuid") UUID simulationUuid,
            @RequestParam(value = "path", required = false) String path,
            HttpServletResponse response,
            HttpSession session) {
        logSessionData("UNICORE-JOB-DOWNLOAD", session, oxidesPortalGridSession);
        return unicoreGridResources.downloadUserJobFile(simulationUuid, path, response, oxidesPortalGridSession);
    }

    @RequestMapping(value = "/unicore/jobs/{uuid}/files", method = RequestMethod.GET,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List> listSimulationFiles(@PathVariable(value = "uuid") UUID simulationUuid,
                                                    @RequestParam(value = "path", required = false) String path,
                                                    HttpSession session) {
        logSessionData("UNICORE-JOB-FILES", session, oxidesPortalGridSession);
        return unicoreGridResources.listUserJobFiles(simulationUuid, path, oxidesPortalGridSession);
    }


    /*
            OPEN OXIDES PORTAL ENDPOINTS:
    ==========================================================================================================
     */
    @RequestMapping(value = "/oxides/data", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> retrieveStructureData(@RequestParam(value = "name") String name,
                                                        HttpSession session) {
        logSessionData("OPEN-OXIDES", session, oxidesPortalGridSession);
        return openOxidesResources.getParticleParameters(name, oxidesPortalGridSession);
    }

    @RequestMapping(value = "/oxides/login", method = RequestMethod.GET)
    public void performAuthenticationRequest(HttpServletResponse response) {
        oxidesPortalGridSession.setReturnUrl("http://openoxides.icm.edu.pl");
        unityAuthenticationHandler.performAuthenticationRequest(response, oxidesPortalGridSession);
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
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public void performAuthenticationRequest(@RequestParam(value = "returnUrl", required = false) String returnUrl,
                                             HttpSession session,
                                             HttpServletResponse response) {
        regenerateSessionToAvoidSessionFixationAfterSignedIn(session);
        if (oxidesPortalGridSession.getReturnUrl() == null && returnUrl != null) {
            oxidesPortalGridSession.setReturnUrl(returnUrl);
        }
        logSessionData("SAML-G", session, oxidesPortalGridSession);
        unityAuthenticationHandler.performAuthenticationRequest(response, oxidesPortalGridSession);
    }

    @RequestMapping(value = "/authn/sign-in", method = RequestMethod.POST)
    public String processAuthenticationResponse(HttpServletRequest request) {
        logSessionData("SAML-P", request.getSession(), oxidesPortalGridSession);
        return processResponseAndUserSessionInitialization(request, oxidesPortalGridSession);
    }

    @RequestMapping(value = "/authn/sign-out", method = RequestMethod.POST)
    public String processLogoutResponse(HttpServletRequest request) {
        logSessionData("SAML-O", request.getSession(), oxidesPortalGridSession);
        return unityAuthenticationHandler.processSingleLogoutResponse(request);
    }

    private String processResponseAndUserSessionInitialization(HttpServletRequest request,
                                                               OxidesPortalGridSession oxidesPortalGridSession) {
        String returnUrl = unityAuthenticationHandler.processAuthenticationResponse(request, oxidesPortalGridSession);
        userResourcesManager.initializeAfterSuccessfulSignIn(oxidesPortalGridSession);
        return returnUrl;
    }


    /*
            HELPER METHOD:
    ==========================================================================================================
     */
    private void regenerateSessionToAvoidSessionFixationAfterSignedIn(HttpSession session) {
        session.invalidate();
    }

    private void logSessionData(String logPrefix, HttpSession session, OxidesPortalGridSession authnSession) {
        log.info(String.format("%10s: %s", logPrefix, session.getId()));
        log.info(String.format("%10s: %s", logPrefix, authnSession));
    }


    private Log log = LogFactory.getLog(OxidesEndpoints.class);
}

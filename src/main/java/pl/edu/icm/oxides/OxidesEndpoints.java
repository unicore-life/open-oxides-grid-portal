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

@Controller
@SessionAttributes("authenticationSession")
@RequestMapping(value = "/oxides")
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
    @RequestMapping(value = "/data", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> retrieveStructureData(@RequestParam(value = "name") String name,
                                                        HttpSession session) {
        logSessionData("OPEN-OXIDES", session, authenticationSession);
        return openOxidesResources.getParticleParameters(name, authenticationSession);
    }

    @RequestMapping(value = "/mol", method = RequestMethod.GET)
    @ResponseBody
    public String mol(HttpServletResponse response) {
        // TODO: remove after implementation
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Content-Type", "text/plain");

        String nl = "\n";
        String mol = "../xyz/MnO2_C12-M1.xyz" + nl +
                " OpenBabel06181523243D" + nl +
                "" + nl +
                "  9  4  0  0  0  0  0  0  0  0999 V2000" + nl +
                "    0.0000    0.0000    0.0000 Mn  0  0  0  0  0" + nl +
                "    8.9316    1.7683    2.5465 Mn  0  0  0  0  0" + nl +
                "    4.7894    0.9380    2.7718 Mn  0  0  0  0  0" + nl +
                "    5.9667    1.1638    4.1068 O   0  0  0  0  0" + nl +
                "    7.7543    1.5425    1.2115 O   0  0  0  0  0" + nl +
                "   12.3580    2.4573    2.0730 O   0  0  0  0  0" + nl +
                "    1.3630    0.2491    3.2453 O   0  0  0  0  0" + nl +
                "   10.4767    2.0684    3.7868 O   0  0  0  0  0" + nl +
                "    3.2443    0.6379    1.5315 O   0  0  0  0  0" + nl +
                "  5  2  1  0  0  0" + nl +
                "  2  8  1  0  0  0" + nl +
                "  9  3  1  0  0  0" + nl +
                "  3  4  1  0  0  0" + nl +
                "M  END";
        return mol;
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public void performAuthenticationRequest(HttpServletResponse response) {
        authenticationSession.setReturnUrl("http://openoxides.icm.edu.pl");
        samlAuthenticationHandler.performAuthenticationRequest(response, authenticationSession);
    }


    /*
            AUTHENTICATION ENDPOINTS:
    ==========================================================================================================
     */
    @RequestMapping(value = "/authn", method = RequestMethod.GET)
    public void performAuthenticationRequest(@RequestParam(value = "returnUrl", required = false) String returnUrl,
                                             HttpSession session,
                                             HttpServletResponse response) {
        if (authenticationSession.getReturnUrl() == null && returnUrl != null) {
            authenticationSession.setReturnUrl(returnUrl);
        }
        logSessionData("SAML-G", session, authenticationSession);
        samlAuthenticationHandler.performAuthenticationRequest(response, authenticationSession);
    }

    @RequestMapping(value = "/authn", method = RequestMethod.POST)
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

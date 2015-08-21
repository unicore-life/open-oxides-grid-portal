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
import pl.edu.icm.oxides.portal.OxidesGridPortalPages;
import pl.edu.icm.oxides.simulation.model.OxidesSimulation;
import pl.edu.icm.oxides.unicore.UnicoreGridResources;
import pl.edu.icm.oxides.user.AuthenticationSession;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
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
    public ModelAndView oneSimulationPage(@PathVariable("uuid") UUID simulationUuid,
                                          @RequestParam(value = "path", required = false) String path) {
        return oxidesGridPortalPages.modelOneSimulationPage(authenticationSession, simulationUuid, path);
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

    /* PLANNED JSON ENDPOINTS:
    ==========================================================================================================
     */

    @RequestMapping(value = "/unicore/submit", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Void> submitSimulation(@RequestBody @Valid OxidesSimulation simulation) {
        log.info("Submitted UNICORE Job: " + simulation);
        return unicoreGridResources.submitSimulation(simulation, authenticationSession);
    }

    @RequestMapping(value = "/unicore/upload", method = RequestMethod.POST,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    @ResponseBody
    public ResponseEntity<String> handleFileUpload(@RequestParam("uploadFile") MultipartFile file,
                                                   @RequestParam("destinationUri") String uri,
                                                   HttpSession session) {
        logSessionData("UNICORE-UPLOAD", session, authenticationSession);
        return unicoreGridResources.uploadFile(file, uri, authenticationSession);
    }

    @RequestMapping(value = "/unicore/jobs", method = RequestMethod.GET,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List> retrieveUnicoreJobs(HttpSession session) {
        logSessionData("UNICORE-JOBS", session, authenticationSession);
        return unicoreGridResources.listUserJobs(authenticationSession);
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


    @RequestMapping(value = "/unicore/files/{uuid}", method = RequestMethod.GET)
    public ResponseEntity<Void> downloadSimulationFile(
            @PathVariable(value = "uuid") UUID simulationUuid,
            @RequestParam(value = "path", required = false) String path,
            HttpServletResponse response,
            HttpSession session) {
        logSessionData("UNICORE-JOB-DOWNLOAD", session, authenticationSession);
        return unicoreGridResources.downloadUserJobFile(simulationUuid, path, response, authenticationSession);
    }

    /* TO BE DECIDED:
    ==========================================================================================================
     */

    @RequestMapping(value = "/unicore/jobs/{uuid}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<Void> destroyJob(@PathVariable(value = "uuid") UUID simulationUuid,
                                           HttpSession session) {
        logSessionData("DELETE-UNICORE-JOB", session, authenticationSession);
        return unicoreGridResources.destroyUserJob(simulationUuid, authenticationSession);
    }

    @RequestMapping(value = "/unicore-storages", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List> listStorages(HttpSession session) {
        logSessionData("STORAGES", session, authenticationSession);
        return unicoreGridResources.listUserStorages(authenticationSession);
    }

    /* TO BE REMOVED:
    ==========================================================================================================
     */

    @RequestMapping(value = "/unicore-sites", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List> listSites(HttpSession session) {
        logSessionData("SITES", session, authenticationSession);
        return unicoreGridResources.listUserSites(authenticationSession);
    }

    @RequestMapping(value = "/unicore-resources", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List> listResources(HttpSession session) {
        logSessionData("RESOURCES", session, authenticationSession);
        return unicoreGridResources.listUserResources(authenticationSession);
    }

    /* AUTHENTICATION ENDPOINTS:
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
        return samlAuthenticationHandler.processAuthenticationResponse(request, authenticationSession);
    }

    private void logSessionData(String logPrefix, HttpSession session, AuthenticationSession authnSession) {
        log.info(String.format("%10s: %s", logPrefix, session.getId()));
        log.info(String.format("%10s: %s", logPrefix, authnSession));
    }

    private Log log = LogFactory.getLog(OxidesController.class);
}

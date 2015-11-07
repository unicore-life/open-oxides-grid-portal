package pl.edu.icm.oxides.portal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import pl.edu.icm.oxides.portal.security.PortalAccess;
import pl.edu.icm.oxides.portal.security.PortalAccessHelper;
import pl.edu.icm.oxides.unicore.site.job.UnicoreJob;
import pl.edu.icm.oxides.user.AuthenticationSession;

import java.util.Optional;
import java.util.UUID;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringEscapeUtils.escapeEcmaScript;
import static pl.edu.icm.oxides.config.PortalConfig.QUANTUM_ESPRESSO_SUBMISSION_MAPPING;
import static pl.edu.icm.oxides.config.PortalConfig.SCRIPT_SUBMISSION_MAPPING;
import static pl.edu.icm.oxides.config.PortalConfig.SIMULATIONS_MAPPING;
import static pl.edu.icm.oxides.portal.security.PortalAccess.VALID;

@Service
class OxidesSimulationsPage {
    private final UnicoreJob unicoreJob;
    private final PortalAccessHelper accessHelper;

    @Autowired
    OxidesSimulationsPage(UnicoreJob unicoreJob, PortalAccessHelper accessHelper) {
        this.unicoreJob = unicoreJob;
        this.accessHelper = accessHelper;
    }

    ModelAndView modelSimulationsPage(AuthenticationSession authenticationSession) {
        PortalAccess portalAccess = accessHelper.determineSessionAccess(authenticationSession);
        if (portalAccess == VALID) {
            return prepareBasicModelAndView("simulations/main", ofNullable(authenticationSession));
        }
        authenticationSession.setReturnUrl(SIMULATIONS_MAPPING);
        return redirectModelAndView(portalAccess);
    }

    ModelAndView modelOneSimulationViewerPage(AuthenticationSession authenticationSession,
                                              UUID simulationUuid,
                                              Optional<String> path,
                                              String viewTemplateName) {
        PortalAccess portalAccess = accessHelper.determineSessionAccess(authenticationSession);
        if (portalAccess == VALID) {
            ModelAndView modelAndView = prepareBasicModelAndView(viewTemplateName, ofNullable(authenticationSession));
            modelAndView.addObject("uuid", simulationUuid.toString());
            modelAndView.addObject("path", path.orElse("/"));
            modelAndView.addObject("escapedPath", escapeEcmaScript(path.orElse("/")));
            return modelAndView;
        }
        authenticationSession.setReturnUrl(String.format("/simulations/%s/files", simulationUuid));
        return redirectModelAndView(portalAccess);
    }

    ModelAndView modelSimulationDetailsPage(AuthenticationSession authenticationSession, UUID simulationUuid) {
        PortalAccess portalAccess = accessHelper.determineSessionAccess(authenticationSession);
        if (portalAccess == VALID) {
            ModelAndView modelAndView = prepareBasicModelAndView("simulations/details", ofNullable(authenticationSession));
            modelAndView.addObject("uuid", simulationUuid.toString());
            modelAndView.addObject("details", unicoreJob.retrieveJobDetails(
                    simulationUuid, authenticationSession.getSelectedTrustDelegation()
            ));
            return modelAndView;
        }
        authenticationSession.setReturnUrl(String.format("/simulations/%s/details", simulationUuid));
        return redirectModelAndView(portalAccess);
    }

    ModelAndView modelSubmitScriptSimulationPage(AuthenticationSession authenticationSession) {
        PortalAccess portalAccess = accessHelper.determineSessionAccess(authenticationSession);
        if (portalAccess == VALID) {
            return prepareBasicModelAndView("simulations/submit", ofNullable(authenticationSession));
        }
        authenticationSession.setReturnUrl(SCRIPT_SUBMISSION_MAPPING);
        return redirectModelAndView(portalAccess);
    }

    ModelAndView modelSubmitQuantumEspressoSimulationPage(AuthenticationSession authenticationSession) {
        PortalAccess portalAccess = accessHelper.determineSessionAccess(authenticationSession);
        if (portalAccess == VALID) {
            return prepareBasicModelAndView("simulations/submit-qe", ofNullable(authenticationSession));
        }
        authenticationSession.setReturnUrl(QUANTUM_ESPRESSO_SUBMISSION_MAPPING);
        return redirectModelAndView(portalAccess);
    }

    private ModelAndView prepareBasicModelAndView(String htmlTemplateName,
                                                  Optional<AuthenticationSession> authenticationSession) {
        ModelAndView modelAndView = new ModelAndView(htmlTemplateName);
        modelAndView.addObject("commonName",
                authenticationSession
                        .map(AuthenticationSession::getCommonName)
                        .orElse("")
        );
        return modelAndView;
    }

    private ModelAndView redirectModelAndView(PortalAccess portalAccess) {
        switch (portalAccess) {
            case PAGE_UNAUTHORIZED:
                return new ModelAndView("redirect:/oxides/authn");
            case PAGE_FORBIDDEN:
                return new ModelAndView("redirect:/error/forbidden");
            case NO_TRUST_DELEGATION:
                return new ModelAndView("redirect:/error/no-etd");
            default:
                return new ModelAndView("redirect:/error");
        }
    }
}

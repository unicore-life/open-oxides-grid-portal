package pl.edu.icm.oxides.portal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import pl.edu.icm.oxides.portal.security.PortalAccess;
import pl.edu.icm.oxides.portal.security.PortalAccessHelper;
import pl.edu.icm.oxides.unicore.site.job.UnicoreJob;
import pl.edu.icm.oxides.user.OxidesPortalGridSession;

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

    ModelAndView modelSimulationsPage(OxidesPortalGridSession oxidesPortalGridSession) {
        PortalAccess portalAccess = accessHelper.determineSessionAccess(oxidesPortalGridSession);
        if (portalAccess == VALID) {
            return prepareBasicModelAndView("simulations/main", ofNullable(oxidesPortalGridSession));
        }
        return redirectModelAndView(portalAccess, SIMULATIONS_MAPPING);
    }

    ModelAndView modelOneSimulationViewerPage(OxidesPortalGridSession oxidesPortalGridSession,
                                              UUID simulationUuid,
                                              Optional<String> path,
                                              String viewTemplateName) {
        PortalAccess portalAccess = accessHelper.determineSessionAccess(oxidesPortalGridSession);
        if (portalAccess == VALID) {
            ModelAndView modelAndView = prepareBasicModelAndView(viewTemplateName, ofNullable(oxidesPortalGridSession));
            modelAndView.addObject("uuid", simulationUuid.toString());
            modelAndView.addObject("path", path.orElse("/"));
            modelAndView.addObject("escapedPath", escapeEcmaScript(path.orElse("/")));
            return modelAndView;
        }
        return redirectModelAndView(portalAccess, String.format("/simulations/%s/files", simulationUuid));
    }

    ModelAndView modelSimulationDetailsPage(OxidesPortalGridSession oxidesPortalGridSession, UUID simulationUuid) {
        PortalAccess portalAccess = accessHelper.determineSessionAccess(oxidesPortalGridSession);
        if (portalAccess == VALID) {
            ModelAndView modelAndView = prepareBasicModelAndView("simulations/details", ofNullable(oxidesPortalGridSession));
            modelAndView.addObject("uuid", simulationUuid.toString());
            modelAndView.addObject("details", unicoreJob.retrieveJobDetails(
                    simulationUuid, oxidesPortalGridSession.getSelectedTrustDelegation()
            ));
            return modelAndView;
        }
        return redirectModelAndView(portalAccess, String.format("/simulations/%s/details", simulationUuid));
    }

    ModelAndView modelSubmitScriptSimulationPage(OxidesPortalGridSession oxidesPortalGridSession) {
        PortalAccess portalAccess = accessHelper.determineSessionAccess(oxidesPortalGridSession);
        if (portalAccess == VALID) {
            return prepareBasicModelAndView("simulations/submit", ofNullable(oxidesPortalGridSession));
        }
        return redirectModelAndView(portalAccess, SCRIPT_SUBMISSION_MAPPING);
    }

    ModelAndView modelSubmitQuantumEspressoSimulationPage(OxidesPortalGridSession oxidesPortalGridSession) {
        PortalAccess portalAccess = accessHelper.determineSessionAccess(oxidesPortalGridSession);
        if (portalAccess == VALID) {
            return prepareBasicModelAndView("simulations/submit-qe", ofNullable(oxidesPortalGridSession));
        }
        return redirectModelAndView(portalAccess, QUANTUM_ESPRESSO_SUBMISSION_MAPPING);
    }

    private ModelAndView prepareBasicModelAndView(String htmlTemplateName,
                                                  Optional<OxidesPortalGridSession> authenticationSession) {
        ModelAndView modelAndView = new ModelAndView(htmlTemplateName);
        modelAndView.addObject("commonName",
                authenticationSession
                        .map(OxidesPortalGridSession::getCommonName)
                        .orElse("")
        );
        return modelAndView;
    }

    private ModelAndView redirectModelAndView(PortalAccess portalAccess, String returnUrl) {
        switch (portalAccess) {
            case PAGE_UNAUTHORIZED:
                return new ModelAndView("redirect:/login?returnUrl=" + returnUrl);
            case PAGE_FORBIDDEN:
                return new ModelAndView("redirect:/error/forbidden");
            case NO_TRUST_DELEGATION:
                return new ModelAndView("redirect:/error/no-etd");
            default:
                return new ModelAndView("redirect:/error");
        }
    }
}

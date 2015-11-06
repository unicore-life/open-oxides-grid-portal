package pl.edu.icm.oxides.portal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import pl.edu.icm.oxides.config.GridOxidesConfig;
import pl.edu.icm.oxides.unicore.site.job.UnicoreJob;
import pl.edu.icm.oxides.user.AuthenticationSession;

import java.util.Optional;
import java.util.UUID;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringEscapeUtils.escapeEcmaScript;
import static pl.edu.icm.oxides.config.PortalConfig.QUANTUM_ESPRESSO_SUBMISSION_MAPPING;
import static pl.edu.icm.oxides.config.PortalConfig.SCRIPT_SUBMISSION_MAPPING;
import static pl.edu.icm.oxides.config.PortalConfig.SIMULATIONS_MAPPING;

@Service
class OxidesSimulationsPage {
    private final UnicoreJob unicoreJob;
    private final GridOxidesConfig oxidesConfig;

    @Autowired
    OxidesSimulationsPage(UnicoreJob unicoreJob, GridOxidesConfig oxidesConfig) {
        this.unicoreJob = unicoreJob;
        this.oxidesConfig = oxidesConfig;
    }

    ModelAndView modelSimulationsPage(AuthenticationSession authenticationSession) {
        AccessType accessType = determineAccessType(authenticationSession);
        if (accessType == AccessType.OK) {
            return prepareBasicModelAndView("simulations/main", ofNullable(authenticationSession));
        }
        authenticationSession.setReturnUrl(SIMULATIONS_MAPPING);
        return redirectToAuthentication(accessType);
    }

    ModelAndView modelOneSimulationViewerPage(AuthenticationSession authenticationSession,
                                              UUID simulationUuid,
                                              Optional<String> path,
                                              String viewTemplateName) {
        if (isValidAuthenticationSession(authenticationSession)) {
            ModelAndView modelAndView = prepareBasicModelAndView(viewTemplateName, ofNullable(authenticationSession));
            modelAndView.addObject("uuid", simulationUuid.toString());
            modelAndView.addObject("path", path.orElse("/"));
            modelAndView.addObject("escapedPath", escapeEcmaScript(path.orElse("/")));
            return modelAndView;
        }
        authenticationSession.setReturnUrl(String.format("/simulations/%s/files", simulationUuid));
        return redirectToAuthentication();
    }

    ModelAndView modelSimulationDetailsPage(AuthenticationSession authenticationSession, UUID simulationUuid) {
        if (isValidAuthenticationSession(authenticationSession)) {
            ModelAndView modelAndView = prepareBasicModelAndView("simulations/details", ofNullable(authenticationSession));
            modelAndView.addObject("uuid", simulationUuid.toString());
            modelAndView.addObject("details", unicoreJob.retrieveJobDetails(
                    simulationUuid, authenticationSession.getSelectedTrustDelegation()
            ));
            return modelAndView;
        }
        authenticationSession.setReturnUrl(String.format("/simulations/%s/details", simulationUuid));
        return redirectToAuthentication();
    }

    ModelAndView modelSubmitScriptSimulationPage(AuthenticationSession authenticationSession) {
        if (isValidAuthenticationSession(authenticationSession)) {
            return prepareBasicModelAndView("simulations/submit", ofNullable(authenticationSession));
        }
        authenticationSession.setReturnUrl(SCRIPT_SUBMISSION_MAPPING);
        return redirectToAuthentication();
    }

    ModelAndView modelSubmitQuantumEspressoSimulationPage(AuthenticationSession authenticationSession) {
        if (isValidAuthenticationSession(authenticationSession)) {
            return prepareBasicModelAndView("simulations/submit-qe", ofNullable(authenticationSession));
        }
        authenticationSession.setReturnUrl(QUANTUM_ESPRESSO_SUBMISSION_MAPPING);
        return redirectToAuthentication();
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

    private boolean isValidAuthenticationSession(AuthenticationSession authenticationSession) {
        return authenticationSession != null
                && authenticationSession.getTrustDelegations() != null
                && authenticationSession.getTrustDelegations().size() > 0;
    }

    private ModelAndView redirectToAuthentication() {
        return new ModelAndView("redirect:/oxides/authn");
    }

    private AccessType determineAccessType(AuthenticationSession authenticationSession) {
        if (authenticationSession == null || authenticationSession.getTrustDelegations() == null) {
            return AccessType.UNAUTHORIZED;
        }
        if (!authenticationSession.isGroupMember(oxidesConfig.getAccessGroup())) {
            return AccessType.FORBIDDEN;
        }
        if (authenticationSession.getTrustDelegations().size() == 0) {
            return AccessType.NO_TRUST_DELEGATION;
        }
        return AccessType.OK;
    }

    private ModelAndView redirectToAuthentication(AccessType accessType) {
        switch (accessType) {
            case UNAUTHORIZED:
                return new ModelAndView("redirect:/oxides/authn");
            case FORBIDDEN:
                return new ModelAndView("redirect:/error/forbidden");
            case NO_TRUST_DELEGATION:
                return new ModelAndView("redirect:/error/no-etd");
            default:
                return new ModelAndView("redirect:/error");
        }
    }

    private enum AccessType {
        UNAUTHORIZED, FORBIDDEN, NO_TRUST_DELEGATION, OK
    }
}

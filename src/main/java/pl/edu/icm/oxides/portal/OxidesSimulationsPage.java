package pl.edu.icm.oxides.portal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import pl.edu.icm.oxides.unicore.site.job.UnicoreJob;
import pl.edu.icm.oxides.user.AuthenticationSession;

import java.util.Optional;
import java.util.UUID;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringEscapeUtils.escapeEcmaScript;

@Service
class OxidesSimulationsPage {
    private final UnicoreJob unicoreJob;

    @Autowired
    OxidesSimulationsPage(UnicoreJob unicoreJob) {
        this.unicoreJob = unicoreJob;
    }

    ModelAndView modelSimulationsPage(AuthenticationSession authenticationSession) {
        if (isValidAuthenticationSession(authenticationSession)) {
            return prepareBasicModelAndView("simulations/main", ofNullable(authenticationSession));
        }
        authenticationSession.setReturnUrl("/simulations");
        return redirectToAuthentication();
    }

    ModelAndView modelOneSimulationPage(AuthenticationSession authenticationSession,
                                        UUID simulationUuid,
                                        Optional<String> path) {
        if (isValidAuthenticationSession(authenticationSession)) {
            ModelAndView modelAndView = prepareBasicModelAndView("simulations/one", ofNullable(authenticationSession));
            modelAndView.addObject("uuid", simulationUuid.toString());
            modelAndView.addObject("path", path.orElse("/"));
            modelAndView.addObject("escapedPath", escapeEcmaScript(path.orElse("/")));
            return modelAndView;
        }
        authenticationSession.setReturnUrl(String.format("/simulations/%s", simulationUuid));
        return redirectToAuthentication();
    }

    public ModelAndView modelSimulationDetailsPage(AuthenticationSession authenticationSession,
                                                   UUID simulationUuid) {
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

    public ModelAndView modelJsmolViewerPage(AuthenticationSession authenticationSession,
                                             UUID simulationUuid,
                                             Optional<String> path) {
        if (isValidAuthenticationSession(authenticationSession)) {
            ModelAndView modelAndView = prepareBasicModelAndView("viewers/jsmol", ofNullable(authenticationSession));
            modelAndView.addObject("uuid", simulationUuid.toString());
            modelAndView.addObject("path", path.orElse("/"));
            modelAndView.addObject("escapedPath", escapeEcmaScript(path.orElse("/")));
            return modelAndView;
        }
        authenticationSession.setReturnUrl(String.format("/simulations/%s", simulationUuid));
        return redirectToAuthentication();
    }

    ModelAndView modelSubmitSimulationPage(AuthenticationSession authenticationSession) {
        if (isValidAuthenticationSession(authenticationSession)) {
            return prepareBasicModelAndView("simulations/submit", ofNullable(authenticationSession));
        }
        authenticationSession.setReturnUrl("/simulations/submit");
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
}

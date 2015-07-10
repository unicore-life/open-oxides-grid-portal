package pl.edu.icm.oxides.simulation;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import pl.edu.icm.oxides.user.AuthenticationSession;

import java.util.Optional;
import java.util.UUID;

import static java.util.Optional.ofNullable;

@Service
public class OxidesSimulationsPage {
    public ModelAndView modelSimulationsPage(AuthenticationSession authenticationSession) {
        if (isValidAuthenticationSession(authenticationSession)) {
            return prepareBasicModelAndView("simulations/main", ofNullable(authenticationSession));
        }
        authenticationSession.setReturnUrl("/oxides/simulations/main");
        return redirectToAuthentication();
    }

    public ModelAndView modelOneSimulationPage(AuthenticationSession authenticationSession, UUID simulationUuid) {
        if (isValidAuthenticationSession(authenticationSession)) {
            ModelAndView modelAndView = prepareBasicModelAndView("simulations/one", ofNullable(authenticationSession));
            modelAndView.addObject("uuid", simulationUuid.toString());
            return modelAndView;
        }
        authenticationSession.setReturnUrl(String.format("/oxides/simulations/%s", simulationUuid));
        return redirectToAuthentication();
    }

    public ModelAndView modelSubmitSimulationPage(AuthenticationSession authenticationSession) {
        if (isValidAuthenticationSession(authenticationSession)) {
            return prepareBasicModelAndView("simulations/submit", ofNullable(authenticationSession));
        }
        authenticationSession.setReturnUrl("/oxides/simulations/submit");
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

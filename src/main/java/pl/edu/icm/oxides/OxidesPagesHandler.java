package pl.edu.icm.oxides;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import pl.edu.icm.oxides.user.AuthenticationSession;

import java.util.Optional;

@Service
public class OxidesPagesHandler {
    public ModelAndView modelWelcomePage(Optional<AuthenticationSession> authenticationSession) {
        ModelAndView modelAndView = new ModelAndView("welcome");
        modelAndView.addObject("commonName",
                authenticationSession
                        .map(AuthenticationSession::getCommonName)
                        .orElse("")
        );
        return modelAndView;
    }
}

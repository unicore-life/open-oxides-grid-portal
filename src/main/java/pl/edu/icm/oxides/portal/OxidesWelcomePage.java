package pl.edu.icm.oxides.portal;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import pl.edu.icm.oxides.user.OxidesPortalGridSession;

import java.util.Optional;

@Service
class OxidesWelcomePage {
    ModelAndView modelWelcomePage(Optional<OxidesPortalGridSession> authenticationSession) {
        ModelAndView modelAndView = new ModelAndView("welcome");
        modelAndView.addObject("commonName",
                authenticationSession
                        .map(OxidesPortalGridSession::getCommonName)
                        .orElse("")
        );
        return modelAndView;
    }
}

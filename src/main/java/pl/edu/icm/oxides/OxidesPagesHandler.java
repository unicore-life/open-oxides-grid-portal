package pl.edu.icm.oxides;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import pl.edu.icm.oxides.user.AuthenticationSession;

import javax.servlet.http.HttpSession;
import java.util.Optional;

@Service
public class OxidesPagesHandler {
    public ModelAndView modelWelcomePage(Optional<AuthenticationSession> authenticationSession) {
        ModelAndView modelAndView = new ModelAndView("welcome");
        modelAndView.addObject("userName",
                authenticationSession.map(AuthenticationSession::getName).orElse("")
        );
        return modelAndView;
    }

    public String signOut(HttpSession session) {
        log.info(String.format("Invalidating session: %s", session.getId()));
        session.invalidate();
        return "redirect:/oxides";
    }

    private Log log = LogFactory.getLog(OxidesPagesHandler.class);
}

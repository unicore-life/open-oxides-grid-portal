package pl.edu.icm.oxides;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import pl.edu.icm.oxides.authn.AuthenticationSession;

import javax.servlet.http.HttpSession;

@Service
public class OxidesPagesHandler {
    public ModelAndView modelWelcomePage(AuthenticationSession authenticationSession) {
        String userName = "";
        if (authenticationSession != null
                && authenticationSession.getTrustDelegations() != null
                && authenticationSession.getTrustDelegations().size() > 0) {
            userName = authenticationSession.getTrustDelegations().get(0).getCustodianDN();
        }

        ModelAndView modelAndView = new ModelAndView("welcome");
        modelAndView.addObject("userName", userName);
        return modelAndView;
    }

    public String signOut(HttpSession session) {
        log.info(String.format("Invalidating session: %s", session.getId()));
        session.invalidate();
        return "redirect:/oxides/welcome";
    }

    private Log log = LogFactory.getLog(OxidesPagesHandler.class);
}

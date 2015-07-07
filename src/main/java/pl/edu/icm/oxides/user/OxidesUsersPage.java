package pl.edu.icm.oxides.user;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OxidesUsersPage {
    public ModelAndView modelPreferencesPage(Optional<AuthenticationSession> authenticationSession) {
        ModelAndView modelAndView = new ModelAndView("preferences");
        modelAndView.addObject("commonName",
                authenticationSession
                        .map(AuthenticationSession::getAttributes)
                        .map(userAttributes -> userAttributes.getCommonName())
                        .orElse("")
        );
        modelAndView.addObject("emailAddress",
                authenticationSession
                        .map(AuthenticationSession::getAttributes)
                        .map(userAttributes -> userAttributes.getEmailAddress())
                        .orElse("")
        );
        modelAndView.addObject("custodianDN",
                authenticationSession
                        .map(AuthenticationSession::getAttributes)
                        .map(userAttributes -> userAttributes.getCustodianDN())
                        .orElse("")
        );
        modelAndView.addObject("memberGroups",
                authenticationSession
                        .map(AuthenticationSession::getAttributes)
                        .map(userAttributes -> userAttributes.getMemberGroups())
                        .map(memberGroups -> memberGroups.stream().collect(Collectors.toList()))
                        .orElse(Collections.emptyList())
        );
        return modelAndView;
    }

    public String signOutAndRedirect(HttpSession session) {
        log.info(String.format("Invalidating session: %s", session.getId()));
        session.invalidate();
        return "redirect:/oxides";
    }

    private Log log = LogFactory.getLog(OxidesUsersPage.class);
}

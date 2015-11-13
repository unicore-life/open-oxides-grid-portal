package pl.edu.icm.oxides.user;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import pl.edu.icm.oxides.portal.security.OxidesForbiddenException;
import pl.edu.icm.oxides.portal.security.PortalAccessHelper;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static pl.edu.icm.oxides.portal.security.PortalAccess.VALID;

@Service
public class OxidesUserPage {
    private final PortalAccessHelper accessHelper;

    @Autowired
    public OxidesUserPage(PortalAccessHelper accessHelper) {
        this.accessHelper = accessHelper;
    }

    public ModelAndView modelPreferencesPage(Optional<AuthenticationSession> authenticationSession) {
        checkIfAccessIsValid(authenticationSession);

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
                        .map(UserAttributes::getMemberGroups)
                        .map(memberGroups -> (List) new ArrayList<>(memberGroups))
                        .orElse(Collections.emptyList())
        );
        return modelAndView;
    }

    private void checkIfAccessIsValid(Optional<AuthenticationSession> authenticationSession) {
        Boolean accessNotValid = authenticationSession
                .map(accessHelper::determineSessionAccess)
                .map(portalAccess -> portalAccess != VALID)
                .orElse(true);

        if (accessNotValid) {
            String commonName = authenticationSession
                    .map(AuthenticationSession::getAttributes)
                    .map(UserAttributes::getCommonName)
                    .orElse("(anonymous)");
            log.info("Preferences page is not allowed for user: " + commonName);
            throw new OxidesForbiddenException("You are not allowed or activated!");
        }
    }

    public String signOutAndRedirect(HttpSession session) {
        log.info(String.format("Invalidating session: %s", session.getId()));
        session.invalidate();
        return "redirect:/";
    }

    private Log log = LogFactory.getLog(OxidesUserPage.class);
}

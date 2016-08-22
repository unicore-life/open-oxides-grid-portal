package pl.edu.icm.oxides.authn;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import pl.edu.icm.oxides.user.AuthenticationSession;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SingleLogoutHandler implements LogoutHandler {
    @Autowired
    private AuthenticationSession authenticationSession;
    @Autowired
    private SamlSingleLogoutHandler singleLogoutHandler;
    @Autowired
    private TestingLogoutHandler testingLogoutHandler;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        log.info("Invalidating session: " + request.getSession().getId());
        log.debug("Disposing authenticated session: " + authenticationSession);

//        testingLogoutHandler.perform2(response, authenticationSession);

        if (!singleLogoutHandler.performSingleLogoutRequest(response, authenticationSession)) {
            final String redirectPath = "/";
            try {
                response.sendRedirect(redirectPath);
            } catch (java.io.IOException e) {
                log.error("Could not redirect to path: " + redirectPath);
            }
        }
    }

    private Log log = LogFactory.getLog(SingleLogoutHandler.class);
}

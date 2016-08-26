package pl.edu.icm.oxides.authn;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import pl.edu.icm.oxides.user.OxidesPortalGridSession;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SingleLogoutHandler implements LogoutHandler {
    @Autowired
    private OxidesPortalGridSession oxidesPortalGridSession;
    @Autowired
    private SamlSingleLogoutHandler singleLogoutHandler;
    @Autowired
    private TestingLogoutHandler testingLogoutHandler;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        log.info("Invalidating session: " + request.getSession().getId());
        log.debug("Disposing authenticated grid session: " + oxidesPortalGridSession);

//        testingLogoutHandler.perform2(response, oxidesPortalGridSession);

        if (!singleLogoutHandler.performSingleLogoutRequest(response, oxidesPortalGridSession)) {
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

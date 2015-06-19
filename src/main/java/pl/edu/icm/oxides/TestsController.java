package pl.edu.icm.oxides;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import pl.edu.icm.oxides.authn.OxidesAuthenticationSession;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Controller
@SessionAttributes("authenticationSession")
public class TestsController {
    private OxidesAuthenticationSession authenticationSession;

    @Autowired
    public TestsController(OxidesAuthenticationSession authenticationSession) {
        this.authenticationSession = authenticationSession;
    }

    @RequestMapping(value = "/redirect")
    public void testRedirect(HttpServletResponse response, HttpSession session) throws IOException {
        log.info("TEST-0: " + session.getId());
        log.info("TEST-0: " + authenticationSession);
        response.sendRedirect("/redirected");
    }

    @RequestMapping(value = "/redirected", method = RequestMethod.GET)
    @ResponseBody
    public String testGetRedirected(HttpSession session) {
        log.info("TEST-G: " + session.getId());
        log.info("TEST-G: " + authenticationSession);
        return "TEST-G";
    }

    @RequestMapping(value = "/redirected", method = RequestMethod.POST)
    @ResponseBody
    public String testPostRedirected(HttpSession session) {
        log.info("TEST-P: " + session.getId());
        log.info("TEST-P: " + authenticationSession);
        return "TEST-P";
    }

    private Log log = LogFactory.getLog(TestsController.class);
}

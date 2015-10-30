package pl.edu.icm.oxides.portal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import pl.edu.icm.oxides.user.AuthenticationSession;
import pl.edu.icm.oxides.user.OxidesUserPage;

import javax.servlet.http.HttpSession;
import java.util.UUID;

import static java.util.Optional.ofNullable;

@Service
public class OxidesGridPortalPages {
    private final OxidesWelcomePage oxidesWelcomePage;
    private final OxidesSimulationsPage oxidesSimulationsPage;
    private final OxidesUserPage oxidesUserPage;

    @Autowired
    public OxidesGridPortalPages(OxidesWelcomePage oxidesWelcomePage,
                                 OxidesSimulationsPage oxidesSimulationsPage,
                                 OxidesUserPage oxidesUserPage) {
        this.oxidesWelcomePage = oxidesWelcomePage;
        this.oxidesSimulationsPage = oxidesSimulationsPage;
        this.oxidesUserPage = oxidesUserPage;
    }

    public ModelAndView modelWelcomePage(AuthenticationSession authenticationSession) {
        return oxidesWelcomePage.modelWelcomePage(ofNullable(authenticationSession));
    }

    public ModelAndView modelSimulationsPage(AuthenticationSession authenticationSession) {
        return oxidesSimulationsPage.modelSimulationsPage(authenticationSession);
    }

    public ModelAndView modelOneSimulationPage(AuthenticationSession authenticationSession, UUID uuid, String path) {
        return oxidesSimulationsPage.modelOneSimulationPage(authenticationSession, uuid, ofNullable(path));
    }

    public ModelAndView modelSimulationDetailsPage(AuthenticationSession authenticationSession, UUID uuid) {
        return oxidesSimulationsPage.modelSimulationDetailsPage(authenticationSession, uuid);
    }

    public ModelAndView modelJsmolViewerPage(AuthenticationSession authenticationSession, UUID uuid, String path) {
        return oxidesSimulationsPage.modelJsmolViewerPage(authenticationSession, uuid, ofNullable(path));
    }

    public ModelAndView modelSubmitSimulationPage(AuthenticationSession authenticationSession) {
        return oxidesSimulationsPage.modelSubmitSimulationPage(authenticationSession);
    }

    public ModelAndView modelSubmitQuantumEspressoSimulationPage(AuthenticationSession authenticationSession) {
        return oxidesSimulationsPage.modelSubmitQuantumEspressoSimulationPage(authenticationSession);
    }

    public ModelAndView modelPreferencesPage(AuthenticationSession authenticationSession) {
        return oxidesUserPage.modelPreferencesPage(ofNullable(authenticationSession));
    }

    public String signOutAndRedirect(HttpSession session) {
        return oxidesUserPage.signOutAndRedirect(session);
    }
}

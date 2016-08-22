package pl.edu.icm.oxides.portal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import pl.edu.icm.oxides.user.OxidesPortalGridSession;
import pl.edu.icm.oxides.user.OxidesUserPage;

import javax.servlet.http.HttpSession;
import java.util.UUID;

import static java.util.Optional.ofNullable;

@Service
public class OxidesGridPortalPages {
    private final OxidesWelcomePage oxidesWelcomePage;
    private final OxidesSimulationsPage oxidesSimulationsPage;
    private final OxidesUserPage oxidesUserPage;
    private final OxidesErrorsPage oxidesErrorsPage;

    @Autowired
    public OxidesGridPortalPages(OxidesWelcomePage oxidesWelcomePage,
                                 OxidesSimulationsPage oxidesSimulationsPage,
                                 OxidesUserPage oxidesUserPage,
                                 OxidesErrorsPage oxidesErrorsPage) {
        this.oxidesWelcomePage = oxidesWelcomePage;
        this.oxidesSimulationsPage = oxidesSimulationsPage;
        this.oxidesUserPage = oxidesUserPage;
        this.oxidesErrorsPage = oxidesErrorsPage;
    }

    public ModelAndView modelWelcomePage(OxidesPortalGridSession oxidesPortalGridSession) {
        return oxidesWelcomePage.modelWelcomePage(ofNullable(oxidesPortalGridSession));
    }

    public ModelAndView modelSimulationsPage(OxidesPortalGridSession oxidesPortalGridSession) {
        return oxidesSimulationsPage.modelSimulationsPage(oxidesPortalGridSession);
    }

    public ModelAndView modelOneSimulationPage(OxidesPortalGridSession oxidesPortalGridSession, UUID uuid, String path) {
        return oxidesSimulationsPage.modelOneSimulationViewerPage(oxidesPortalGridSession,
                uuid, ofNullable(path), "simulations/one");
    }

    public ModelAndView modelSimulationDetailsPage(OxidesPortalGridSession oxidesPortalGridSession, UUID uuid) {
        return oxidesSimulationsPage.modelSimulationDetailsPage(oxidesPortalGridSession, uuid);
    }

    public ModelAndView modelJsmolViewerPage(OxidesPortalGridSession oxidesPortalGridSession, UUID uuid, String path) {
        return oxidesSimulationsPage.modelOneSimulationViewerPage(oxidesPortalGridSession,
                uuid, ofNullable(path), "viewers/jsmol");
    }

    public ModelAndView modelSubmitScriptSimulationPage(OxidesPortalGridSession oxidesPortalGridSession) {
        return oxidesSimulationsPage.modelSubmitScriptSimulationPage(oxidesPortalGridSession);
    }

    public ModelAndView modelSubmitQuantumEspressoSimulationPage(OxidesPortalGridSession oxidesPortalGridSession) {
        return oxidesSimulationsPage.modelSubmitQuantumEspressoSimulationPage(oxidesPortalGridSession);
    }

    public ModelAndView modelPreferencesPage(OxidesPortalGridSession oxidesPortalGridSession) {
        return oxidesUserPage.modelPreferencesPage(ofNullable(oxidesPortalGridSession));
    }

    public ModelAndView modelForbiddenErrorPage(HttpSession session) {
        return oxidesErrorsPage.modelForbiddenPage(session);
    }

    public ModelAndView modelNoTrustDelegationErrorPage(HttpSession session) {
        return oxidesErrorsPage.modelNoTrustDelegationPage(session);
    }
}

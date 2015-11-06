package pl.edu.icm.oxides.portal;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;

@Service
class OxidesErrorsPage {

    public ModelAndView modelForbiddenPage(HttpSession session) {
        session.invalidate();
        return prepareBasicModelAndView("errors/forbidden");
    }

    public ModelAndView modelNoTrustDelegationPage(HttpSession session) {
        session.invalidate();
        return prepareBasicModelAndView("errors/no-etd");
    }

    public ModelAndView modelDefaultPage(HttpSession session) {
        session.invalidate();
        return prepareBasicModelAndView("errors/default");
    }

    private ModelAndView prepareBasicModelAndView(String htmlTemplateName) {
        ModelAndView modelAndView = new ModelAndView(htmlTemplateName);
        modelAndView.addObject("commonName", "");
        return modelAndView;
    }
}

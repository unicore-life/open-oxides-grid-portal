package pl.edu.icm.oxides.authn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.edu.icm.oxides.user.OxidesPortalGridSession;
import pl.edu.icm.unicore.spring.security.GridIdentityProvider;
import pl.edu.icm.unity.spring.authn.SamlResponseData;
import pl.edu.icm.unity.spring.saml.SamlAuthenticationHandler;
import pl.edu.icm.unity.spring.slo.UserUnityAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
public class UnityAuthenticationHandler {
    private final SamlAuthenticationHandler samlAuthenticationHandler;
    private final GridIdentityProvider gridIdentityProvider;

    @Autowired
    public UnityAuthenticationHandler(SamlAuthenticationHandler samlAuthenticationHandler,
                                      GridIdentityProvider gridIdentityProvider) {
        this.samlAuthenticationHandler = samlAuthenticationHandler;
        this.gridIdentityProvider = gridIdentityProvider;
    }

    public void performAuthenticationRequest(HttpServletResponse response,
                                             OxidesPortalGridSession oxidesPortalGridSession) {
        samlAuthenticationHandler.performAuthenticationRequest(
                response,
                oxidesPortalGridSession.getUuid(),
                gridIdentityProvider.getGridCredential()
        );
    }

    public String processAuthenticationResponse(HttpServletRequest request,
                                                OxidesPortalGridSession oxidesPortalGridSession) {
        final SamlResponseData responseData = samlAuthenticationHandler.processAuthenticationResponse(
                request,
                oxidesPortalGridSession.getUuid(),
                gridIdentityProvider.getGridCredential(),
                gridIdentityProvider.getIdpValidator()
        );

        oxidesPortalGridSession.setTrustDelegations(responseData.getTrustDelegations());
        oxidesPortalGridSession.setSessionIndex(responseData.getSessionIndex());

        final UserUnityAttributes unityAttributes = responseData.getUserUnityAttributes();

        oxidesPortalGridSession.storeAttribute("cn", unityAttributes.getCommonName());
        oxidesPortalGridSession.storeAttribute("TrustDelegationOfUser", unityAttributes.getCustodianDN());
        oxidesPortalGridSession.storeAttribute("email", unityAttributes.getEmailAddress());

        unityAttributes.getMemberGroups().forEach(entry ->
                oxidesPortalGridSession.storeAttribute("memberOf", entry)
        );
        unityAttributes.getOthers().forEach((k, v) -> oxidesPortalGridSession.storeAttribute(k, v));

        return String.format("redirect:%s", oxidesPortalGridSession.getReturnUrl());
    }

    public String processSingleLogoutResponse(HttpServletRequest request) {
        samlAuthenticationHandler.processSingleLogoutResponse(request);
        return "redirect:/";
    }
}

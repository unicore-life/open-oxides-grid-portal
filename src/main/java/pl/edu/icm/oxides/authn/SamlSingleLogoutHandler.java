package pl.edu.icm.oxides.authn;

import eu.emi.security.authn.x509.X509Credential;
import eu.unicore.samly2.binding.HttpPostBindingSupport;
import eu.unicore.samly2.binding.SAMLMessageType;
import eu.unicore.samly2.exceptions.SAMLResponderException;
import eu.unicore.samly2.proto.LogoutRequest;
import eu.unicore.security.dsig.DSigException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.edu.icm.oxides.config.GridConfig;
import pl.edu.icm.oxides.user.OxidesPortalGridSession;
import pl.edu.icm.unicore.spring.security.GridIdentityProvider;
import xmlbeans.org.oasis.saml2.assertion.NameIDType;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import static pl.edu.icm.oxides.authn.Utils.DEFAULT_LOGOUT_REQUEST_VALIDITY_IN_MILLIS;
import static pl.edu.icm.oxides.authn.Utils.configureHttpResponse;
import static pl.edu.icm.oxides.authn.Utils.convertDistinguishedNameToNameID;

@Component
class SamlSingleLogoutHandler {
    private final GridConfig gridConfig;
    private final GridIdentityProvider idProvider;

    @Autowired
    SamlSingleLogoutHandler(GridConfig gridConfig, GridIdentityProvider idProvider) {
        this.gridConfig = gridConfig;
        this.idProvider = idProvider;
    }

    public boolean performSingleLogoutRequest(HttpServletResponse response,
                                              OxidesPortalGridSession oxidesPortalGridSession) {
        final String singleLogoutEndpointUrl = gridConfig.getIdpSloUrl();
        final String principalCommonName = oxidesPortalGridSession.getDistinguishedName();
        final String sessionIndex = oxidesPortalGridSession.getSessionIndex();
        final String localSamlId = idProvider.getGridCredential().getSubjectName();

        try {
            LogoutRequest logoutRequest =
                    createLogoutRequest(singleLogoutEndpointUrl, principalCommonName, sessionIndex, localSamlId);

            log.debug("Returning redirect with SLO request with HTTP POST binding to: " + singleLogoutEndpointUrl);
            configureHttpResponse(response);
            String htmlFormContent = HttpPostBindingSupport.getHtmlPOSTFormContents(
                    SAMLMessageType.SAMLRequest,
                    singleLogoutEndpointUrl,
                    logoutRequest.getXMLBeanDoc().xmlText(),
                    null);
            log.trace(String.format("Returning HTML content: %s", htmlFormContent));

            PrintWriter writer = response.getWriter();
            writer.write(htmlFormContent);
            writer.flush();

            return true;
        } catch (IOException | SAMLResponderException exception) {
            log.error("Could not responde with logout form!", exception);
        }
        return false;
    }

    LogoutRequest createLogoutRequest(String singleLogoutEndpoint,
                                      String principalCommonName,
                                      String authoritySessionIndex,
                                      String localSamlId) throws SAMLResponderException {
        final NameIDType toBeLoggedOutIdentity = convertDistinguishedNameToNameID(principalCommonName);
        final NameIDType portalGridIdentityIssuer = convertDistinguishedNameToNameID(localSamlId);

        LogoutRequest request = new LogoutRequest(portalGridIdentityIssuer, toBeLoggedOutIdentity);
        request.setNotAfter(new Date(System.currentTimeMillis() + DEFAULT_LOGOUT_REQUEST_VALIDITY_IN_MILLIS));
        request.setSessionIds(authoritySessionIndex);
        request.getXMLBean().setDestination(singleLogoutEndpoint);

        try {
            X509Credential gridCredential = idProvider.getGridCredential();
            request.sign(gridCredential.getKey(), gridCredential.getCertificateChain());
        } catch (DSigException e) {
            log.warn("Unable to sign SLO request", e);
            throw new SAMLResponderException("Internal server error signing request.");
        }
        return request;
    }

    private Log log = LogFactory.getLog(SamlSingleLogoutHandler.class);
}

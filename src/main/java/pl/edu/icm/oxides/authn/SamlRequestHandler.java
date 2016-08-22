package pl.edu.icm.oxides.authn;

import eu.emi.security.authn.x509.X509Credential;
import eu.unicore.samly2.SAMLConstants;
import eu.unicore.samly2.binding.HttpPostBindingSupport;
import eu.unicore.samly2.binding.SAMLMessageType;
import eu.unicore.samly2.proto.AuthnRequest;
import eu.unicore.security.dsig.DSigException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.edu.icm.oxides.config.GridConfig;
import pl.edu.icm.unicore.spring.security.GridIdentityProvider;
import xmlbeans.org.oasis.saml2.assertion.NameIDType;
import xmlbeans.org.oasis.saml2.protocol.AuthnRequestDocument;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;

import static pl.edu.icm.oxides.authn.Utils.configureHttpResponse;
import static pl.edu.icm.oxides.authn.Utils.convertDistinguishedNameToNameID;

@Component
class SamlRequestHandler {
    private final GridConfig gridConfig;
    private final GridIdentityProvider idProvider;

    @Autowired
    SamlRequestHandler(GridConfig gridConfig, GridIdentityProvider idProvider) {
        this.gridConfig = gridConfig;
        this.idProvider = idProvider;
    }

    void performAuthenticationRequest(HttpServletResponse response, String authenticationRequestId) {
        String idpUrl = gridConfig.getIdpUrl();
        String targetUrl = gridConfig.getTargetUrl();
        try {
            AuthnRequest authnRequest = createRequest(idpUrl, targetUrl,
                    idProvider.getGridCredential(), authenticationRequestId);
            AuthnRequestDocument authnRequestDocument = AuthnRequestDocument.Factory.parse(
                    authnRequest.getXMLBeanDoc().xmlText());

            configureHttpResponse(response);
            String htmlFormContent = HttpPostBindingSupport.getHtmlPOSTFormContents(
                    SAMLMessageType.SAMLRequest,
                    idpUrl,
                    authnRequestDocument.xmlText(),
                    null);
            PrintWriter writer = response.getWriter();
            writer.write(htmlFormContent);
            writer.flush();
        } catch (URISyntaxException e) {
            log.error("Wrong service provider target URL.", e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private AuthnRequest createRequest(String identityProviderUrl,
                                       String serviceProviderTargetUrl,
                                       X509Credential x509Credential,
                                       String requestId) throws DSigException, URISyntaxException {
        final URI samlServletUri = new URI(serviceProviderTargetUrl);
        final NameIDType requestIssuer = convertDistinguishedNameToNameID(x509Credential.getSubjectName());

        AuthnRequest request = new AuthnRequest(requestIssuer);
        request.setFormat(SAMLConstants.NFORMAT_DN);
        request.getXMLBean().setDestination(identityProviderUrl);
        request.getXMLBean().setAssertionConsumerServiceURL(samlServletUri.toASCIIString());
        request.getXMLBean().setID(requestId);

        request.sign(x509Credential.getKey(), x509Credential.getCertificateChain());
        return request;
    }

    private Log log = LogFactory.getLog(SamlRequestHandler.class);
}

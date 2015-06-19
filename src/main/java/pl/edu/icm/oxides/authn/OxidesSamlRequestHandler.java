package pl.edu.icm.oxides.authn;

import eu.emi.security.authn.x509.X509Credential;
import eu.unicore.samly2.SAMLConstants;
import eu.unicore.samly2.binding.HttpPostBindingSupport;
import eu.unicore.samly2.binding.SAMLMessageType;
import eu.unicore.samly2.elements.NameID;
import eu.unicore.samly2.proto.AuthnRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import pl.edu.icm.oxides.config.GridConfig;
import pl.edu.icm.oxides.config.GridIdProvider;
import xmlbeans.org.oasis.saml2.protocol.AuthnRequestDocument;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;

@Component
public class OxidesSamlRequestHandler {
    private final GridConfig gridConfig;
    private final GridIdProvider idProvider;

    @Autowired
    public OxidesSamlRequestHandler(GridConfig gridConfig, GridIdProvider idProvider) {
        this.gridConfig = gridConfig;
        this.idProvider = idProvider;
    }

    public void performAuthenticationRequest(HttpServletResponse response, OxidesAuthenticationSession authnSession) {
        String idpUrl = gridConfig.getIdpUrl();
        String targetUrl = gridConfig.getTargetUrl();
        try {
            AuthnRequest authnRequest = createRequest(idpUrl, targetUrl,
                    idProvider.getGridCredential(), authnSession.getUuid());
            AuthnRequestDocument authnRequestDocument = AuthnRequestDocument.Factory.parse(
                    authnRequest.getXMLBeanDoc().xmlText());

            configureHttpResponse(response);
            String form = HttpPostBindingSupport.getHtmlPOSTFormContents(
                    SAMLMessageType.SAMLRequest,
                    idpUrl,
                    authnRequestDocument.xmlText(),
                    null);
            PrintWriter writer = response.getWriter();
            writer.write(form);
            writer.flush();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void configureHttpResponse(HttpServletResponse response) {
        response.setContentType(String.format("%s; charset=%s", MediaType.TEXT_HTML, StandardCharsets.UTF_8));
//        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache,no-store,must-revalidate");
//        response.setHeader(HttpHeaders.PRAGMA, "no-cache");
//        response.setDateHeader(HttpHeaders.EXPIRES, -1);
    }

    private AuthnRequest createRequest(String idpUrl, String targetUrl,
                                       X509Credential credential, String requestId) throws Exception {
        URI samlServletUri = new URI(targetUrl);
        NameID myId = new NameID(credential.getSubjectName(), SAMLConstants.NFORMAT_DN);

        AuthnRequest request = new AuthnRequest(myId.getXBean());
        request.setFormat(SAMLConstants.NFORMAT_DN);
        request.getXMLBean().setDestination(idpUrl);
        request.getXMLBean().setAssertionConsumerServiceURL(samlServletUri.toASCIIString());
        request.getXMLBean().setID(requestId);

        request.sign(credential.getKey(), credential.getCertificateChain());
        return request;
    }

    private Log log = LogFactory.getLog(OxidesSamlRequestHandler.class);
}

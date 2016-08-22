package pl.edu.icm.oxides.authn;

import eu.unicore.samly2.binding.SAMLMessageType;
import eu.unicore.samly2.exceptions.SAMLResponderException;
import eu.unicore.samly2.proto.LogoutRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.TrustStrategy;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.impl.util.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import pl.edu.icm.oxides.user.AuthenticationSession;
import pl.edu.icm.unicore.spring.security.GridIdentityProvider;
import xmlbeans.org.oasis.saml2.assertion.NameIDType;
import xmlbeans.org.oasis.saml2.protocol.LogoutRequestDocument;
import xmlbeans.org.oasis.saml2.protocol.LogoutRequestType;

import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletResponse;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

@Component
class TestingLogoutHandler {
    private final GridIdentityProvider idProvider;
    private final SamlSingleLogoutHandler singleLogoutHandler;

    @Autowired
    TestingLogoutHandler(GridIdentityProvider idProvider, SamlSingleLogoutHandler singleLogoutHandler) {
        this.idProvider = idProvider;
        this.singleLogoutHandler = singleLogoutHandler;
    }

    public void perform2(HttpServletResponse response, AuthenticationSession authenticationSession) {
        final String logoutEndpoint = "https://unity.grid.icm.edu.pl/unicore-portal/SLO-SOAP/SingleLogoutService";
        final String principalName = "CN=plgkluszczynski,CN=Rafal Kluszczynski,O=ICM,O=Uzytkownik,O=PL-Grid,C=PL";
        final String sessionIndex = authenticationSession.getSessionIndex();
        final String localSamlId = idProvider.getGridCredential().getSubjectName();

        LogoutRequest logoutRequest = null;
        try {
            logoutRequest = singleLogoutHandler.createLogoutRequest(logoutEndpoint, principalName, sessionIndex, localSamlId);
        } catch (SAMLResponderException e) {
            log.error("ERROR", e);
        }

        LogoutRequestDocument logoutRequestDocument = LogoutRequestDocument.Factory.newInstance();
        final LogoutRequestType logoutRequestType = logoutRequestDocument.addNewLogoutRequest();
        final NameIDType nameIDType = logoutRequestType.addNewNameID();

        TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

        SSLContext sslContext = null;
        try {
            sslContext = org.apache.http.ssl.SSLContexts.custom()
                    .loadTrustMaterial(idProvider.getGridCredential().getKeyStore(), acceptingTrustStrategy)
                    .build();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }

        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);
        CloseableHttpClient httpClient2 = HttpClients.custom()
                .setSSLSocketFactory(csf)
                .build();

        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setHttpClient(httpClient2);

        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_XML);

//        final String requestBody = new String(Base64.encode(logoutRequest.getXMLBeanDoc().xmlText().getBytes()));
        final String requestBody = logoutRequest.getXMLBeanDoc().xmlText();
        log.warn("SLO.REQ: " + requestBody);

        try {
            LogoutRequestDocument.Factory.parse(requestBody);
            log.info("COULD PARSE IT!");
        } catch (XmlException e) {
            log.error("COULD NOT PARSE!", e);
        }

        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, requestHeaders);
        log.debug("REQ.ENTITY: " + requestEntity.toString());

        final ResponseEntity<String> responseEntity =
                restTemplate.exchange(logoutEndpoint, HttpMethod.POST, requestEntity, String.class);
        log.info("RESP.CODE: " + responseEntity.getStatusCode().name());
        log.info("RESP.BODY: " + responseEntity.getBody());
    }

    public void perform(HttpServletResponse response, AuthenticationSession authenticationSession) {
        final String logoutEndpoint = "https://unity.grid.icm.edu.pl/unicore-portal/SLO-WEB";
        final String principalName = "CN=plgkluszczynski,CN=Rafal Kluszczynski,O=ICM,O=Uzytkownik,O=PL-Grid,C=PL";
        final String sessionIndex = authenticationSession.getSessionIndex();
        final String localSamlId = idProvider.getGridCredential().getSubjectName();

        LogoutRequest logoutRequest = null;
        try {
            logoutRequest = singleLogoutHandler.createLogoutRequest(logoutEndpoint, principalName, sessionIndex, localSamlId);
        } catch (SAMLResponderException e) {
            log.error("ERROR", e);
        }

        CloseableHttpClient httpClient1 =
                HttpClients.custom()
                        .setSSLHostnameVerifier(new NoopHostnameVerifier())
                        .build();

        TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

        SSLContext sslContext = null;
        try {
            sslContext = org.apache.http.ssl.SSLContexts.custom()
                    .loadTrustMaterial(idProvider.getGridCredential().getKeyStore(), acceptingTrustStrategy)
                    .build();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }

        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);

        CloseableHttpClient httpClient2 = HttpClients.custom()
                .setSSLSocketFactory(csf)
                .build();

        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setHttpClient(httpClient2);

        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);

        HttpHeaders requestHeaders = new HttpHeaders();
//        requestHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        requestHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        final LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("RelayState", newArrayList(""));
        params.put(SAMLMessageType.SAMLRequest.toString(),
                newArrayList(new String(Base64.encode(logoutRequest.getXMLBeanDoc().xmlText().getBytes()))));

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, requestHeaders);
        log.debug("REQ.ENTITY: " + requestEntity.toString());

        final ResponseEntity<String> responseEntity =
                restTemplate.exchange(logoutEndpoint, HttpMethod.POST, requestEntity, String.class);
        log.info("RESP.CODE: " + responseEntity.getStatusCode().name());
        log.info("RESP.BODY: " + responseEntity.getBody());
    }

    private List<String> newArrayList(String... elements) {
        return Arrays.asList(elements);
    }

    private Log log = LogFactory.getLog(TestingLogoutHandler.class);
}

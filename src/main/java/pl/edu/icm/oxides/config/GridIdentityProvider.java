package pl.edu.icm.oxides.config;

import eu.emi.security.authn.x509.X509CertChainValidatorExt;
import eu.emi.security.authn.x509.X509Credential;
import eu.unicore.security.canl.TrustedIssuersProperties;
import eu.unicore.util.httpclient.ClientProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Properties;

@Component
public class GridIdentityProvider {
    private final X509Credential gridCredential;
    private final X509CertChainValidatorExt gridValidator;
    private final X509CertChainValidatorExt idpValidator;

    public GridIdentityProvider() throws IOException {
        Resource resource = new ClassPathResource("grid.properties");
        Properties gridIdentityProperties = PropertiesLoaderUtils.loadProperties(resource);

        ClientProperties clientProperties = new ClientProperties(gridIdentityProperties);
        gridCredential = clientProperties.getAuthnAndTrustConfiguration().getCredential();
        gridValidator = clientProperties.getAuthnAndTrustConfiguration().getValidator();

        TrustedIssuersProperties trustedIssuersProperties = new TrustedIssuersProperties(
                gridIdentityProperties, null, "idp.truststore."
        );
        idpValidator = trustedIssuersProperties.getValidator();
    }

    public X509Credential getGridCredential() {
        return gridCredential;
    }

    public X509CertChainValidatorExt getGridValidator() {
        return gridValidator;
    }

    public X509CertChainValidatorExt getIdpValidator() {
        return idpValidator;
    }
}

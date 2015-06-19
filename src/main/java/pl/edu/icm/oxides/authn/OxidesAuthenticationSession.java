package pl.edu.icm.oxides.authn;

import eu.unicore.security.etd.TrustDelegation;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS, value = "session")
@Lazy
public class OxidesAuthenticationSession {
    private String idpUrl;
    private String returnUrl;
    private List<TrustDelegation> trustDelegations;

    private final String uuid = UUID.randomUUID().toString();

    public String getIdpUrl() {
        return idpUrl;
    }

    public void setIdpUrl(String idpUrl) {
        this.idpUrl = idpUrl;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public List<TrustDelegation> getTrustDelegations() {
        return trustDelegations;
    }

    public void setTrustDelegations(List<TrustDelegation> trustDelegations) {
        this.trustDelegations = trustDelegations;
    }

    public String getUuid() {
        return uuid;
    }

    @Override
    public String toString() {
        return String.format("AuthenticationSession{idpUrl='%s', returnUrl='%s', trustDelegations=%s, uuid='%s'}",
                idpUrl, returnUrl, trustDelegations, uuid);
    }
}

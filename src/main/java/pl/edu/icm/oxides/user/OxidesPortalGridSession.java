package pl.edu.icm.oxides.user;

import eu.unicore.security.etd.TrustDelegation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS, value = "session")
@Lazy
public class OxidesPortalGridSession {
    private String returnUrl;
    private List<TrustDelegation> trustDelegations;
    private String sessionIndex;

    private final UserAttributes attributes = new UserAttributes();
    private final UserResources resources = new UserResources();
    private final String uuid = UUID.randomUUID().toString();

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

    public String getSessionIndex() {
        return sessionIndex;
    }

    public void setSessionIndex(String sessionIndex) {
        this.sessionIndex = sessionIndex;
    }

    public UserResources getResources() {
        return resources;
    }

    public String getUuid() {
        return uuid;
    }

    @Override
    public String toString() {
        return String.format("OxidesPortalGridSession{returnUrl='%s', trustDelegations=%s, sessionIndex='%s', " +
                "attributes=%s, uuid='%s'}", returnUrl, trustDelegations, sessionIndex, attributes, uuid);
    }

    public void storeAttribute(String key, String value) {
        attributes.store(key, value);
    }

    public String getAttribute(String key) {
        return attributes.getAttribute(key);
    }

    public UserAttributes getAttributes() {
        return attributes;
    }

    public String getCommonName() {
        return attributes.getCommonName();
    }

    public String getDistinguishedName() {
        return attributes.getCustodianDN();
    }

    public boolean isGroupMember(String groupName) {
        return attributes.getMemberGroups().contains(groupName);
    }

    public TrustDelegation getSelectedTrustDelegation() {
        TrustDelegation trustDelegation = trustDelegations.get(0);
        if (trustDelegations.size() > 1) {
            log.warn(String.format("Too many trust delegations. Using the one with custodian DN = <%s> and " +
                            "subject = <%s> issued by <%s>.", trustDelegation.getCustodianDN(),
                    trustDelegation.getSubjectName(), trustDelegation.getIssuerName()));
        }
        return trustDelegation;
    }

    private Log log = LogFactory.getLog(OxidesPortalGridSession.class);
}

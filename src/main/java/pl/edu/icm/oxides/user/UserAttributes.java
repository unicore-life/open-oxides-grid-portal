package pl.edu.icm.oxides.user;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

class UserAttributes {
    private String commonName;
    private String emailAddress;
    private String custodianDN;

    private final Set<String> memberGroups = new TreeSet<>();
    private final Map<String, String> others = new ConcurrentHashMap<>();

    UserAttributes() {
    }

    String getCommonName() {
        return commonName;
    }

    String getEmailAddress() {
        return emailAddress;
    }

    String getCustodianDN() {
        return custodianDN;
    }

    Set<String> getMemberGroups() {
        return memberGroups;
    }

    String getAttribute(String key) {
        return others.get(key);
    }

    @Override
    public String toString() {
        return String.format("UserAttributes{commonName='%s', emailAddress='%s', custodianDN='%s', memberGroups=%s}",
                commonName, emailAddress, custodianDN, memberGroups);
    }

    void store(String name, String value) {
        switch (name) {
            case "cn":
                commonName = value;
                break;
            case "email":
                emailAddress = value;
                break;
            case "TrustDelegationOfUser":
                custodianDN = value;
                break;
            case "memberOf":
                memberGroups.add(value);
                break;
            default:
                others.put(name, value);
        }
    }
}

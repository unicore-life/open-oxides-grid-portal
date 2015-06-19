package pl.edu.icm.oxides.unicore;

import eu.unicore.security.etd.TrustDelegation;
import eu.unicore.util.httpclient.DefaultClientConfiguration;
import eu.unicore.util.httpclient.ETDClientSettings;
import eu.unicore.util.httpclient.IClientConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.edu.icm.oxides.authn.AuthenticationSession;

import java.util.Arrays;
import java.util.List;

@Component
public class ClientConfigurationHelper {
    private final GridIdentityProvider idProvider;

    @Autowired
    public ClientConfigurationHelper(GridIdentityProvider idProvider) {
        this.idProvider = idProvider;
    }

    public IClientConfiguration createUserConfiguration(AuthenticationSession authenticationSession) {
        List<TrustDelegation> trustDelegations = authenticationSession.getTrustDelegations();
        TrustDelegation trustDelegation = trustDelegations.get(0);
        if (trustDelegations.size() > 1) {
            log.warn(String.format("Too many trust delegations. Using the one with custodian DN = <%s> and " +
                            "subject = <%s> issued by <%s>.", trustDelegation.getCustodianDN(),
                    trustDelegation.getSubjectName(), trustDelegation.getIssuerName()));
        }
        DefaultClientConfiguration clientConfiguration = new DefaultClientConfiguration(
                idProvider.getGridValidator(),
                idProvider.getGridCredential()
        );
        ETDClientSettings etdSettings = clientConfiguration.getETDSettings();
        etdSettings.setTrustDelegationTokens(Arrays.asList(trustDelegation));
        etdSettings.setRequestedUser(trustDelegation.getCustodianDN());
        etdSettings.setExtendTrustDelegation(true);
        return clientConfiguration;
    }

    private Log log = LogFactory.getLog(ClientConfigurationHelper.class);
}

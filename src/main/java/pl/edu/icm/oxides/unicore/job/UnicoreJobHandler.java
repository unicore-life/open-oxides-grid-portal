package pl.edu.icm.oxides.unicore.job;

import de.fzj.unicore.uas.TargetSystemFactory;
import de.fzj.unicore.uas.client.TSFClient;
import de.fzj.unicore.uas.client.TSSClient;
import de.fzj.unicore.wsrflite.xmlbeans.client.RegistryClient;
import eu.unicore.util.httpclient.IClientConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3.x2005.x08.addressing.EndpointReferenceType;
import pl.edu.icm.oxides.authn.OxidesAuthenticationSession;
import pl.edu.icm.oxides.config.GridConfig;
import pl.edu.icm.oxides.unicore.SecurityProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UnicoreJobHandler {
    private final GridConfig gridConfig;
    private final SecurityProvider securityProvider;

    @Autowired
    public UnicoreJobHandler(GridConfig gridConfig, SecurityProvider securityProvider) {
        this.gridConfig = gridConfig;
        this.securityProvider = securityProvider;
    }

    public List<UnicoreJobEntity> retrieveUserJobsList(OxidesAuthenticationSession authenticationSession) {
        IClientConfiguration userConfiguration = securityProvider.createUserConfiguration(authenticationSession);
        return collectUserJobList(userConfiguration);
    }

    private List<UnicoreJobEntity> collectUserJobList(IClientConfiguration userConfiguration) {
        String registryUrl = gridConfig.getRegistry();
        EndpointReferenceType registryEpr = EndpointReferenceType.Factory.newInstance();
        registryEpr.addNewAddress().setStringValue(registryUrl);

        List<UnicoreJobEntity> unicoreJobList = new ArrayList<>();
        try {
            RegistryClient registryClient = new RegistryClient(registryEpr, userConfiguration);
            for (EndpointReferenceType tsfEpr : registryClient.listAccessibleServices(TargetSystemFactory.TSF_PORT)) {
                TSFClient tsfClient = new TSFClient(tsfEpr, userConfiguration);
                for (EndpointReferenceType tssEpr : tsfClient.getAccessibleTargetSystems()) {
                    TSSClient tssClient = new TSSClient(tssEpr, userConfiguration);
                    unicoreJobList.addAll(tssClient.getJobs()
                            .parallelStream()
                            .map(tssStorageEpr -> new UnicoreJobEntity(tssStorageEpr.getAddress().getStringValue()))
                            .collect(Collectors.toList()));
                }
            }
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
        }
        return unicoreJobList;
    }

    private Log log = LogFactory.getLog(UnicoreJobHandler.class);
}

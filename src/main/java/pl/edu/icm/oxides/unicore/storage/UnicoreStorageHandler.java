package pl.edu.icm.oxides.unicore.storage;

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
public class UnicoreStorageHandler {
    private final GridConfig gridConfig;
    private final SecurityProvider securityProvider;

    @Autowired
    public UnicoreStorageHandler(GridConfig gridConfig, SecurityProvider securityProvider) {
        this.gridConfig = gridConfig;
        this.securityProvider = securityProvider;
    }

    public List<UnicoreStorageEntity> retrieveUserStorageList(OxidesAuthenticationSession authenticationSession) {
        IClientConfiguration userConfiguration = securityProvider.createUserConfiguration(authenticationSession);
        return collectUserStorageList(userConfiguration);
    }

    private List<UnicoreStorageEntity> collectUserStorageList(IClientConfiguration userConfiguration) {
        String registryUrl = gridConfig.getRegistry();
        EndpointReferenceType registryEpr = EndpointReferenceType.Factory.newInstance();
        registryEpr.addNewAddress().setStringValue(registryUrl);

        List<UnicoreStorageEntity> unicoreStorageList = new ArrayList<>();
        try {
            RegistryClient registryClient = new RegistryClient(registryEpr, userConfiguration);
            for (EndpointReferenceType tsfEpr : registryClient.listAccessibleServices(TargetSystemFactory.TSF_PORT)) {
                TSFClient tsfClient = new TSFClient(tsfEpr, userConfiguration);
                for (EndpointReferenceType tssEpr : tsfClient.getAccessibleTargetSystems()) {
                    TSSClient tssClient = new TSSClient(tssEpr, userConfiguration);
                    unicoreStorageList.addAll(tssClient.getStorages()
                            .stream()
                            .map(tssStorageEpr -> new UnicoreStorageEntity(tssStorageEpr.getAddress().getStringValue()))
                            .collect(Collectors.toList()));
                }
            }
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
        }
        return unicoreStorageList;
    }

    private Log log = LogFactory.getLog(UnicoreStorageHandler.class);
}

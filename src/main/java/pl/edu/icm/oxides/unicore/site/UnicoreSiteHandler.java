package pl.edu.icm.oxides.unicore.site;

import de.fzj.unicore.uas.TargetSystemFactory;
import de.fzj.unicore.wsrflite.xmlbeans.client.RegistryClient;
import eu.unicore.util.httpclient.IClientConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.w3.x2005.x08.addressing.EndpointReferenceType;
import pl.edu.icm.oxides.authn.OxidesAuthenticationSession;
import pl.edu.icm.oxides.config.GridConfig;
import pl.edu.icm.oxides.unicore.SecurityProvider;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class UnicoreSiteHandler {
    private final GridConfig gridConfig;
    private final SecurityProvider securityProvider;

    @Autowired
    public UnicoreSiteHandler(GridConfig gridConfig, SecurityProvider securityProvider) {
        this.gridConfig = gridConfig;
        this.securityProvider = securityProvider;
    }

    @Cacheable(value = "retrieveUnicoreUserSites", key = "#authenticationSession.uuid")
    public List<UnicoreSiteEntity> retrieveUserSiteList(OxidesAuthenticationSession authenticationSession) {
        IClientConfiguration userConfiguration = securityProvider.createUserConfiguration(authenticationSession);
        return collectUserSiteList(userConfiguration);
    }

    private List<UnicoreSiteEntity> collectUserSiteList(IClientConfiguration userConfiguration) {
        String registryUrl = gridConfig.getRegistry();
        EndpointReferenceType registryEpr = EndpointReferenceType.Factory.newInstance();
        registryEpr.addNewAddress().setStringValue(registryUrl);
        try {
            RegistryClient registryClient = new RegistryClient(registryEpr, userConfiguration);
            return registryClient.listAccessibleServices(TargetSystemFactory.TSF_PORT)
                    .parallelStream()
                    .map(endpointReferenceType -> new UnicoreSiteEntity(endpointReferenceType.getAddress().getStringValue()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
        }
        return Collections.emptyList();
    }

    private Log log = LogFactory.getLog(UnicoreSiteHandler.class);
}

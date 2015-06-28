package pl.edu.icm.oxides.unicore.central.global;

import de.fzj.unicore.uas.StorageManagement;
import de.fzj.unicore.wsrflite.xmlbeans.client.RegistryClient;
import eu.unicore.util.httpclient.IClientConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.w3.x2005.x08.addressing.EndpointReferenceType;
import pl.edu.icm.oxides.config.GridConfig;
import pl.edu.icm.oxides.unicore.GridClientHelper;
import pl.edu.icm.oxides.user.AuthenticationSession;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class UnicoreGlobalStorage {
    private final GridConfig gridConfig;
    private final GridClientHelper clientHelper;

    @Autowired
    public UnicoreGlobalStorage(GridConfig gridConfig, GridClientHelper clientHelper) {
        this.gridConfig = gridConfig;
        this.clientHelper = clientHelper;
    }

    @Cacheable(value = "unicoreSessionGlobalStorageList", key = "#authenticationSession.uuid")
    public List<UnicoreGlobalStorageEntity> retrieveServiceList(AuthenticationSession authenticationSession) {
        IClientConfiguration clientConfiguration = clientHelper.createClientConfiguration(authenticationSession);
        return collectGlobalStorageServiceList(clientConfiguration);
    }

    private List<UnicoreGlobalStorageEntity> collectGlobalStorageServiceList(IClientConfiguration clientConfiguration) {
        String registryUrl = gridConfig.getRegistry();
        EndpointReferenceType registryEpr = EndpointReferenceType.Factory.newInstance();
        registryEpr.addNewAddress().setStringValue(registryUrl);
        try {
            return new RegistryClient(registryEpr, clientConfiguration)
                    .listAccessibleServices(StorageManagement.SMS_PORT)
                    .parallelStream()
                    .map(endpointReferenceType -> new UnicoreGlobalStorageEntity(endpointReferenceType))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error(String.format("Error retrieving Global Storage from UNICORE Registry <%s>!", registryUrl), e);
            // TODO: should be used RuntimeException?
            throw new UnavailableGlobalStorageException(e);
        }
    }

    private Log log = LogFactory.getLog(UnicoreGlobalStorage.class);
}

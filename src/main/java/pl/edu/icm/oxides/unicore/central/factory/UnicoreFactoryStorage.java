package pl.edu.icm.oxides.unicore.central.factory;

import de.fzj.unicore.uas.StorageFactory;
import de.fzj.unicore.uas.client.StorageClient;
import de.fzj.unicore.uas.client.StorageFactoryClient;
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

import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class UnicoreFactoryStorage {
    private final GridConfig gridConfig;
    private final GridClientHelper clientHelper;

    @Autowired
    public UnicoreFactoryStorage(GridConfig gridConfig, GridClientHelper clientHelper) {
        this.gridConfig = gridConfig;
        this.clientHelper = clientHelper;
    }

    public StorageClient getStorageClient(AuthenticationSession authenticationSession) {
        IClientConfiguration clientConfiguration = clientHelper.createClientConfiguration(authenticationSession);
        return retrieveServiceList(authenticationSession)
                .stream()
                .findAny()
                .map(unicoreFactoryStorageEntity -> toStorageClient(unicoreFactoryStorageEntity, clientConfiguration))
                .orElseThrow(() -> new UnavailableFactoryStorageException("No Broker at All!"));
    }

    @Cacheable(value = "unicoreSessionFactoryStorageList", key = "#authenticationSession.uuid")
    public List<UnicoreFactoryStorageEntity> retrieveServiceList(AuthenticationSession authenticationSession) {
        IClientConfiguration clientConfiguration = clientHelper.createClientConfiguration(authenticationSession);
        return collectFactoryStorageServiceList(clientConfiguration);
    }

    private List<UnicoreFactoryStorageEntity> collectFactoryStorageServiceList(IClientConfiguration clientConfiguration) {
        String registryUrl = gridConfig.getRegistry();
        EndpointReferenceType registryEpr = EndpointReferenceType.Factory.newInstance();
        registryEpr.addNewAddress().setStringValue(registryUrl);
        try {
            return new RegistryClient(registryEpr, clientConfiguration)
                    .listAccessibleServices(StorageFactory.SMF_PORT)
                    .parallelStream()
                    .map(endpointReferenceType -> new UnicoreFactoryStorageEntity(endpointReferenceType))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error(String.format("Error retrieving Storage Factory from UNICORE Registry <%s>!", registryUrl), e);
            // TODO: should be used RuntimeException?
            throw new UnavailableFactoryStorageException(e);
        }
    }

    private StorageClient toStorageClient(UnicoreFactoryStorageEntity unicoreFactoryStorageEntity,
                                          IClientConfiguration clientConfiguration) {
        try {
            StorageFactoryClient storageFactoryClient = new StorageFactoryClient(
                    unicoreFactoryStorageEntity.getEpr(), clientConfiguration
            );
            return storageFactoryClient.createSMS(calculateStorageLifetime());
        } catch (Exception e) {
            log.error(String.format("Problem with storage factory client creation: <%s>",
                    unicoreFactoryStorageEntity.getEpr()), e);
            // TODO: should be used RuntimeException?
            return null;
        }
    }

    private Calendar calculateStorageLifetime() {
        Calendar storageLifetimeFixedCalendar = Calendar.getInstance();
        storageLifetimeFixedCalendar.add(Calendar.SECOND, STORAGE_LIFETIME_IN_SECONDS);
        return storageLifetimeFixedCalendar;
    }

    private Log log = LogFactory.getLog(UnicoreFactoryStorage.class);

    private static final int STORAGE_LIFETIME_IN_SECONDS = 24 * 60 * 60;
}

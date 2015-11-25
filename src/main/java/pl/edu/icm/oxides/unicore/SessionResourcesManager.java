package pl.edu.icm.oxides.unicore;

import de.fzj.unicore.uas.client.StorageClient;
import de.fzj.unicore.uas.client.StorageFactoryClient;
import eu.unicore.security.etd.TrustDelegation;
import eu.unicore.util.httpclient.IClientConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import pl.edu.icm.oxides.unicore.central.factory.UnavailableFactoryStorageException;
import pl.edu.icm.oxides.unicore.central.factory.UnicoreFactoryStorage;
import pl.edu.icm.oxides.unicore.central.factory.UnicoreFactoryStorageEntity;
import pl.edu.icm.oxides.user.AuthenticationSession;
import pl.edu.icm.oxides.user.UserResources;

import java.util.Calendar;

@Service
public class SessionResourcesManager {
    private final UnicoreFactoryStorage unicoreFactoryStorage;
    private final ThreadPoolTaskExecutor taskExecutor;
    private final GridClientHelper clientHelper;

    @Autowired
    public SessionResourcesManager(UnicoreFactoryStorage unicoreFactoryStorage,
                                   ThreadPoolTaskExecutor taskExecutor,
                                   GridClientHelper clientHelper) {
        this.unicoreFactoryStorage = unicoreFactoryStorage;
        this.taskExecutor = taskExecutor;
        this.clientHelper = clientHelper;
    }

    public void prepareStorageClient(AuthenticationSession authenticationSession) {
        TrustDelegation trustDelegation = authenticationSession.getSelectedTrustDelegation();
        UserResources resources = authenticationSession.getResources();

        taskExecutor.execute(() -> resources.setStorageClient(getStorageClient(trustDelegation)));
    }

    public StorageClient getStorageClient(TrustDelegation trustDelegation) {
        log.warn(String.format("Using the one with custodian DN = <%s> and " +
                        "subject = <%s> issued by <%s>.", trustDelegation.getCustodianDN(),
                trustDelegation.getSubjectName(), trustDelegation.getIssuerName()));
        return unicoreFactoryStorage.retrieveServiceList(trustDelegation)
                .stream()
                .findAny()
                .map(unicoreFactoryStorageEntity -> toStorageClient(unicoreFactoryStorageEntity, trustDelegation))
                .orElseThrow(() -> new UnavailableFactoryStorageException("No Broker at All!"));
    }

    private StorageClient toStorageClient(UnicoreFactoryStorageEntity unicoreFactoryStorageEntity,
                                          TrustDelegation trustDelegation) {
        IClientConfiguration clientConfiguration = clientHelper.createClientConfiguration(trustDelegation);
        try {
            StorageFactoryClient storageFactoryClient = new StorageFactoryClient(
                    unicoreFactoryStorageEntity.getEpr(), clientConfiguration
            );
            return storageFactoryClient.createSMS(null, FACTORY_STORAGE_NAME, calculateFactoryStorageLifetime());
        } catch (Exception e) {
            log.error(String.format("Problem with storage factory client creation: <%s>",
                    unicoreFactoryStorageEntity.getEpr()), e);
            return null;
        }
    }

    private Calendar calculateFactoryStorageLifetime() {
        Calendar storageLifetimeFixedCalendar = Calendar.getInstance();
        storageLifetimeFixedCalendar.add(Calendar.SECOND, STORAGE_LIFETIME_IN_SECONDS);
        return storageLifetimeFixedCalendar;
    }

    private Log log = LogFactory.getLog(SessionResourcesManager.class);

    private static final int STORAGE_LIFETIME_IN_SECONDS = 24 * 60 * 60;
    private static final String FACTORY_STORAGE_NAME = "Open Oxides";
}

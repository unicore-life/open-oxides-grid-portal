package pl.edu.icm.oxides.unicore.central;

import eu.unicore.security.etd.TrustDelegation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import pl.edu.icm.unicore.spring.central.factory.UnavailableFactoryStorageException;
import pl.edu.icm.unicore.spring.central.factory.UnicoreFactoryStorage;
import pl.edu.icm.unicore.spring.central.factory.UnicoreFactoryStorageEntity;

import java.util.List;

@Repository
public class UnicoreStorageFactory {
    private final UnicoreFactoryStorage unicoreFactoryStorage;

    @Autowired
    public UnicoreStorageFactory(UnicoreFactoryStorage unicoreFactoryStorage) {
        this.unicoreFactoryStorage = unicoreFactoryStorage;
    }

    @Cacheable(value = "unicoreSessionFactoryStorageList", key = "#trustDelegation.custodianDN")
    public List<UnicoreFactoryStorageEntity> retrieveServiceList(TrustDelegation trustDelegation) {
        try {
            return unicoreFactoryStorage.getFactoryStorageList(trustDelegation);
        } catch (UnavailableFactoryStorageException exception) {
            throw new UnicoreSpringException(exception);
        }
    }
}

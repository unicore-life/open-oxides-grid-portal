package pl.edu.icm.oxides.unicore.central;

import eu.unicore.security.etd.TrustDelegation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.w3.x2005.x08.addressing.EndpointReferenceType;
import pl.edu.icm.oxides.unicore.site.job.UnicoreJob;
import pl.edu.icm.oxides.unicore.site.job.UnicoreJobEntity;

@Component
@CacheConfig(cacheNames = "endpointReferenceTypeCache")
public class EndpointReferenceTypeCache {
    private final UnicoreJob unicoreJob;

    @Autowired
    public EndpointReferenceTypeCache(UnicoreJob unicoreJob) {
        this.unicoreJob = unicoreJob;
    }

    @Cacheable(key = "#uuid", unless = "#result != null")
    public EndpointReferenceType get(String uuid, TrustDelegation trustDelegation) {
        return unicoreJob.retrieveSiteResourceList(trustDelegation)
                .stream()
                .filter(jobEntity -> uuid.equalsIgnoreCase(jobEntity.getUuid()))
                .map(UnicoreJobEntity::getEpr)
                .findFirst()
                .orElse(null);
//        FIXME: instead of null, maybe an exception
    }

    @CachePut(key = "#uuid")
    public EndpointReferenceType update(String uuid, EndpointReferenceType epr) {
        return epr;
    }
}

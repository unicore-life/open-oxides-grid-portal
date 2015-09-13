package pl.edu.icm.oxides.unicore.site.job;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.w3.x2005.x08.addressing.EndpointReferenceType;

@Component
class UnicoreJobEprCache {

    @Cacheable(value = "endpointReferenceTypeCache", key = "#uuid", unless = "#result == null")
    public EndpointReferenceType get(String uuid) {
        return null;
    }

    @CachePut(value = "endpointReferenceTypeCache", key = "#uuid")
    public EndpointReferenceType put(String uuid, EndpointReferenceType epr) {
        log.trace("Put into cache " + epr.getAddress().getStringValue() + " with key = " + uuid);
        return epr;
    }

    private Log log = LogFactory.getLog(UnicoreJobEprCache.class);
}

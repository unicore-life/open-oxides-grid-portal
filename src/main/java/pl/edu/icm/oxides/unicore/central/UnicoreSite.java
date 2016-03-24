package pl.edu.icm.oxides.unicore.central;

import eu.unicore.security.etd.TrustDelegation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import pl.edu.icm.unicore.spring.central.site.UnavailableSiteException;
import pl.edu.icm.unicore.spring.central.site.UnicoreSiteEntity;
import pl.edu.icm.unicore.spring.central.site.UnicoreSites;

import java.util.List;

@Repository
public class UnicoreSite {
    private final UnicoreSites unicoreSites;

    @Autowired
    public UnicoreSite(UnicoreSites unicoreSites) {
        this.unicoreSites = unicoreSites;
    }

    @Cacheable(value = "unicoreSessionSiteList", key = "#trustDelegation.custodianDN")
    public List<UnicoreSiteEntity> retrieveServiceList(TrustDelegation trustDelegation) {
        try {
            return unicoreSites.getTargetSystemList(trustDelegation);
        } catch (UnavailableSiteException e) {
            throw new UnicoreSpringException(e);
        }
    }
}

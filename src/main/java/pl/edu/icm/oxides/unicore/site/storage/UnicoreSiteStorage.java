package pl.edu.icm.oxides.unicore.site.storage;

import de.fzj.unicore.uas.client.TSSClient;
import eu.unicore.security.etd.TrustDelegation;
import eu.unicore.util.httpclient.IClientConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.w3.x2005.x08.addressing.EndpointReferenceType;
import pl.edu.icm.oxides.unicore.GridClientHelper;
import pl.edu.icm.oxides.unicore.central.tss.UnicoreSite;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static pl.edu.icm.oxides.unicore.site.ProcessingHelper.toAccessibleTargetSystems;

@Component
public class UnicoreSiteStorage {
    private final GridClientHelper clientHelper;
    private final UnicoreSite unicoreSite;

    @Autowired
    public UnicoreSiteStorage(GridClientHelper clientHelper, UnicoreSite unicoreSite) {
        this.clientHelper = clientHelper;
        this.unicoreSite = unicoreSite;
    }

    @Cacheable(value = "unicoreSessionSiteStorageList", key = "#trustDelegation.custodianDN")
    public List<UnicoreSiteStorageEntity> retrieveSiteResourceList(TrustDelegation trustDelegation) {
        IClientConfiguration clientConfiguration = clientHelper.createClientConfiguration(trustDelegation);
        return unicoreSite.retrieveServiceList(trustDelegation)
                .parallelStream()
                .map(siteEntity -> toAccessibleTargetSystems(siteEntity, clientConfiguration))
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .map(targetSystemEpr -> toSiteStorageList(targetSystemEpr, clientConfiguration))
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .map(this::toUnicoreStorageEntity)
                .collect(Collectors.toList());
    }

    private List<EndpointReferenceType> toSiteStorageList(EndpointReferenceType targetSystemEpr,
                                                          IClientConfiguration clientConfiguration) {
        try {
            return new TSSClient(targetSystemEpr, clientConfiguration)
                    .getStorages();
        } catch (Exception e) {
            log.warn(String.format("Could not get storage list from target system <%s>!",
                    targetSystemEpr.getAddress().getStringValue()), e);
        }
        return null;
    }

    private UnicoreSiteStorageEntity toUnicoreStorageEntity(EndpointReferenceType epr) {
        return new UnicoreSiteStorageEntity(epr);
    }

    private Log log = LogFactory.getLog(UnicoreSiteStorage.class);
}

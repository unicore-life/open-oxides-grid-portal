package pl.edu.icm.oxides.unicore.site.job;

import de.fzj.unicore.uas.client.TSSClient;
import eu.unicore.security.etd.TrustDelegation;
import eu.unicore.util.httpclient.IClientConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
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
class UnicoreJobsListing {
    private final UnicoreSite unicoreSite;
    private final CacheManager cacheManager;
    private final GridClientHelper clientHelper;

    @Autowired
    UnicoreJobsListing(UnicoreSite unicoreSite, CacheManager cacheManager, GridClientHelper clientHelper) {
        this.unicoreSite = unicoreSite;
        this.cacheManager = cacheManager;
        this.clientHelper = clientHelper;
    }

    @Cacheable(value = "unicoreSessionJobList", key = "#trustDelegation.custodianDN")
    public List<EndpointReferenceType> retrieveSiteResourceList(TrustDelegation trustDelegation) {
        IClientConfiguration clientConfiguration = clientHelper.createClientConfiguration(trustDelegation);
        return unicoreSite.retrieveServiceList(trustDelegation)
                .parallelStream()
                .map(siteEntity -> toAccessibleTargetSystems(siteEntity, clientConfiguration))
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .map(targetSystemEpr -> toTargetSystemJobList(targetSystemEpr, clientConfiguration))
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    public void updateSiteResourcesList(TrustDelegation trustDelegation, List<EndpointReferenceType> eprs) {
        cacheManager
                .getCache("unicoreSessionJobList")
                .put(trustDelegation.getCustodianDN(), eprs);
    }

    private List<EndpointReferenceType> toTargetSystemJobList(EndpointReferenceType targetSystemEpr,
                                                              IClientConfiguration clientConfiguration) {
        try {
            return new TSSClient(targetSystemEpr, clientConfiguration)
                    .getJobs();
        } catch (Exception e) {
            log.warn(String.format("Could not get jobs from target system <%s>!",
                    targetSystemEpr.getAddress().getStringValue()), e);
        }
        return null;
    }

    private Log log = LogFactory.getLog(UnicoreJobsListing.class);
}

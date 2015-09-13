package pl.edu.icm.oxides.unicore.site.job;

import de.fzj.unicore.uas.client.TSFClient;
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
import pl.edu.icm.oxides.unicore.central.tss.UnicoreSiteEntity;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
class UnicoreJobsListing {
    private final UnicoreSite unicoreSite;
    private final GridClientHelper clientHelper;

    @Autowired
    UnicoreJobsListing(UnicoreSite unicoreSite, GridClientHelper clientHelper) {
        this.unicoreSite = unicoreSite;
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

    private List<EndpointReferenceType> toAccessibleTargetSystems(UnicoreSiteEntity unicoreSiteEntity,
                                                                  IClientConfiguration clientConfiguration) {
        try {
            return new TSFClient(unicoreSiteEntity.getEpr(), clientConfiguration)
                    .getAccessibleTargetSystems();
        } catch (Exception e) {
            log.warn(String.format("Could not get accessible target systems from site <%s>!",
                    unicoreSiteEntity.getUri()), e);
        }
        return null;
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

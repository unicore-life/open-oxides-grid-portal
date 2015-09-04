package pl.edu.icm.oxides.unicore.site.resource;

import de.fzj.unicore.uas.client.TSSClient;
import eu.unicore.security.etd.TrustDelegation;
import eu.unicore.util.httpclient.IClientConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.RangeValueType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.unigrids.x2006.x04.services.tss.TargetSystemPropertiesDocument.TargetSystemProperties;
import org.w3.x2005.x08.addressing.EndpointReferenceType;
import pl.edu.icm.oxides.unicore.GridClientHelper;
import pl.edu.icm.oxides.unicore.central.tss.UnicoreSite;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static pl.edu.icm.oxides.unicore.site.ProcessingHelper.toAccessibleTargetSystems;

@Component
public class UnicoreResource {
    private final GridClientHelper clientHelper;
    private final UnicoreSite unicoreSite;

    @Autowired
    public UnicoreResource(GridClientHelper clientHelper, UnicoreSite unicoreSite) {
        this.clientHelper = clientHelper;
        this.unicoreSite = unicoreSite;
    }

    @Cacheable(value = "unicoreSessionResourceList", key = "#trustDelegation.custodianDN")
    public List<UnicoreResourceEntity> retrieveSiteResourceList(TrustDelegation trustDelegation) {
        IClientConfiguration clientConfiguration = clientHelper.createClientConfiguration(trustDelegation);
        return unicoreSite.retrieveServiceList(trustDelegation)
                .parallelStream()
                .map(siteEntity -> toAccessibleTargetSystems(siteEntity, clientConfiguration))
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .map(targetSystemEpr -> toSiteAvailableResources(targetSystemEpr, clientConfiguration))
                .filter(Objects::nonNull)
                .flatMap(List::stream)
//                .map(availableResourceType -> new UnicoreResourceEntity(availableResourceType.getName()))
                .collect(Collectors.toList());
    }

    private List<UnicoreResourceEntity> toSiteAvailableResources(EndpointReferenceType targetSystemEpr,
                                                                 IClientConfiguration clientConfiguration) {
        try {
            TSSClient client = new TSSClient(targetSystemEpr, clientConfiguration);
            List<UnicoreResourceEntity> resourceEntities = client.getAvailableResources()
                    .stream()
                    .map(availableResourceType -> new UnicoreResourceEntity(availableResourceType.getName()))
                    .collect(Collectors.toList());
            resourceEntities.addAll(
                    retrieveStandardResources(client
                            .getResourcePropertiesDocument()
                            .getTargetSystemProperties())
            );
            return resourceEntities;
        } catch (Exception e) {
            log.warn(String.format("Could not get available resources from target system <%s>!",
                    targetSystemEpr.getAddress().getStringValue()), e);
            e.printStackTrace();
        }
        return null;
    }

    private List<UnicoreResourceEntity> retrieveStandardResources(TargetSystemProperties targetSystemProperties) {
        List<UnicoreResourceEntity> availableResources = new ArrayList<>();
        // NOTE: ignoring resources JSDLUtils.CPU_architecture, JSDLUtils.OS

        if (targetSystemProperties.isSetIndividualCPUCount()) {
            String name = String.format("JSDLUtils.CPU_per_node -- %s",
                    parseExactValue(targetSystemProperties.getIndividualCPUCount()));
            availableResources.add(new UnicoreResourceEntity(name));
        }
        if (targetSystemProperties.isSetIndividualCPUTime()) {
            String name = String.format("JSDLUtils.CPU_time -- %s",
                    parseExactValue(targetSystemProperties.getIndividualCPUTime()));
            availableResources.add(new UnicoreResourceEntity(name));
        }
        if (targetSystemProperties.isSetIndividualPhysicalMemory()) {
            String name = String.format("JSDLUtils.RAM -- %s",
                    parseExactValue(targetSystemProperties.getIndividualPhysicalMemory()));
            availableResources.add(new UnicoreResourceEntity(name));
        }
        if (targetSystemProperties.isSetTotalCPUCount()) {
            String name = String.format("JSDLUtils.total_CPUs -- %s",
                    parseExactValue(targetSystemProperties.getTotalCPUCount()));
            availableResources.add(new UnicoreResourceEntity(name));
        }
        if (targetSystemProperties.isSetTotalResourceCount()) {
            String name = String.format("JSDLUtils.total_nodes -- %s",
                    parseExactValue(targetSystemProperties.getTotalResourceCount()));
            availableResources.add(new UnicoreResourceEntity(name));
        }
        return availableResources;
    }

    private String parseExactValue(RangeValueType rangeValue) {
        if (rangeValue.getExactArray().length > 0) {
            return rangeValue.getExactArray()[0].getStringValue();
        }
        return "NONE";
    }

    private Log log = LogFactory.getLog(UnicoreResource.class);
}

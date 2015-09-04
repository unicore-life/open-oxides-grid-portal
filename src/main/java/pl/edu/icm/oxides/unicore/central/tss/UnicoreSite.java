package pl.edu.icm.oxides.unicore.central.tss;

import de.fzj.unicore.uas.TargetSystemFactory;
import de.fzj.unicore.wsrflite.xmlbeans.client.RegistryClient;
import eu.unicore.security.etd.TrustDelegation;
import eu.unicore.util.httpclient.IClientConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.w3.x2005.x08.addressing.EndpointReferenceType;
import pl.edu.icm.oxides.config.GridConfig;
import pl.edu.icm.oxides.unicore.GridClientHelper;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class UnicoreSite {
    private final GridConfig gridConfig;
    private final GridClientHelper clientHelper;

    @Autowired
    public UnicoreSite(GridConfig gridConfig, GridClientHelper clientHelper) {
        this.gridConfig = gridConfig;
        this.clientHelper = clientHelper;
    }

    @Cacheable(value = "unicoreSessionSiteList", key = "#trustDelegation.custodianDN")
    public List<UnicoreSiteEntity> retrieveServiceList(TrustDelegation trustDelegation) {
        IClientConfiguration clientConfiguration = clientHelper.createClientConfiguration(trustDelegation);
        return collectTargetSystemList(clientConfiguration);
    }

    private List<UnicoreSiteEntity> collectTargetSystemList(IClientConfiguration clientConfiguration) {
        String registryUrl = gridConfig.getRegistry();
        EndpointReferenceType registryEpr = EndpointReferenceType.Factory.newInstance();
        registryEpr.addNewAddress().setStringValue(registryUrl);
        try {
            return new RegistryClient(registryEpr, clientConfiguration)
                    .listAccessibleServices(TargetSystemFactory.TSF_PORT)
                    .parallelStream()
                    .map(endpointReferenceType -> new UnicoreSiteEntity(endpointReferenceType))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error(String.format("Error retrieving Target System from UNICORE Registry <%s>!", registryUrl), e);
            // TODO: should be used RuntimeException?
            throw new UnavailableSiteException(e);
        }
    }

    private Log log = LogFactory.getLog(UnicoreSite.class);
}

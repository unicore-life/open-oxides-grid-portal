package pl.edu.icm.oxides.unicore.central.broker;

import de.fzj.unicore.wsrflite.xmlbeans.client.RegistryClient;
import eu.unicore.util.httpclient.IClientConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.chemomentum.common.ws.IServiceOrchestrator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.w3.x2005.x08.addressing.EndpointReferenceType;
import pl.edu.icm.oxides.config.GridConfig;
import pl.edu.icm.oxides.unicore.GridClientHelper;
import pl.edu.icm.oxides.user.AuthenticationSession;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class UnicoreBroker {
    private final GridConfig gridConfig;
    private final GridClientHelper clientHelper;

    @Autowired
    public UnicoreBroker(GridConfig gridConfig, GridClientHelper clientHelper) {
        this.gridConfig = gridConfig;
        this.clientHelper = clientHelper;
    }

    @Cacheable(value = "unicoreSessionBrokerList", key = "#authenticationSession.uuid")
    public List<UnicoreBrokerEntity> retrieveServiceList(AuthenticationSession authenticationSession) {
        IClientConfiguration clientConfiguration = clientHelper.createClientConfiguration(authenticationSession);
        return collectBrokerServiceList(clientConfiguration);
    }

    private List<UnicoreBrokerEntity> collectBrokerServiceList(IClientConfiguration clientConfiguration) {
        String registryUrl = gridConfig.getRegistry();
        EndpointReferenceType registryEpr = EndpointReferenceType.Factory.newInstance();
        registryEpr.addNewAddress().setStringValue(registryUrl);
        try {
            return new RegistryClient(registryEpr, clientConfiguration)
                    .listAccessibleServices(IServiceOrchestrator.PORT)
                    .parallelStream()
                    .map(endpointReferenceType -> new UnicoreBrokerEntity(endpointReferenceType))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error(String.format("Error retrieving Service Orchestrator from UNICORE Registry <%s>!", registryUrl), e);
            // TODO: should be used RuntimeException?
            throw new UnavailableBrokerException(e);
        }
    }

    private Log log = LogFactory.getLog(UnicoreBroker.class);
}

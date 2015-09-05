package pl.edu.icm.oxides.unicore.central.broker;

import de.fzj.unicore.wsrflite.xmlbeans.WSUtilities;
import de.fzj.unicore.wsrflite.xmlbeans.client.RegistryClient;
import eu.unicore.security.etd.TrustDelegation;
import eu.unicore.util.httpclient.IClientConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.chemomentum.common.ws.IServiceOrchestrator;
import org.chemomentum.workassignment.xmlbeans.SubmitWorkAssignmentRequestDocument;
import org.chemomentum.workassignment.xmlbeans.SubmitWorkAssignmentResponseDocument;
import org.chemomentum.workassignment.xmlbeans.WorkAssignmentType;
import org.chemomentum.workassignment.xmlbeans.WorkDocument;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionDocument;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.w3.x2005.x08.addressing.EndpointReferenceType;
import pl.edu.icm.oxides.config.GridConfig;
import pl.edu.icm.oxides.config.GridOxidesConfig;
import pl.edu.icm.oxides.portal.model.OxidesSimulation;
import pl.edu.icm.oxides.unicore.GridClientHelper;
import pl.edu.icm.oxides.unicore.simulation.BrokeredJobModel;
import pl.edu.icm.oxides.user.AuthenticationSession;

import javax.security.auth.x500.X500Principal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class UnicoreBroker {
    private final GridConfig gridConfig;
    private final GridOxidesConfig oxidesConfig;
    private final GridClientHelper clientHelper;

    @Autowired
    public UnicoreBroker(GridConfig gridConfig, GridOxidesConfig oxidesConfig, GridClientHelper clientHelper) {
        this.gridConfig = gridConfig;
        this.oxidesConfig = oxidesConfig;
        this.clientHelper = clientHelper;
    }

    @Cacheable(value = "unicoreSessionBrokerList", key = "#trustDelegation.custodianDN")
    public List<UnicoreBrokerEntity> retrieveServiceList(TrustDelegation trustDelegation) {
        IClientConfiguration clientConfiguration = clientHelper.createClientConfiguration(trustDelegation);
        log.debug("COLLECT BROKERS");
        return collectBrokerServiceList(clientConfiguration);
    }

    public void submitBrokeredJob(OxidesSimulation simulation, AuthenticationSession authenticationSession) {
        UnicoreBrokerEntity brokerEntity = retrieveServiceList(authenticationSession.getSelectedTrustDelegation())
                .stream()
                .findAny()
                .orElseThrow(() -> new UnavailableBrokerException(new Exception("NO BROKER AT ALL!")));

        IClientConfiguration clientConfiguration = clientHelper.createClientConfiguration(authenticationSession);
        // Extending ETD with broker's DN:
        clientConfiguration.getETDSettings().setReceiver(
                new X500Principal(
                        WSUtilities.extractServerIDFromEPR(brokerEntity.getEpr())));
//        clientConfiguration.getETDSettings().setExtendTrustDelegation(true);

        Optional<IServiceOrchestrator> brokerClient = brokerEntity.createBrokerClient(clientConfiguration);

        String simulationName = oxidesConfig.getJobPrefix() + simulation.getName();
        JobDefinitionDocument jobDefinitionDocument =
                BrokeredJobModel.prepareJobDefinitionDocument(
                        oxidesConfig.getApplicationName(),
                        oxidesConfig.getApplicationVersion(),
                        simulationName,
                        simulation,
                        authenticationSession.getResources().getImportFiles());
        log.info("BROKER JOB DEFINITION: " + jobDefinitionDocument.toString());


        SubmitWorkAssignmentRequestDocument waDoc = SubmitWorkAssignmentRequestDocument.Factory
                .newInstance();
        WorkAssignmentType workAssignment = waDoc
                .addNewSubmitWorkAssignmentRequest()
                .addNewWorkAssignment();

        WorkDocument.Work work = workAssignment.addNewWork();
        JobDefinitionType jobDef = jobDefinitionDocument.getJobDefinition();
        work.setJobDefinition(jobDef);

        workAssignment.setParent("OpenOxidesGridJob");
        String workAssignmentID = WSUtilities.newUniqueID();
        workAssignment.setId(workAssignmentID);

        workAssignment.setStorageEPR(
                authenticationSession
                        .getResources()
                        .getStorageClient()
                        .getEPR()
        );

        /*
         * if(!isJSDL && builder.getImports().size()>0){
         * createWorkflowDataStorage(); wa.setStorageEPR(storageEPR); try{
         * uploadLocalData(builder, waID); }catch(IOException ex){
         * error("Can't upload local files.",ex); endProcessing(1); } }
         */
        log.info("BROKER WORK ASSIGNMENT: " + workAssignment.toString());
        SubmitWorkAssignmentResponseDocument response =
                brokerClient.get().submitWorkAssignment(waDoc);

        log.info("WA SUBMITTED: " + response);
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

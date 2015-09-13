package pl.edu.icm.oxides.unicore.simulation;

import de.fzj.unicore.wsrflite.xmlbeans.WSUtilities;
import eu.unicore.jsdl.extensions.IgnoreFailureDocument;
import eu.unicore.jsdl.extensions.ResourceRequestDocument;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.ApplicationDocument;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.ApplicationType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.CreationFlagEnumeration;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.DataStagingDocument;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.DataStagingType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionDocument;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDescriptionType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.ResourcesDocument;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.ResourcesType;
import org.ggf.schemas.jsdl.x2005.x11.jsdlPosix.EnvironmentType;
import org.ggf.schemas.jsdl.x2005.x11.jsdlPosix.POSIXApplicationDocument;
import org.ggf.schemas.jsdl.x2005.x11.jsdlPosix.POSIXApplicationType;
import org.unigrids.services.atomic.types.ProtocolType;
import org.w3.x2005.x08.addressing.EndpointReferenceType;
import pl.edu.icm.oxides.portal.model.OxidesSimulation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class BrokeredJobModel {
    private BrokeredJobModel() {
    }

    public static JobDefinitionDocument prepareJobDefinitionDocument(String applicationName,
                                                                     String applicationVersion,
                                                                     String simulationName,
                                                                     OxidesSimulation simulation,
                                                                     String inputScriptName,
                                                                     EndpointReferenceType storageEpr) {
        ApplicationDocument appDoc = ApplicationDocument.Factory.newInstance();
        ApplicationType app = appDoc.addNewApplication();
        app.setApplicationName(applicationName);
        if (applicationVersion != null) {
            app.setApplicationVersion(applicationVersion);
        }

        // TODO:
        POSIXApplicationDocument posixApplicationDocument = POSIXApplicationDocument.Factory.newInstance();
        POSIXApplicationType posixApplicationType = posixApplicationDocument.addNewPOSIXApplication();
        EnvironmentType environmentType = posixApplicationType.addNewEnvironment();
        environmentType.setName("SOURCE");
        environmentType.setStringValue(INPUT_SCRIPT_DESTINATION_NAME);
        WSUtilities.append(posixApplicationDocument, app);

        JobDescriptionType jobDesc = JobDescriptionType.Factory.newInstance();
        jobDesc.setApplication(app);

        jobDesc.addNewJobIdentification().setJobName(simulationName);

        List<DataStagingType> dataStaging = createDataStagingFragment(
                simulation.getFiles(),
                inputScriptName,
                storageEpr);
        jobDesc.setDataStagingArray(dataStaging.toArray(new DataStagingType[dataStaging.size()]));

        jobDesc.setResources(prepareResourceFragment(simulation));

        JobDefinitionDocument jobDefinitionDocument = JobDefinitionDocument.Factory.newInstance();
        jobDefinitionDocument.addNewJobDefinition().setJobDescription(jobDesc);

        return jobDefinitionDocument;
    }

    private static List<DataStagingType> createDataStagingFragment(List<String> files,
                                                                   String inputScriptName,
                                                                   EndpointReferenceType epr) {
        List<DataStagingType> dataStagingList = new ArrayList<>();

        files.forEach((filename) -> {
            DataStagingDocument dataStagingDocument = DataStagingDocument.Factory.newInstance();
            DataStagingType dataStagingType = dataStagingDocument.addNewDataStaging();
            dataStagingType.setFileName(filename);
            dataStagingType.setCreationFlag(CreationFlagEnumeration.OVERWRITE);

            dataStagingType.addNewSource().setURI(filenameToStorageUri(filename, epr));

            IgnoreFailureDocument ifd = IgnoreFailureDocument.Factory.newInstance();
            ifd.setIgnoreFailure(false);

            WSUtilities.append(ifd, dataStagingDocument);
            dataStagingList.add(dataStagingType);
        });

        // Workaround for script input:
        //
        DataStagingDocument dataStagingDocument = DataStagingDocument.Factory.newInstance();
        DataStagingType dataStagingType = dataStagingDocument.addNewDataStaging();
        dataStagingType.setFileName(INPUT_SCRIPT_DESTINATION_NAME);
        dataStagingType.setCreationFlag(CreationFlagEnumeration.OVERWRITE);
        dataStagingType.addNewSource().setURI(filenameToStorageUri(inputScriptName, epr));

        IgnoreFailureDocument ifd = IgnoreFailureDocument.Factory.newInstance();
        ifd.setIgnoreFailure(false);
        WSUtilities.append(ifd, dataStagingDocument);

        dataStagingList.add(dataStagingType);

        return dataStagingList;
    }

    private static String filenameToStorageUri(String filename, EndpointReferenceType storageEpr) {
        return ProtocolType.BFT + ":" + storageEpr.getAddress().getStringValue() + "#" + filename;
    }

    private static ResourcesType prepareResourceFragment(OxidesSimulation simulation) {
        ResourcesDocument resourcesDocument = ResourcesDocument.Factory.newInstance();
        ResourcesType resourcesType = resourcesDocument.addNewResources();

        if (!isNullOrBlank(simulation.getProject())) {
            insertResourceRequest("Project", simulation.getProject(), resourcesDocument);
        }
        if (!isNullOrBlank(simulation.getQueue())) {
            insertResourceRequest("Queue", simulation.getQueue(), resourcesDocument);
        }
        if (simulation.getMemory() != null) {
            BigDecimal memory = new BigDecimal(simulation.getMemory()).multiply(new BigDecimal(1024L * 1024L));
            resourcesType.addNewIndividualPhysicalMemory().addNewExact().setDoubleValue(memory.doubleValue());
        }
        if (simulation.getNodes() != null) {
            resourcesType.addNewTotalResourceCount().addNewExact().setDoubleValue(simulation.getNodes());
        }
        if (simulation.getCpus() != null) {
            resourcesType.addNewIndividualCPUCount().addNewExact().setDoubleValue(simulation.getCpus());
        }
        if (!isNullOrBlank(simulation.getReservation())) {
            try {
                insertReservation(simulation.getReservation(), resourcesDocument);
            } catch (XmlException e) {
                log.warn("Could not add reservation resource! Skipping it.", e);
            }
        }
        return resourcesType;
    }

    private static void insertResourceRequest(String name, String value, ResourcesDocument resourcesDocument) {
        ResourceRequestDocument resourceRequestDocument = ResourceRequestDocument.Factory.newInstance();
        resourceRequestDocument.addNewResourceRequest().setName(name);
        resourceRequestDocument.getResourceRequest().setValue(value);
        WSUtilities.append(resourceRequestDocument, resourcesDocument);
    }

    private static void insertReservation(String id, ResourcesDocument resourcesDocument) throws XmlException {
        String reservationXml = "<u6rr:ReservationReference xmlns:u6rr=\"http://www.unicore.eu/unicore/xnjs\">"
                + id + "</u6rr:ReservationReference>";
        XmlObject xmlObject = XmlObject.Factory.parse(reservationXml);

//        ReservationReferenceDocument rrd = ReservationReferenceDocument.Factory.newInstance();
//        rrd.setReservationReference(id);
        WSUtilities.append(xmlObject, resourcesDocument);
    }

    private static boolean isNullOrBlank(String param) {
        return param == null || param.trim().length() == 0;
    }

    private static Log log = LogFactory.getLog(BrokeredJobModel.class);

    private static final String INPUT_SCRIPT_DESTINATION_NAME = "input";
}

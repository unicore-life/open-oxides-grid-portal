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

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public final class BrokeredJobModel {
    private BrokeredJobModel() {
    }

    public static JobDefinitionDocument prepareJobDefinitionDocument(String applicationName,
                                                                     String applicationVersion,
                                                                     WorkAssignmentDescription waDescription,
                                                                     EndpointReferenceType storageEpr) {
        JobDescriptionType jobDescription = JobDescriptionType.Factory.newInstance();
        jobDescription.setApplication(
                createApplicationDescription(applicationName, applicationVersion));
        jobDescription.addNewJobIdentification().setJobName(waDescription.getName());

        List<DataStagingType> dataStaging = createDataStagingFragment(waDescription.getFiles(), storageEpr);
        jobDescription.setDataStagingArray(dataStaging.toArray(new DataStagingType[dataStaging.size()]));

        jobDescription.setResources(prepareResourceFragment(waDescription));

        JobDefinitionDocument jobDefinitionDocument = JobDefinitionDocument.Factory.newInstance();
        jobDefinitionDocument.addNewJobDefinition().setJobDescription(jobDescription);

        return jobDefinitionDocument;
    }

    private static ApplicationType createApplicationDescription(String applicationName, String applicationVersion) {
        ApplicationDocument applicationDocument = ApplicationDocument.Factory.newInstance();
        ApplicationType applicationType = applicationDocument.addNewApplication();
        applicationType.setApplicationName(applicationName);
        if (applicationVersion != null) {
            applicationType.setApplicationVersion(applicationVersion);
        }
        WSUtilities.append(createBashScriptPosixDescription(), applicationType);
        return applicationType;
    }

    private static POSIXApplicationDocument createBashScriptPosixDescription() {
        POSIXApplicationDocument posixApplicationDocument = POSIXApplicationDocument.Factory.newInstance();
        POSIXApplicationType posixApplicationType = posixApplicationDocument.addNewPOSIXApplication();
        EnvironmentType environmentType = posixApplicationType.addNewEnvironment();
        environmentType.setName("SOURCE");
        environmentType.setStringValue(INPUT_SCRIPT_DESTINATION_NAME);
        return posixApplicationDocument;
    }

    private static List<DataStagingType> createDataStagingFragment(List<WorkAssignmentFile> files,
                                                                   EndpointReferenceType epr) {
        return files.stream()
                .map(fileEntry -> createDataStagingEntry(
                        fileEntry.getSourceName(),
                        fileEntry.getDestinationName(),
                        epr))
                .collect(Collectors.toList());
    }

    private static DataStagingType createDataStagingEntry(String sourceFilename,
                                                          String destinationFilename,
                                                          EndpointReferenceType epr) {
        DataStagingDocument dataStagingDocument = DataStagingDocument.Factory.newInstance();
        DataStagingType dataStagingType = dataStagingDocument.addNewDataStaging();
        dataStagingType.setFileName(destinationFilename);
        dataStagingType.setCreationFlag(CreationFlagEnumeration.OVERWRITE);

        dataStagingType.addNewSource().setURI(filenameToStorageUri(sourceFilename, epr));

        IgnoreFailureDocument ifd = IgnoreFailureDocument.Factory.newInstance();
        ifd.setIgnoreFailure(false);
        WSUtilities.append(ifd, dataStagingDocument);

        return dataStagingType;
    }

    private static String filenameToStorageUri(String filename, EndpointReferenceType storageEpr) {
        return ProtocolType.BFT + ":" + storageEpr.getAddress().getStringValue() + "#" + filename;
    }

    private static ResourcesType prepareResourceFragment(WorkAssignmentDescription description) {
        ResourcesDocument resourcesDocument = ResourcesDocument.Factory.newInstance();
        ResourcesType resourcesType = resourcesDocument.addNewResources();

        if (!isNullOrBlank(description.getProject())) {
            insertResourceRequest("Project", description.getProject(), resourcesDocument);
        }
        if (!isNullOrBlank(description.getQueue())) {
            insertResourceRequest("Queue", description.getQueue(), resourcesDocument);
        }
        if (description.getMemory() != null) {
            BigDecimal memory = new BigDecimal(description.getMemory()).multiply(ONE_MEGA_BIG_DECIMAL);
            resourcesType.addNewIndividualPhysicalMemory().addNewExact().setDoubleValue(memory.doubleValue());
        }
        if (description.getNodes() != null) {
            resourcesType.addNewTotalResourceCount().addNewExact().setDoubleValue(description.getNodes());
        }
        if (description.getCpus() != null) {
            resourcesType.addNewIndividualCPUCount().addNewExact().setDoubleValue(description.getCpus());
        }
        if (!isNullOrBlank(description.getReservation())) {
            try {
                insertReservation(description.getReservation(), resourcesDocument);
            } catch (XmlException e) {
                log.warn("Could not add reservation resource! Skipping it.", e);
            }
        }
        if (!isNullOrBlank(description.getProperty())) {
            insertResourceRequest("NodesFilter", description.getProperty(), resourcesDocument);
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
        WSUtilities.append(xmlObject, resourcesDocument);
//        ReservationReferenceDocument rrd = ReservationReferenceDocument.Factory.newInstance();
//        rrd.setReservationReference(id);
//        WSUtilities.append(rrd, resourcesDocument);
    }

    private static boolean isNullOrBlank(String param) {
        return param == null || param.trim().length() == 0;
    }

    private static Log log = LogFactory.getLog(BrokeredJobModel.class);

    private static final String INPUT_SCRIPT_DESTINATION_NAME = "input";
    private static final BigDecimal ONE_MEGA_BIG_DECIMAL = new BigDecimal(1024L * 1024L);
}

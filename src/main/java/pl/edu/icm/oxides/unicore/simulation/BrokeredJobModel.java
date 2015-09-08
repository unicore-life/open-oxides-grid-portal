package pl.edu.icm.oxides.unicore.simulation;

import de.fzj.unicore.wsrflite.xmlbeans.WSUtilities;
import eu.unicore.jsdl.extensions.IgnoreFailureDocument;
import eu.unicore.jsdl.extensions.ResourceRequestDocument;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.values.XmlValueOutOfRangeException;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.ApplicationDocument;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.ApplicationType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.CreationFlagEnumeration;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.DataStagingDocument;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.DataStagingType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionDocument;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDescriptionType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.ResourcesDocument;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.ResourcesType;
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
                                                                     EndpointReferenceType storageEpr) {
        JobDescriptionType jobDesc = JobDescriptionType.Factory.newInstance();
        ApplicationDocument appDoc = ApplicationDocument.Factory.newInstance();
        ApplicationType app = appDoc.addNewApplication();
        app.setApplicationName(applicationName);
        app.setApplicationVersion(applicationVersion);
        jobDesc.setApplication(app);

        jobDesc.addNewJobIdentification().setJobName(simulationName);

        List<DataStagingType> dataStaging = createDataStagingFragment(simulation.getFiles(), storageEpr);
        jobDesc.setDataStagingArray(dataStaging.toArray(new DataStagingType[dataStaging.size()]));

        jobDesc.setResources(prepareResourceFragment(simulation));

        JobDefinitionDocument jobDefinitionDocument = JobDefinitionDocument.Factory.newInstance();
        jobDefinitionDocument.addNewJobDefinition().setJobDescription(jobDesc);

        return jobDefinitionDocument;
    }

    private static List<DataStagingType> createDataStagingFragment(List<String> files, EndpointReferenceType epr) {
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
        if (!isNullOrBlank(simulation.getMemory())) {
            try {
                BigDecimal memory = new BigDecimal(simulation.getMemory()).multiply(new BigDecimal(1024L * 1024L));
                resourcesType.addNewIndividualPhysicalMemory().addNewExact().setStringValue(memory.toPlainString());
            } catch (NumberFormatException e) {
                log.warn("Memory is not a number! Skipping resource restriction.", e);
            }
        }
        if (!isNullOrBlank(simulation.getNodes())) {
            try {
                resourcesType.addNewTotalResourceCount().addNewExact().setStringValue(simulation.getNodes());
            } catch (XmlValueOutOfRangeException e) {
                log.warn("Wrong nodes number. Skipping resource restriction.", e);
            }
        }
        if (!isNullOrBlank(simulation.getCpus())) {
            try {
                resourcesType.addNewIndividualCPUCount().addNewExact().setStringValue(simulation.getCpus());
            } catch (XmlValueOutOfRangeException e) {
                log.warn("Wrong CPUs number. Skipping resource restriction.", e);
            }
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
        WSUtilities.append(xmlObject, resourcesDocument);
    }

    private static boolean isNullOrBlank(String param) {
        return param == null || param.trim().length() == 0;
    }

    private static Log log = LogFactory.getLog(BrokeredJobModel.class);
}

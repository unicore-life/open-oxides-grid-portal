package pl.edu.icm.oxides.unicore.simulation;

import de.fzj.unicore.wsrflite.xmlbeans.WSUtilities;
import eu.unicore.jsdl.extensions.IgnoreFailureDocument;
import eu.unicore.jsdl.extensions.ResourceRequestDocument;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import pl.edu.icm.oxides.portal.model.OxidesSimulation;
import pl.edu.icm.oxides.portal.model.SimulationImportFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class BrokeredJobModel {
    private BrokeredJobModel() {
    }

    public static JobDefinitionDocument prepareJobDefinitionDocument(String applicationName,
                                                                     String applicationVersion,
                                                                     String simulationName,
                                                                     OxidesSimulation simulation,
                                                                     Map<String, SimulationImportFile> importFiles) {
        JobDescriptionType jobDesc = JobDescriptionType.Factory.newInstance();
        ApplicationDocument appDoc = ApplicationDocument.Factory.newInstance();
        ApplicationType app = appDoc.addNewApplication();
        app.setApplicationName(applicationName);
        app.setApplicationVersion(applicationVersion);
        jobDesc.setApplication(app);

        jobDesc.addNewJobIdentification().setJobName(simulationName);

        List<DataStagingType> dataStaging = createDataStagingFragment(importFiles);
        jobDesc.setDataStagingArray(dataStaging.toArray(new DataStagingType[dataStaging.size()]));

        jobDesc.setResources(prepareResourceFragment(simulation));

        JobDefinitionDocument jobDefinitionDocument = JobDefinitionDocument.Factory.newInstance();
        jobDefinitionDocument.addNewJobDefinition().setJobDescription(jobDesc);

        return jobDefinitionDocument;
    }

    private static List<DataStagingType> createDataStagingFragment(Map<String, SimulationImportFile> importFiles) {
        List<DataStagingType> dataStagingList = new ArrayList<>();

        importFiles.forEach((filename, importFile) -> {
            DataStagingDocument dataStagingDocument = DataStagingDocument.Factory.newInstance();
            DataStagingType dataStagingType = dataStagingDocument.addNewDataStaging();
            dataStagingType.setFileName(importFile.getName());
            dataStagingType.setCreationFlag(CreationFlagEnumeration.OVERWRITE);

            dataStagingType.addNewSource().setURI(importFile.getUri());

            IgnoreFailureDocument ifd = IgnoreFailureDocument.Factory.newInstance();
            ifd.setIgnoreFailure(false);

            WSUtilities.append(ifd, dataStagingDocument);
            dataStagingList.add(dataStagingType);
        });

        return dataStagingList;
    }

    private static ResourcesType prepareResourceFragment(OxidesSimulation simulation) {
        ResourcesDocument resourcesDocument = ResourcesDocument.Factory.newInstance();
        ResourcesType resourcesType = resourcesDocument.addNewResources();

        if (simulation.getProject() != null) {
            insertResourceRequest("Project", simulation.getProject(), resourcesDocument);
        }
        if (simulation.getQueue() != null) {
            insertResourceRequest("Queue", simulation.getQueue(), resourcesDocument);
        }
        if (simulation.getMemory() != null) {
            try {
                BigDecimal memory = new BigDecimal(simulation.getMemory()).multiply(new BigDecimal(1024L * 1024L));
                resourcesType.addNewIndividualPhysicalMemory().addNewExact().setStringValue(memory.toPlainString());
            } catch (NumberFormatException e) {
                log.warn("Memory is not a number! Skipping resource restriction.", e);
            }
        }
        if (simulation.getNodes() != null) {
            try {
                resourcesType.addNewTotalResourceCount().addNewExact().setStringValue(simulation.getNodes());
            } catch (XmlValueOutOfRangeException e) {
                log.warn("Wrong nodes number. Skipping resource restriction.", e);
            }
        }
        if (simulation.getCpus() != null) {
            try {
                resourcesType.addNewIndividualCPUCount().addNewExact().setStringValue(simulation.getCpus());
            } catch (XmlValueOutOfRangeException e) {
                log.warn("Wrong CPUs number. Skipping resource restriction.", e);
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

    private static Log log = LogFactory.getLog(BrokeredJobModel.class);
}

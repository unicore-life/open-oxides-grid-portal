package pl.edu.icm.oxides.unicore.simulation;

import de.fzj.unicore.wsrflite.xmlbeans.WSUtilities;
import eu.unicore.jsdl.extensions.IgnoreFailureDocument;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.ApplicationDocument;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.ApplicationType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.CreationFlagEnumeration;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.DataStagingDocument;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.DataStagingType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionDocument;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDescriptionType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class BrokeredJobModel {
    private BrokeredJobModel() {
    }

    public static JobDefinitionDocument prepareJobDefinitionDocument(String applicationName,
                                                                     String applicationVersion,
                                                                     String simulationName,
                                                                     Map<String, String> importFiles) {
        JobDescriptionType jobDesc = JobDescriptionType.Factory.newInstance();
        ApplicationDocument appDoc = ApplicationDocument.Factory.newInstance();
        ApplicationType app = appDoc.addNewApplication();
        app.setApplicationName(applicationName);
        app.setApplicationVersion(applicationVersion);
        jobDesc.setApplication(app);

        jobDesc.addNewJobIdentification().setJobName(simulationName);

        List<DataStagingType> dataStaging = createDataStagingFragment(importFiles);
        jobDesc.setDataStagingArray(dataStaging.toArray(new DataStagingType[dataStaging.size()]));

//        try {
//            jobDesc.setResources(makeResources());
//        } catch (Exception e) {
//            logger.error("Error setting job resources!", e);
//        }

        JobDefinitionDocument jobDefinitionDocument = JobDefinitionDocument.Factory.newInstance();
        jobDefinitionDocument.addNewJobDefinition().setJobDescription(jobDesc);

        return jobDefinitionDocument;
    }

    private static List<DataStagingType> createDataStagingFragment(Map<String, String> importFiles) {
        List<DataStagingType> dataStagingList = new ArrayList<>();

        importFiles.forEach((filename, gridPath) -> {
            DataStagingDocument dsd = DataStagingDocument.Factory.newInstance();
            DataStagingType d = dsd.addNewDataStaging();
            d.setFileName(filename);
            d.setCreationFlag(CreationFlagEnumeration.OVERWRITE);

            d.addNewSource().setURI(gridPath);

            IgnoreFailureDocument ifd = IgnoreFailureDocument.Factory.newInstance();
            ifd.setIgnoreFailure(false);

            WSUtilities.append(ifd, dsd);
            dataStagingList.add(d);
        });

        return dataStagingList;
    }
}

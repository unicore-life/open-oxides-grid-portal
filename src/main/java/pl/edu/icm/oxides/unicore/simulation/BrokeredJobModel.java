package pl.edu.icm.oxides.unicore.simulation;

import org.ggf.schemas.jsdl.x2005.x11.jsdl.ApplicationDocument;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.ApplicationType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionDocument;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDescriptionType;

public final class BrokeredJobModel {
    private BrokeredJobModel() {
    }

    public static JobDefinitionDocument prepareJobDefinitionDocument(String applicationName,
                                                                     String applicationVersion,
                                                                     String simulationName) {
        JobDescriptionType jobDesc = JobDescriptionType.Factory.newInstance();
        ApplicationDocument appDoc = ApplicationDocument.Factory.newInstance();
        ApplicationType app = appDoc.addNewApplication();
        app.setApplicationName(applicationName);
        app.setApplicationVersion(applicationVersion);
        jobDesc.setApplication(app);

        jobDesc.addNewJobIdentification().setJobName(simulationName);

//        List<DataStagingType> dataStaging = createDataStagingFragment();
//        jobDesc.setDataStagingArray(dataStaging.toArray(new DataStagingType[dataStaging.size()]));

//        try {
//            jobDesc.setResources(makeResources());
//        } catch (Exception e) {
//            logger.error("Error setting job resources!", e);
//        }

        JobDefinitionDocument jobDefinitionDocument = JobDefinitionDocument.Factory.newInstance();
        jobDefinitionDocument.addNewJobDefinition().setJobDescription(jobDesc);

        return jobDefinitionDocument;
    }
}

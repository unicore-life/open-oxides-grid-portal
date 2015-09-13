package pl.edu.icm.oxides.unicore.site.job;

import de.fzj.unicore.uas.client.JobClient;
import de.fzj.unicore.wsrflite.xmlbeans.client.BaseWSRFClient;
import eu.unicore.security.etd.TrustDelegation;
import eu.unicore.util.httpclient.IClientConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasisOpen.docs.wsrf.rl2.TerminationTimeDocument.TerminationTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.unigrids.x2006.x04.services.jms.JobPropertiesDocument.JobProperties;
import org.w3.x2005.x08.addressing.EndpointReferenceType;
import pl.edu.icm.oxides.config.GridOxidesConfig;
import pl.edu.icm.oxides.unicore.GridClientHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;

@Component
class UnicoreJobOperations {
    private final GridOxidesConfig oxidesConfig;
    private final GridClientHelper clientHelper;
    private final ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    UnicoreJobOperations(GridOxidesConfig oxidesConfig,
                         GridClientHelper clientHelper,
                         ThreadPoolTaskExecutor taskExecutor) {
        this.oxidesConfig = oxidesConfig;
        this.clientHelper = clientHelper;
        this.taskExecutor = taskExecutor;
    }

    @Cacheable(
            value = "unicoreSessionJobClientList",
            key = "#trustDelegation.custodianDN + '_' + #epr.getAddress().getStringValue()",
            unless = "#result == null || "
                    + "("
                    + "  #result.getStatusInfo().getStatus().toString().compareTo('SUCCESSFUL') != 0"
                    + " &&"
                    + "  #result.getStatusInfo().getStatus().toString().compareTo('FAILED') != 0"
                    + ")"
    )
    public JobProperties retrieveJobProperties(EndpointReferenceType epr, TrustDelegation trustDelegation) {
        log.trace("Retrieving properties for job: " + epr.getAddress().getStringValue());

        IClientConfiguration clientConfiguration = clientHelper.createClientConfiguration(trustDelegation);
        try {
            return new JobClient(epr, clientConfiguration)
                    .getResourcePropertiesDocument()
                    .getJobProperties();
        } catch (Exception e) {
            log.error("Error retrieving job properties for job <" + epr.getAddress().getStringValue()
                    + "> of <" + trustDelegation.getCustodianDN() + ">", e);
            return null;
        }
    }

    @CacheEvict(value = "unicoreSessionJobList", key = "#trustDelegation.custodianDN")
    public void destroyJob(EndpointReferenceType epr, TrustDelegation trustDelegation) {
        taskExecutor.execute(() -> {
            try {
                new BaseWSRFClient(epr, clientHelper.createClientConfiguration(trustDelegation))
                        .destroy();
            } catch (Exception e) {
                log.error("Could not destroy job <" + epr.getAddress().getStringValue() + ">", e);
            }
        });
    }

    UnicoreJobEntity translateJobPropertiesToUnicoreJobEntity(JobProperties jobProperties) {
        EndpointReferenceType epr = jobProperties.getResourceEndpointReference();
        String name = jobProperties.getOriginalJSDL()
                .getJobDescription()
                .getJobIdentification()
                .getJobName();
        String status = jobProperties.getStatusInfo()
                .getStatus()
                .toString();
        Calendar submissionTime = jobProperties.getSubmissionTime();
        String queue = jobProperties.getQueue();

        return new UnicoreJobEntity(epr, name, status, submissionTime, queue);
    }

    UnicoreJobDetailsEntity translateJobPropertiesToUnicoreJobDetailsEntity(JobProperties jobProperties) {
        EndpointReferenceType epr = jobProperties.getResourceEndpointReference();
        String uri = epr.getAddress().getStringValue();
        String name = jobProperties.getOriginalJSDL()
                .getJobDescription()
                .getJobIdentification()
                .getJobName();
        if (name.startsWith(oxidesConfig.getJobPrefix())) {
            name = name.substring(oxidesConfig.getJobPrefix().length());
        }
        String jobLog = jobProperties.getLog();
        Calendar submissionTime = jobProperties.getSubmissionTime();
        TerminationTime terminationTime = jobProperties.getTerminationTime();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(submissionTime.getTimeZone());

        return new UnicoreJobDetailsEntity(uri, name, jobLog,
                dateFormat.format(submissionTime.getTime()),
                dateFormat.format(terminationTime.getDateValue()));
    }

    private Log log = LogFactory.getLog(UnicoreJobOperations.class);
}

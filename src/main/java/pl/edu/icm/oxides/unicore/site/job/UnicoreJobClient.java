package pl.edu.icm.oxides.unicore.site.job;

import de.fzj.unicore.uas.client.JobClient;
import eu.unicore.security.etd.TrustDelegation;
import eu.unicore.util.httpclient.IClientConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasisOpen.docs.wsrf.rl2.TerminationTimeDocument.TerminationTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.unigrids.x2006.x04.services.jms.JobPropertiesDocument.JobProperties;
import org.w3.x2005.x08.addressing.EndpointReferenceType;
import pl.edu.icm.oxides.config.GridOxidesConfig;
import pl.edu.icm.oxides.unicore.GridClientHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;

@Component
@CacheConfig(cacheNames = {"unicoreSessionJobClientList"})
public class UnicoreJobClient {
    private final GridOxidesConfig oxidesConfig;
    private final GridClientHelper clientHelper;

    @Autowired
    public UnicoreJobClient(GridOxidesConfig oxidesConfig, GridClientHelper clientHelper) {
        this.oxidesConfig = oxidesConfig;
        this.clientHelper = clientHelper;
    }

    @Cacheable(
            key = "#trustDelegation.custodianDN + '_' + #uri",
            unless = "#result == null"
    )
    public JobProperties retrieveJobProperties(String uri, TrustDelegation trustDelegation) {
        EndpointReferenceType epr = EndpointReferenceType.Factory.newInstance();
        epr.addNewAddress().setStringValue(uri);

        IClientConfiguration clientConfiguration = clientHelper.createClientConfiguration(trustDelegation);
        try {
            return new JobClient(epr, clientConfiguration)
                    .getResourcePropertiesDocument()
                    .getJobProperties();
        } catch (Exception e) {
            log.error("Error retrieving job properties for job <" + uri + "> of <"
                    + trustDelegation.getCustodianDN() + ">", e);
            return null;
        }
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

    private Log log = LogFactory.getLog(UnicoreJobClient.class);
}

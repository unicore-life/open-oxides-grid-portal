package pl.edu.icm.oxides.unicore.site.job;

import de.fzj.unicore.uas.client.JobClient;
import eu.unicore.security.etd.TrustDelegation;
import eu.unicore.util.httpclient.IClientConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.unigrids.x2006.x04.services.jms.JobPropertiesDocument;
import org.w3.x2005.x08.addressing.EndpointReferenceType;
import pl.edu.icm.oxides.config.GridOxidesConfig;
import pl.edu.icm.oxides.unicore.GridClientHelper;

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

    //    @Cacheable(key = "#trustDelegation.custodianDN", unless = "#result.completed")
    @Cacheable(key = "#trustDelegation.custodianDN+'_'+#uri")
    public UnicoreJobEntity retrieveJobProperties(String uri, TrustDelegation trustDelegation) {
        EndpointReferenceType epr = EndpointReferenceType.Factory.newInstance();
        epr.addNewAddress().setStringValue(uri);

        IClientConfiguration clientConfiguration = clientHelper.createClientConfiguration(trustDelegation);
        try {
            JobClient jobClient = new JobClient(epr, clientConfiguration);
            JobPropertiesDocument document = jobClient.getResourcePropertiesDocument();
            JobPropertiesDocument.JobProperties jobProperties = document.getJobProperties();

            String status = jobClient.getStatus().toString();

            Calendar submissionTime = jobProperties.getSubmissionTime();
            String queue = jobProperties.getQueue();

            String name = jobProperties.getOriginalJSDL().getJobDescription().getJobIdentification().getJobName();

            return new UnicoreJobEntity(epr, name, status, submissionTime, queue);
        } catch (Exception e) {
            log.error("Error retrieving job properties for job <" + uri + "> of <"
                    + trustDelegation.getCustodianDN() + ">", e);
            return null;
        }
    }

    private Log log = LogFactory.getLog(UnicoreJobClient.class);
}

package pl.edu.icm.oxides.unicore.site.job;

import de.fzj.unicore.uas.client.TSFClient;
import de.fzj.unicore.uas.client.TSSClient;
import eu.unicore.util.httpclient.IClientConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.w3.x2005.x08.addressing.EndpointReferenceType;
import pl.edu.icm.oxides.authn.AuthenticationSession;
import pl.edu.icm.oxides.unicore.GridClientHelper;
import pl.edu.icm.oxides.unicore.central.tss.UnicoreSite;
import pl.edu.icm.oxides.unicore.central.tss.UnicoreSiteEntity;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class UnicoreJob {
    private final GridClientHelper clientHelper;
    private final UnicoreSite unicoreSite;

    @Autowired
    public UnicoreJob(GridClientHelper clientHelper, UnicoreSite unicoreSite) {
        this.clientHelper = clientHelper;
        this.unicoreSite = unicoreSite;
    }

    @Cacheable(value = "unicoreSessionJobList", key = "#authenticationSession.uuid")
    public List<UnicoreJobEntity> retrieveSiteResourceList(AuthenticationSession authenticationSession) {
        IClientConfiguration clientConfiguration = clientHelper.createClientConfiguration(authenticationSession);
        return unicoreSite.retrieveServiceList(authenticationSession)
                .parallelStream()
                .map(siteEntity -> toAccessibleTargetSystems(siteEntity, clientConfiguration))
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .map(targetSystemEpr -> toTargetSystemJobList(targetSystemEpr, clientConfiguration))
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .map(this::toUnicoreJobEntity)
                .collect(Collectors.toList());
    }

    public List<EndpointReferenceType> toAccessibleTargetSystems(UnicoreSiteEntity unicoreSiteEntity,
                                                                 IClientConfiguration clientConfiguration) {
        try {
            return new TSFClient(unicoreSiteEntity.getEpr(), clientConfiguration)
                    .getAccessibleTargetSystems();
        } catch (Exception e) {
            log.warn(String.format("Could not get accessible target systems from site <%s>!",
                    unicoreSiteEntity.getUri()), e);
        }
        return null;
    }


    private List<EndpointReferenceType> toTargetSystemJobList(EndpointReferenceType targetSystemEpr,
                                                              IClientConfiguration clientConfiguration) {
        try {
            return new TSSClient(targetSystemEpr, clientConfiguration)
                    .getJobs();
        } catch (Exception e) {
            log.warn(String.format("Could not get jobs from target system <%s>!",
                    targetSystemEpr.getAddress().getStringValue()), e);
        }
        return null;
    }

    private UnicoreJobEntity toUnicoreJobEntity(EndpointReferenceType epr) {
        return new UnicoreJobEntity(epr);
    }

    private Log log = LogFactory.getLog(UnicoreJob.class);
}

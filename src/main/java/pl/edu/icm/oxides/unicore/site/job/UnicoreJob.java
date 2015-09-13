package pl.edu.icm.oxides.unicore.site.job;

import eu.unicore.security.etd.TrustDelegation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3.x2005.x08.addressing.EndpointReferenceType;
import pl.edu.icm.oxides.config.GridOxidesConfig;
import pl.edu.icm.oxides.portal.model.SimulationGridFile;
import pl.edu.icm.oxides.unicore.GridClientHelper;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class UnicoreJob {
    private final UnicoreJobsListing unicoreJobsListing;
    private final UnicoreJobEprCache unicoreJobEprCache;
    private final UnicoreJobOperations unicoreJobOperations;
    private final UnicoreJobStorage unicoreJobStorage;
    private final GridClientHelper clientHelper;
    private final GridOxidesConfig oxidesConfig;

    @Autowired
    public UnicoreJob(UnicoreJobsListing unicoreJobsListing,
                      UnicoreJobEprCache unicoreJobEprCache,
                      UnicoreJobOperations unicoreJobOperations,
                      UnicoreJobStorage unicoreJobStorage,
                      GridClientHelper clientHelper,
                      GridOxidesConfig oxidesConfig) {
        this.unicoreJobsListing = unicoreJobsListing;
        this.unicoreJobEprCache = unicoreJobEprCache;
        this.unicoreJobOperations = unicoreJobOperations;
        this.unicoreJobStorage = unicoreJobStorage;
        this.clientHelper = clientHelper;
        this.oxidesConfig = oxidesConfig;
    }

    // TODO: do some cache at this level
    public List<UnicoreJobEntity> retrieveSiteResourceList(TrustDelegation trustDelegation) {
        log.trace("Retrieving job list for user " + trustDelegation.getCustodianDN());
        return getJobsList(trustDelegation)
                .parallelStream()
                .map(epr -> unicoreJobOperations.retrieveJobProperties(epr, trustDelegation))
                .filter(Objects::nonNull)
                .map(unicoreJobOperations::translateJobPropertiesToUnicoreJobEntity)
                .filter(unicoreJobEntity -> unicoreJobEntity.getFullName().startsWith(oxidesConfig.getJobPrefix()))
                .sorted((o1, o2) -> o1.getTimestamp() > o2.getTimestamp() ? -1 : 1)
                .collect(Collectors.toList());
    }

    public void destroySiteResource(UUID simulationUuid, TrustDelegation trustDelegation) {
        getEpr(simulationUuid, trustDelegation)
                .ifPresent(epr -> unicoreJobOperations.destroyJob(epr, trustDelegation));
    }

    private Optional<EndpointReferenceType> getEpr(UUID uuid, TrustDelegation trustDelegation) {
        String uuidString = String.valueOf(uuid);
        EndpointReferenceType uuidEpr = unicoreJobEprCache.get(uuidString);
        if (uuidEpr == null) {
            return Optional.empty();
        }
        return getJobsList(trustDelegation)
                .stream()
                .filter(epr -> epr.getAddress().getStringValue().endsWith(uuidString))
                .findFirst();
    }

    private List<EndpointReferenceType> getJobsList(TrustDelegation trustDelegation) {
        List<EndpointReferenceType> jobsListing = unicoreJobsListing.retrieveSiteResourceList(trustDelegation);
        jobsListing.forEach(epr -> {
                    String uriString = epr.getAddress().getStringValue();
                    UUID uuid = UUID.fromString(uriString.substring(uriString.length() - 36));
                    unicoreJobEprCache.put(String.valueOf(uuid), epr);
                }
        );
        return jobsListing;
    }

    public UnicoreJobDetailsEntity retrieveJobDetails(UUID simulationUuid, TrustDelegation trustDelegation) {
        return getEpr(simulationUuid, trustDelegation)
                .map(epr -> unicoreJobOperations.retrieveJobProperties(epr, trustDelegation))
                .map(unicoreJobOperations::translateJobPropertiesToUnicoreJobDetailsEntity)
                .orElseThrow(() -> new RuntimeException("Problem while getting details!"));
    }

    public List<SimulationGridFile> listJobFiles(UUID simulationUuid,
                                                 Optional<String> path,
                                                 TrustDelegation trustDelegation) {
        Optional<EndpointReferenceType> epr = getEpr(simulationUuid, trustDelegation);
        return unicoreJobStorage.listFiles(epr, path, trustDelegation);
    }

    public void downloadJobFile(UUID simulationUuid,
                                Optional<String> path,
                                HttpServletResponse response,
                                TrustDelegation trustDelegation) {
        Optional<EndpointReferenceType> epr = getEpr(simulationUuid, trustDelegation);
        unicoreJobStorage.downloadFile(epr, path, response, trustDelegation);
    }

    private Log log = LogFactory.getLog(UnicoreJob.class);
}

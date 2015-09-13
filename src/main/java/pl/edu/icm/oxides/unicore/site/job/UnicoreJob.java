package pl.edu.icm.oxides.unicore.site.job;

import de.fzj.unicore.uas.client.JobClient;
import de.fzj.unicore.uas.client.StorageClient;
import de.fzj.unicore.wsrflite.xmlbeans.BaseFault;
import de.fzj.unicore.wsrflite.xmlbeans.client.BaseWSRFClient;
import eu.unicore.security.etd.TrustDelegation;
import eu.unicore.util.httpclient.IClientConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.unigrids.services.atomic.types.GridFileType;
import org.unigrids.services.atomic.types.ProtocolType;
import org.w3.x2005.x08.addressing.EndpointReferenceType;
import pl.edu.icm.oxides.config.GridOxidesConfig;
import pl.edu.icm.oxides.portal.model.SimulationGridFile;
import pl.edu.icm.oxides.unicore.GridClientHelper;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class UnicoreJob {
    private final UnicoreJobListing unicoreJobListing;
    private final UnicoreJobEprCache unicoreJobEprCache;
    private final UnicoreJobProperties unicoreJobProperties;

    private final GridClientHelper clientHelper;
    private final GridOxidesConfig oxidesConfig;
    private final ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    public UnicoreJob(UnicoreJobListing unicoreJobListing,
                      UnicoreJobEprCache unicoreJobEprCache,
                      UnicoreJobProperties unicoreJobProperties,
                      GridClientHelper clientHelper,

                      GridOxidesConfig oxidesConfig,
                      ThreadPoolTaskExecutor taskExecutor) {
        this.unicoreJobListing = unicoreJobListing;
        this.unicoreJobEprCache = unicoreJobEprCache;
        this.unicoreJobProperties = unicoreJobProperties;
        this.clientHelper = clientHelper;
        this.oxidesConfig = oxidesConfig;
        this.taskExecutor = taskExecutor;
    }

    // TODO: do some cache at this level
    public List<UnicoreJobEntity> retrieveSiteResourceList(TrustDelegation trustDelegation) {
        log.trace("Retrieving job list for user " + trustDelegation.getCustodianDN());
        return getJobsList(trustDelegation)
                .parallelStream()
                .map(epr -> unicoreJobProperties.retrieveJobProperties(epr, trustDelegation))
                .filter(Objects::nonNull)
                .map(unicoreJobProperties::translateJobPropertiesToUnicoreJobEntity)
                .filter(unicoreJobEntity -> unicoreJobEntity.getFullName().startsWith(oxidesConfig.getJobPrefix()))
                .sorted((o1, o2) -> o1.getTimestamp() > o2.getTimestamp() ? -1 : 1)
                .collect(Collectors.toList());
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
        List<EndpointReferenceType> jobsListing = unicoreJobListing.retrieveSiteResourceList(trustDelegation);
        jobsListing.forEach(epr -> {
                    String uriString = epr.getAddress().getStringValue();
                    UUID uuid = UUID.fromString(uriString.substring(uriString.length() - 36));
                    unicoreJobEprCache.put(String.valueOf(uuid), epr);
                }
        );
        return jobsListing;
    }

    @CacheEvict(value = "unicoreSessionJobList", key = "#trustDelegation.custodianDN")
    public void destroyJob(UUID simulationUuid, TrustDelegation trustDelegation) {
        taskExecutor.execute(() -> {
            getEpr(simulationUuid, trustDelegation).ifPresent(epr -> {
                try {
                    new BaseWSRFClient(epr, clientHelper.createClientConfiguration(trustDelegation))
                            .destroy();
                } catch (Exception e) {
                    log.error("Could not destroy job <" + epr.getAddress().getStringValue() + ">", e);
                }
            });
        });
    }

    public UnicoreJobDetailsEntity retrieveJobDetails(UUID simulationUuid, TrustDelegation trustDelegation) {
        String uuidString = String.valueOf(simulationUuid);
        return retrieveSiteResourceList(trustDelegation)
                .stream()
                .filter(unicoreJobEntity -> unicoreJobEntity.getUri().endsWith(uuidString))
                .findFirst()
                .map(jobEntity -> unicoreJobProperties.retrieveJobProperties(jobEntity.getEpr(), trustDelegation))
                .filter(Objects::nonNull)
                .map(unicoreJobProperties::translateJobPropertiesToUnicoreJobDetailsEntity)
                .orElseThrow(() -> new RuntimeException("Problem while getting details!"));
    }

    public List<SimulationGridFile> listJobFiles(UUID simulationUuid,
                                                 Optional<String> path,
                                                 TrustDelegation trustDelegation) {
        // FIXME: testing and temporary implementation
        Optional<StorageClient> storageClient = getStorageClient(simulationUuid, trustDelegation);

        List<SimulationGridFile> listing = new ArrayList<>();
        storageClient.ifPresent(client -> {
            try {
                GridFileType[] gridFileTypes = client.listDirectory(path.orElse("/"));

                listing.addAll(
                        Arrays.stream(gridFileTypes)
                                .map(this::toSimulationGridFile)
                                .collect(Collectors.toList())
                );
            } catch (BaseFault baseFault) {
                baseFault.printStackTrace();
            }
        });

        Comparator<SimulationGridFile> simulationGridFileComparator = (o1, o2) -> {
            if (o1.isDirectory() != o2.isDirectory()) {
                return o2.isDirectory() ? 1 : -1;
            }
            return o1.getPath().compareToIgnoreCase(o2.getPath());
        };
        return listing.stream()
                .sorted(simulationGridFileComparator)
                .collect(Collectors.toList());
    }

    private SimulationGridFile toSimulationGridFile(GridFileType gridFileType) {
        String filePath = gridFileType.getPath();
        if (gridFileType.getIsDirectory()) {
            filePath += "/";
        }

        String filename = Paths.get(filePath)
                .getFileName()
                .toString();
        int indexOf = filename.lastIndexOf('.');
        String extension = indexOf > 0 ? filename.substring(indexOf) : null;

        return new SimulationGridFile(
                filePath,
                gridFileType.getIsDirectory(),
                extension);
    }

    public void downloadJobFile(UUID simulationUuid,
                                Optional<String> path,
                                HttpServletResponse response,
                                TrustDelegation trustDelegation) {
        // FIXME: testing and temporary implementation
        Optional<StorageClient> storageClient = getStorageClient(simulationUuid, trustDelegation);

        path.ifPresent(filePath -> {
            storageClient.ifPresent(sms -> {
                try {
                    sms.getImport(filePath, ProtocolType.BFT, ProtocolType.RBYTEIO)
                            .readAllData(response.getOutputStream());
                } catch (Exception e) {
                    log.error("Problem with file download!", e);
                }
            });
        });

        try {
            response.flushBuffer();
        } catch (IOException e) {
            log.error("problem with buffer flush", e);
        }
    }

    private Optional<StorageClient> getStorageClient(UUID simulationUuid, TrustDelegation trustDelegation) {
        IClientConfiguration clientConfiguration = clientHelper.createClientConfiguration(trustDelegation);
        return retrieveSiteResourceList(trustDelegation)
                .stream()
                .filter(unicoreJobEntity -> unicoreJobEntity.getUri().endsWith(simulationUuid.toString()))
                .map(unicoreJobEntity -> {
                    try {
                        return new JobClient(unicoreJobEntity.getEpr(), clientConfiguration);
                    } catch (Exception e) {
                        log.warn("ERROR creating TSSClient", e);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .map(jobClient -> {
                    try {
                        return jobClient.getUspaceClient();
                    } catch (Exception e) {
                        log.warn("Error getting UspaceClient", e);
                    }
                    return null;
                })
                .findAny();
    }

    private Log log = LogFactory.getLog(UnicoreJob.class);
}

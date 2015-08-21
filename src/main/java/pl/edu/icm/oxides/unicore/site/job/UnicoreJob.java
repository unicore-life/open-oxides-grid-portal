package pl.edu.icm.oxides.unicore.site.job;

import de.fzj.unicore.uas.client.JobClient;
import de.fzj.unicore.uas.client.StorageClient;
import de.fzj.unicore.uas.client.TSFClient;
import de.fzj.unicore.uas.client.TSSClient;
import de.fzj.unicore.wsrflite.xmlbeans.BaseFault;
import eu.unicore.util.httpclient.IClientConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.unigrids.services.atomic.types.GridFileType;
import org.unigrids.services.atomic.types.ProtocolType;
import org.w3.x2005.x08.addressing.EndpointReferenceType;
import pl.edu.icm.oxides.simulation.model.SimulationGridFile;
import pl.edu.icm.oxides.unicore.GridClientHelper;
import pl.edu.icm.oxides.unicore.central.tss.UnicoreSite;
import pl.edu.icm.oxides.unicore.central.tss.UnicoreSiteEntity;
import pl.edu.icm.oxides.user.AuthenticationSession;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
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

    public List<SimulationGridFile> listJobFiles(UUID simulationUuid, Optional<String> path, AuthenticationSession authenticationSession) {
        // FIXME: testing and temporary implementation
        Optional<StorageClient> storageClient = getStorageClient(simulationUuid, authenticationSession);

        List<SimulationGridFile> listing = new ArrayList<>();
        storageClient.ifPresent(client -> {
            try {
                GridFileType[] gridFileTypes = client.listDirectory(path.orElse("/"));
                for (GridFileType gridFileType : gridFileTypes) {
                    String filePath = gridFileType.getPath();
                    if (gridFileType.getIsDirectory()) {
                        filePath += "/";
                    }
                    listing.add(new SimulationGridFile(filePath, gridFileType.getIsDirectory()));
                }
            } catch (BaseFault baseFault) {
                baseFault.printStackTrace();
            }
        });
        return listing;
    }

    public void downloadJobFile(UUID simulationUuid, Optional<String> path, HttpServletResponse response, AuthenticationSession authenticationSession) {
        // FIXME: testing and temporary implementation
        Optional<StorageClient> storageClient = getStorageClient(simulationUuid, authenticationSession);

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

    private Optional<StorageClient> getStorageClient(UUID simulationUuid, AuthenticationSession authenticationSession) {
        IClientConfiguration clientConfiguration = clientHelper.createClientConfiguration(authenticationSession);
        return retrieveSiteResourceList(authenticationSession)
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

    private List<EndpointReferenceType> toAccessibleTargetSystems(UnicoreSiteEntity unicoreSiteEntity,
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

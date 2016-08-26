package pl.edu.icm.oxides.unicore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.icm.oxides.portal.model.OxidesSimulation;
import pl.edu.icm.oxides.portal.security.PortalAccess;
import pl.edu.icm.oxides.portal.security.PortalAccessHelper;
import pl.edu.icm.oxides.unicore.central.UnicoreBroker;
import pl.edu.icm.oxides.unicore.central.UnicoreBroker.BrokerJobType;
import pl.edu.icm.oxides.unicore.site.job.UnicoreJob;
import pl.edu.icm.oxides.user.OxidesPortalGridSession;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.UUID;

import static java.util.Optional.ofNullable;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;
import static pl.edu.icm.oxides.portal.security.PortalAccess.VALID;

@Component
public class UnicoreGridResources {
    private final UnicoreJob jobHandler;
    private final UnicoreBroker unicoreBroker;
    private final GridFileUploader fileUploader;
    private final CachingResourcesManager cachingResourcesManager;
    private final PortalAccessHelper accessHelper;

    @Autowired
    public UnicoreGridResources(UnicoreJob jobHandler,
                                UnicoreBroker unicoreBroker,
                                GridFileUploader fileUploader,
                                CachingResourcesManager cachingResourcesManager,
                                PortalAccessHelper accessHelper) {
        this.jobHandler = jobHandler;
        this.unicoreBroker = unicoreBroker;
        this.fileUploader = fileUploader;
        this.cachingResourcesManager = cachingResourcesManager;
        this.accessHelper = accessHelper;
    }

    public ResponseEntity<List> listUserJobs(OxidesPortalGridSession oxidesPortalGridSession) {
        PortalAccess portalAccess = accessHelper.determineSessionAccess(oxidesPortalGridSession);
        if (portalAccess == VALID) {
            return ok(jobHandler.retrieveSiteResourceList(oxidesPortalGridSession.getSelectedTrustDelegation()));
        }
        return notValidResponse(portalAccess);
    }

    public ResponseEntity<Void> submitScriptWorkAssignment(OxidesSimulation simulation,
                                                           OxidesPortalGridSession oxidesPortalGridSession) {
        PortalAccess portalAccess = accessHelper.determineSessionAccess(oxidesPortalGridSession);
        if (portalAccess == VALID) {
            unicoreBroker.submitBrokeredJob(BrokerJobType.SCRIPT, simulation, oxidesPortalGridSession);
            cachingResourcesManager.reinitializeAfterSubmission(oxidesPortalGridSession.getSelectedTrustDelegation());
            return ResponseEntity.noContent().build();
        }
        return notValidResponse(portalAccess);
    }

    public ResponseEntity<Void> submitQuantumEspressoWorkAssignment(OxidesSimulation simulation,
                                                                    OxidesPortalGridSession oxidesPortalGridSession) {
        PortalAccess portalAccess = accessHelper.determineSessionAccess(oxidesPortalGridSession);
        if (portalAccess == VALID) {
            unicoreBroker.submitBrokeredJob(BrokerJobType.QUANTUM_ESPRESSO, simulation, oxidesPortalGridSession);
            cachingResourcesManager.reinitializeAfterSubmission(oxidesPortalGridSession.getSelectedTrustDelegation());
            return ResponseEntity.noContent().build();
        }
        return notValidResponse(portalAccess);
    }

    public ResponseEntity<List> listUserJobFiles(UUID simulationUuid,
                                                 String path,
                                                 OxidesPortalGridSession oxidesPortalGridSession) {
        PortalAccess portalAccess = accessHelper.determineSessionAccess(oxidesPortalGridSession);
        if (portalAccess == VALID) {
            return ok(jobHandler.retrieveJobFilesListing(simulationUuid,
                    ofNullable(path),
                    oxidesPortalGridSession.getSelectedTrustDelegation()));
        }
        return notValidResponse(portalAccess);
    }

    public ResponseEntity<Void> downloadUserJobFile(UUID simulationUuid,
                                                    String path,
                                                    HttpServletResponse response,
                                                    OxidesPortalGridSession oxidesPortalGridSession) {
        PortalAccess portalAccess = accessHelper.determineSessionAccess(oxidesPortalGridSession);
        if (portalAccess == VALID) {
            jobHandler.downloadJobFile(simulationUuid,
                    ofNullable(path),
                    response,
                    oxidesPortalGridSession.getSelectedTrustDelegation());
            return ok().build();
        }
        return notValidResponse(portalAccess);
    }

    public ResponseEntity<String> uploadFile(MultipartFile file, OxidesPortalGridSession oxidesPortalGridSession) {
        PortalAccess portalAccess = accessHelper.determineSessionAccess(oxidesPortalGridSession);
        if (portalAccess == VALID) {
            return ok(fileUploader.uploadFileToGrid(file, oxidesPortalGridSession));
        }
        return notValidResponse(portalAccess);
    }

    public ResponseEntity<Void> destroyUserJob(UUID simulationUuid, OxidesPortalGridSession oxidesPortalGridSession) {
        PortalAccess portalAccess = accessHelper.determineSessionAccess(oxidesPortalGridSession);
        if (portalAccess == VALID) {
            jobHandler.destroySiteResource(simulationUuid, oxidesPortalGridSession.getSelectedTrustDelegation());
            return noContent().build();
        }
        return notValidResponse(portalAccess);
    }

    private <T> ResponseEntity<T> notValidResponse(PortalAccess portalAccess) {
        switch (portalAccess) {
            case PAGE_UNAUTHORIZED:
                return status(UNAUTHORIZED).body(null);
            default:
                return status(FORBIDDEN).body(null);
        }
    }
}

package pl.edu.icm.oxides.unicore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.icm.oxides.portal.model.OxidesSimulation;
import pl.edu.icm.oxides.unicore.central.broker.UnicoreBroker;
import pl.edu.icm.oxides.unicore.site.job.UnicoreJob;
import pl.edu.icm.oxides.user.AuthenticationSession;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.UUID;

import static java.util.Optional.ofNullable;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@Component
public class UnicoreGridResources {
    private final UnicoreJob jobHandler;
    private final UnicoreBroker unicoreBroker;
    private final GridFileUploader fileUploader;
    private final CachingResourcesManager cachingResourcesManager;

    @Autowired
    public UnicoreGridResources(UnicoreJob jobHandler,
                                UnicoreBroker unicoreBroker,
                                GridFileUploader fileUploader,
                                CachingResourcesManager cachingResourcesManager) {
        this.jobHandler = jobHandler;
        this.unicoreBroker = unicoreBroker;
        this.fileUploader = fileUploader;
        this.cachingResourcesManager = cachingResourcesManager;
    }

    public ResponseEntity<List> listUserJobs(AuthenticationSession authenticationSession) {
        if (isValidAuthenticationSession(authenticationSession)) {
            return ok(jobHandler.retrieveSiteResourceList(authenticationSession.getSelectedTrustDelegation()));
        }
        return unauthorizedResponse();
    }

    public ResponseEntity<Void> submitWorkAssignment(OxidesSimulation simulation,
                                                     AuthenticationSession authenticationSession) {
        if (isValidAuthenticationSession(authenticationSession)) {
            unicoreBroker.submitBrokeredJob(simulation, authenticationSession);
            cachingResourcesManager.reinitializeAfterSubmission(authenticationSession.getSelectedTrustDelegation());
            return ResponseEntity.noContent().build();
        }
        return unauthorizedResponse();
    }

    public ResponseEntity<List> listUserJobFiles(UUID simulationUuid,
                                                 String path,
                                                 AuthenticationSession authenticationSession) {
        if (isValidAuthenticationSession(authenticationSession)) {
            return ok(jobHandler.retrieveJobFilesListing(simulationUuid,
                    ofNullable(path),
                    authenticationSession.getSelectedTrustDelegation()));
        }
        return unauthorizedResponse();
    }

    public ResponseEntity<Void> downloadUserJobFile(UUID simulationUuid,
                                                    String path,
                                                    HttpServletResponse response,
                                                    AuthenticationSession authenticationSession) {
        if (isValidAuthenticationSession(authenticationSession)) {
            jobHandler.downloadJobFile(simulationUuid,
                    ofNullable(path),
                    response,
                    authenticationSession.getSelectedTrustDelegation());
            return ok().build();
        }
        return unauthorizedResponse();
    }

    public ResponseEntity<String> uploadFile(MultipartFile file, AuthenticationSession authenticationSession) {
        if (isValidAuthenticationSession(authenticationSession)) {
            return ok(fileUploader.uploadFileToGrid(file, authenticationSession));
        }
        return unauthorizedResponse();
    }

    public ResponseEntity<Void> destroyUserJob(UUID simulationUuid, AuthenticationSession authenticationSession) {
        if (isValidAuthenticationSession(authenticationSession)) {
            jobHandler.destroySiteResource(simulationUuid, authenticationSession.getSelectedTrustDelegation());
            return noContent().build();
        }
        return unauthorizedResponse();
    }

    private boolean isValidAuthenticationSession(AuthenticationSession authenticationSession) {
        return authenticationSession != null
                && authenticationSession.getTrustDelegations() != null
                && authenticationSession.getTrustDelegations().size() > 0;
    }

    private <T> ResponseEntity<T> unauthorizedResponse() {
        return status(UNAUTHORIZED).body(null);
    }
}

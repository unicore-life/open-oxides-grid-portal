package pl.edu.icm.oxides.unicore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import pl.edu.icm.oxides.simulation.model.OxidesSimulation;
import pl.edu.icm.oxides.unicore.central.broker.UnicoreBroker;
import pl.edu.icm.oxides.unicore.central.tss.UnicoreSite;
import pl.edu.icm.oxides.unicore.site.job.UnicoreJob;
import pl.edu.icm.oxides.unicore.site.resource.UnicoreResource;
import pl.edu.icm.oxides.unicore.site.storage.UnicoreSiteStorage;
import pl.edu.icm.oxides.user.AuthenticationSession;

import java.util.List;
import java.util.UUID;

import static java.util.Optional.ofNullable;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@Component
public class UnicoreGridResources {
    private final UnicoreSite siteHandler;
    private final UnicoreSiteStorage storageHandler;
    private final UnicoreJob jobHandler;
    private final UnicoreResource resourceHandler;
    private final UnicoreBroker unicoreBroker;

    @Autowired
    public UnicoreGridResources(UnicoreSite siteHandler,
                                UnicoreSiteStorage storageHandler,
                                UnicoreJob jobHandler,
                                UnicoreResource resourceHandler,
                                UnicoreBroker unicoreBroker) {
        this.siteHandler = siteHandler;
        this.storageHandler = storageHandler;
        this.jobHandler = jobHandler;
        this.resourceHandler = resourceHandler;
        this.unicoreBroker = unicoreBroker;
    }

    public ResponseEntity<List> listUserSites(AuthenticationSession authenticationSession) {
        if (isValidAuthenticationSession(authenticationSession)) {
            return ok(siteHandler.retrieveServiceList(authenticationSession));
        }
        return unauthorizedResponse();
    }

    public ResponseEntity<List> listUserStorages(AuthenticationSession authenticationSession) {
        if (isValidAuthenticationSession(authenticationSession)) {
            return ok(storageHandler.retrieveSiteResourceList(authenticationSession));
        }
        return unauthorizedResponse();
    }

    public ResponseEntity<List> listUserJobs(AuthenticationSession authenticationSession) {
        if (isValidAuthenticationSession(authenticationSession)) {
            return ok(jobHandler.retrieveSiteResourceList(authenticationSession));
        }
        return unauthorizedResponse();
    }

    public ResponseEntity<List> listUserResources(AuthenticationSession authenticationSession) {
        if (isValidAuthenticationSession(authenticationSession)) {
            return ok(resourceHandler.retrieveSiteResourceList(authenticationSession));
        }
        return unauthorizedResponse();
    }

    public ResponseEntity<Void> submitSimulation(OxidesSimulation simulation, AuthenticationSession authenticationSession) {
        if (isValidAuthenticationSession(authenticationSession)) {
            unicoreBroker.submitBrokeredJob(simulation, authenticationSession);
            return ok(null);
        }
        return unauthorizedResponse();
    }

    public ResponseEntity<List> listUserJobFiles(UUID simulationUuid, String path, AuthenticationSession authenticationSession) {
        if (isValidAuthenticationSession(authenticationSession)) {
            return ok(jobHandler.listJobFiles(simulationUuid, ofNullable(path), authenticationSession));
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

package pl.edu.icm.oxides.open;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import pl.edu.icm.oxides.config.OpenOxidesConfig;
import pl.edu.icm.oxides.open.model.Oxide;
import pl.edu.icm.oxides.user.AuthenticationSession;

import java.io.IOException;
import java.util.List;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.ResponseEntity.status;
import static pl.edu.icm.oxides.open.FileResourceLoader.getResourceString;

@Service
public class OpenOxidesResources {
    private final OpenOxidesConfig openOxidesConfig;
    private final FileResourceLoader fileResourceLoader;
    private final OpenOxidesResults openOxidesResults;
    private final HttpHeaders responseHeaders;

    @Autowired
    public OpenOxidesResources(OpenOxidesConfig openOxidesConfig,
                               FileResourceLoader fileResourceLoader,
                               OpenOxidesResults openOxidesResults) {
        this.openOxidesConfig = openOxidesConfig;
        this.fileResourceLoader = fileResourceLoader;
        this.openOxidesResults = openOxidesResults;

        responseHeaders = new HttpHeaders();
        responseHeaders.setAccessControlAllowOrigin("*");
    }

    public List<Oxide> getOpenOxidesResults() {
        return openOxidesResults.getResultOxides();
    }

    public ResponseEntity<String> getParticleParameters(String name, AuthenticationSession authenticationSession) {
        if (isValidAuthenticationSession(authenticationSession)) {
            Resource resource = fileResourceLoader.getResource("classpath:data/" + name + ".json");
            if (!resource.exists()) {
                return serviceResponse(NOT_FOUND);
            }

            try {
                return ResponseEntity.status(OK)
                        .headers(responseHeaders)
                        .body(getResourceString(resource));
            } catch (IOException e) {
                log.error("Could not get particle parameters data", e);
                return serviceResponse(INTERNAL_SERVER_ERROR);
            }
        }
        return serviceResponse(UNAUTHORIZED);
    }

    private <T> ResponseEntity<T> serviceResponse(HttpStatus httpStatus) {
        return status(httpStatus).headers(responseHeaders).body(null);
    }

    private boolean isValidAuthenticationSession(AuthenticationSession authenticationSession) {
        return authenticationSession != null
                && authenticationSession.isGroupMember(openOxidesConfig.getGroupName());
    }

    private Log log = LogFactory.getLog(OpenOxidesResources.class);
}

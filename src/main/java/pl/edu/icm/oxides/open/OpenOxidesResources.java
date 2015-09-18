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
import pl.edu.icm.oxides.user.AuthenticationSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.ResponseEntity.status;

@Service
public class OpenOxidesResources {
    private final OpenOxidesConfig openOxidesConfig;
    private final FileResourceLoader fileResourceLoader;
    private final HttpHeaders okResponseHeaders;

    @Autowired
    public OpenOxidesResources(OpenOxidesConfig openOxidesConfig, FileResourceLoader fileResourceLoader) {
        this.openOxidesConfig = openOxidesConfig;
        this.fileResourceLoader = fileResourceLoader;

        okResponseHeaders = new HttpHeaders();
        okResponseHeaders.add(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
    }

    public ResponseEntity<String> getParticleParameters(String name, AuthenticationSession authenticationSession) {
        if (isValidAuthenticationSession(authenticationSession)) {
            Resource resource = fileResourceLoader.getResource("classpath:data/" + name + ".json");
            if (!resource.exists()) {
                return serviceResponse(NOT_FOUND);
            }

            try {
                return ResponseEntity.status(OK)
                        .headers(okResponseHeaders)
                        .body(getJsonString(resource));
            } catch (IOException e) {
                log.error("Could not get particle parameters data", e);
                return serviceResponse(INTERNAL_SERVER_ERROR);
            }
        }
        return serviceResponse(UNAUTHORIZED);
    }

    private String getJsonString(Resource resource) throws IOException {
        String output = "";
        InputStream is = resource.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        String line;
        while ((line = br.readLine()) != null) {
            output += line;
        }
        br.close();
        return output;
    }

    private <T> ResponseEntity<T> serviceResponse(HttpStatus httpStatus) {
        return status(httpStatus).body(null);
    }

    private boolean isValidAuthenticationSession(AuthenticationSession authenticationSession) {
        return authenticationSession != null
                && authenticationSession.isGroupMember(openOxidesConfig.getGroupName());
    }

    private Log log = LogFactory.getLog(OpenOxidesResources.class);

    private static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
}

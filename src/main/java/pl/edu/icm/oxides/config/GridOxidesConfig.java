package pl.edu.icm.oxides.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "gridOxides")
public class GridOxidesConfig {
    private String applicationName;
    private String applicationVersion;

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApplicationVersion() {
        return applicationVersion;
    }

    public void setApplicationVersion(String applicationVersion) {
        this.applicationVersion = applicationVersion;
    }
}

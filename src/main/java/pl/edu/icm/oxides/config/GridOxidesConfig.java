package pl.edu.icm.oxides.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "gridOxides")
public class GridOxidesConfig {
    private String applicationName;
    private String applicationVersion;
    private String jobPrefix;
    private String accessGroup;
    private String resultsUrl;

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

    public String getJobPrefix() {
        return jobPrefix;
    }

    public void setJobPrefix(String jobPrefix) {
        this.jobPrefix = jobPrefix;
    }

    public String getAccessGroup() {
        return accessGroup;
    }

    public void setAccessGroup(String accessGroup) {
        this.accessGroup = accessGroup;
    }

    public String getResultsUrl() {
        return resultsUrl;
    }

    public void setResultsUrl(String resultsUrl) {
        this.resultsUrl = resultsUrl;
    }
}

package pl.edu.icm.oxides.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "feeds")
public class FeedsConfig {
    @NestedConfigurationProperty
    private List<FeedProperties> sources;

    public List<FeedProperties> getSources() {
        return sources;
    }

    public void setSources(List<FeedProperties> sources) {
        this.sources = sources;
    }
}

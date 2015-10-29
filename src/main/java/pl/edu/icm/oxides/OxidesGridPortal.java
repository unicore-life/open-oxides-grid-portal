package pl.edu.icm.oxides;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableConfigurationProperties
@EnableScheduling
@SpringBootApplication
public class OxidesGridPortal {

    public static void main(String[] args) throws Exception {
        SpringApplication application = new SpringApplication(OxidesGridPortal.class);
        application.setShowBanner(false);
        application.run(args);
    }
}

package pl.edu.icm.oxides.open;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pl.edu.icm.oxides.config.GridOxidesConfig;
import pl.edu.icm.oxides.open.model.Oxide;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
class OpenOxidesResults {
    private final GridOxidesConfig gridOxidesConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final List<Oxide> resultOxides = new ArrayList<>();

    @Autowired
    OpenOxidesResults(GridOxidesConfig gridOxidesConfig, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.gridOxidesConfig = gridOxidesConfig;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    List<Oxide> getResultOxides() {
        return resultOxides;
    }

    @Scheduled(fixedDelay = SIX_HOURS_IN_MILLIS)
    public void updateOpenOxidesResults() {
        String resultsUrl = gridOxidesConfig.getResultsUrl();
        Objects.nonNull(resultsUrl);

        String response = restTemplate.getForObject(resultsUrl, String.class);
        try {
            Oxide[] oxides = objectMapper.readValue(response, Oxide[].class);

            resultOxides.clear();
            resultOxides.addAll(Arrays.asList(oxides));

            log.info(String.format("Fetched results data from Open Oxides URL: %s", resultsUrl));
        } catch (IOException e) {
            log.error("Problem with parsing Open Oxides results response!", e);
        }
    }

    private Log log = LogFactory.getLog(OpenOxidesResults.class);

    private static final long SIX_HOURS_IN_MILLIS = 6L * 60L * 60L * 1000L;
}

package pl.edu.icm.oxides;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.icm.oxides.portal.model.OxidesSimulation;

import java.util.Arrays;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
public class AngularTestingController {

    @RequestMapping(value = "/testing", method = GET)
    public List<String> getItemList() {
        return Arrays.asList("Test1", "Test2", "Test3");
    }

    @RequestMapping(value = "/unauthorized", method = GET)
    public ResponseEntity<String> entityResponse() {
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @RequestMapping(value = "/testing-post", method = POST,
            consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public OxidesSimulation submitSimulation(@RequestBody OxidesSimulation simulation) {
        return new OxidesSimulation("DONE", "project", "queue", "memory", "nodes", "cpus", "reservation");
    }

    @RequestMapping(value = "/testing-long-call", method = GET, produces = TEXT_PLAIN_VALUE)
    public ResponseEntity<String> respondAfterMoment() throws InterruptedException {
        Thread.sleep(5000);
        return ResponseEntity.ok("DONE");
    }
}

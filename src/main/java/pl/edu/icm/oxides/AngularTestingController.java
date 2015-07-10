package pl.edu.icm.oxides;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.icm.oxides.simulation.model.OxidesSimulation;

import java.util.Arrays;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
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
        return new OxidesSimulation("DONE");
    }
}

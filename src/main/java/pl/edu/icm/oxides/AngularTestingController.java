package pl.edu.icm.oxides;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
public class AngularTestingController {

    @RequestMapping(value = "/testing", method = RequestMethod.GET)
    public List<String> getItemList() {
        return Arrays.asList("Test1", "Test2", "Test3");
    }

    @RequestMapping(value = "/unauthorized", method = RequestMethod.GET)
    public ResponseEntity<String> entityResponse() {
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
}

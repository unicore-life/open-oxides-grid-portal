package pl.edu.icm.oxides;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import pl.edu.icm.oxides.authn.UnprocessableResponseException;
import pl.edu.icm.oxides.portal.security.OxidesForbiddenException;

import java.time.Instant;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@ControllerAdvice
public class ErrorsHandler {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity genericExceptionHandler(Exception ex) {
        log.error("Error!", ex);
        return createErrorResponse(INTERNAL_SERVER_ERROR, ex);
    }

    @ExceptionHandler({UnprocessableResponseException.class, OxidesForbiddenException.class})
    public ModelAndView handleUnprocessableResponseException(RuntimeException ex) {
        ModelAndView modelAndView = new ModelAndView("errors/default");
        modelAndView.addObject("commonName", "");
        modelAndView.addObject("exceptionMessage", ex.getMessage());
        return modelAndView;
    }

    private ResponseEntity createErrorResponse(HttpStatus httpStatus, Exception ex) {
        ErrorMessage errorMessage = new ErrorMessage(ex);
        return ResponseEntity.status(httpStatus).body(errorMessage);
    }

    private static final class ErrorMessage {
        @JsonProperty("code")
        private final String code;
        @JsonProperty("message")
        private final String message;
        @JsonProperty("timestamp")
        private final String timestamp;

        private ErrorMessage(Exception ex) {
            this.code = ex.getClass().getSimpleName();
            this.message = ex.getMessage();
            this.timestamp = ISO_INSTANT.format(Instant.now());
        }
    }

    private Log log = LogFactory.getLog(ErrorsHandler.class);
}

package pl.edu.icm.oxides;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@ControllerAdvice
public class OxidesErrorsHandler {

    //    @ExceptionHandler(NoHandlerFoundException.class)
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity genericExceptionHandler(Exception ex) {
        log.error("Error!", ex);
        ErrorMessage errorMessage = new ErrorMessage(ex.getClass().getSimpleName(), ex.getMessage());
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(errorMessage);
    }

    private static final class ErrorMessage {
        private final String code;
        private final String message;
        private final String timestamp;

        private ErrorMessage(String code, String message) {
            this.code = code;
            this.message = message;
            this.timestamp = String.valueOf(System.currentTimeMillis());
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }

    private Log log = LogFactory.getLog(OxidesErrorsHandler.class);
}

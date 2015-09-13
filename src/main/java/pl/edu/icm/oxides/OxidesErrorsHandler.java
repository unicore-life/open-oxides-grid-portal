package pl.edu.icm.oxides;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import pl.edu.icm.oxides.authn.UnprocessableResponseException;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@ControllerAdvice
public class OxidesErrorsHandler {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity genericExceptionHandler(Exception ex) {
        log.error("Error!", ex);
        return createErrorResponse(INTERNAL_SERVER_ERROR, ex);
    }

    @ExceptionHandler(UnprocessableResponseException.class)
    @ResponseBody
    public ResponseEntity handleUnprocessableResponseException(UnprocessableResponseException ex) {
        return createErrorResponse(UNPROCESSABLE_ENTITY, ex);
    }

    private ResponseEntity createErrorResponse(HttpStatus httpStatus, Exception ex) {
        ErrorMessage errorMessage = new ErrorMessage(ex.getClass().getSimpleName(), ex.getMessage());
        return ResponseEntity.status(httpStatus).body(errorMessage);
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

        public String getTimestamp() {
            return timestamp;
        }
    }

    private Log log = LogFactory.getLog(OxidesErrorsHandler.class);
}

package pl.edu.icm.oxides.authn;

public class UnprocessableResponseException extends RuntimeException {
    UnprocessableResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}

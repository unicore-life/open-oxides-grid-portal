package pl.edu.icm.oxides.portal.security;

public class OxidesForbiddenException extends RuntimeException {
    public OxidesForbiddenException(String message) {
        super(message);
    }
}

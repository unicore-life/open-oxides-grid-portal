package pl.edu.icm.oxides.unicore.central;

public class UnicoreSpringException extends RuntimeException {
    public UnicoreSpringException(String message) {
        super(message);
    }

    public UnicoreSpringException(Throwable cause) {
        super(cause);
    }
}

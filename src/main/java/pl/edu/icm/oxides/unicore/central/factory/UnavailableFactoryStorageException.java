package pl.edu.icm.oxides.unicore.central.factory;

public class UnavailableFactoryStorageException extends RuntimeException {
    public UnavailableFactoryStorageException(String message) {
        super(message);
    }

    public UnavailableFactoryStorageException(Throwable e) {
        super(e);
    }
}

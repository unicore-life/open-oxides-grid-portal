package pl.edu.icm.oxides.unicore.central.broker;

public class UnavailableBrokerException extends RuntimeException {
    public UnavailableBrokerException(Throwable e) {
        super(e);
    }
}

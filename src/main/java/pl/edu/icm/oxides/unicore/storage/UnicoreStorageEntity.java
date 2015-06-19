package pl.edu.icm.oxides.unicore.storage;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.w3.x2005.x08.addressing.EndpointReferenceType;

public class UnicoreStorageEntity {
    private final String uri;

    public UnicoreStorageEntity(String uri) {
        this.uri = uri;
    }

    public String getUri() {
        return uri;
    }

    @JsonIgnore
    public EndpointReferenceType getEpr() {
        EndpointReferenceType epr = EndpointReferenceType.Factory.newInstance();
        epr.addNewAddress().setStringValue(this.uri);
        return epr;
    }
}

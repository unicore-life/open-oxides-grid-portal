package pl.edu.icm.oxides.unicore.site.storage;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.w3.x2005.x08.addressing.EndpointReferenceType;

import java.io.Serializable;

public class UnicoreSiteStorageEntity implements Serializable {
    private final String uri;

    public UnicoreSiteStorageEntity(EndpointReferenceType uri) {
        this.uri = uri.getAddress().getStringValue();
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

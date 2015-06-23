package pl.edu.icm.oxides.unicore.site.resource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.w3.x2005.x08.addressing.EndpointReferenceType;

import java.io.Serializable;

public class UnicoreResourceEntity implements Serializable {
    private final String name;

    public UnicoreResourceEntity(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @JsonIgnore
    public EndpointReferenceType getEpr() {
        EndpointReferenceType epr = EndpointReferenceType.Factory.newInstance();
        epr.addNewAddress().setStringValue(this.name);
        return epr;
    }
}

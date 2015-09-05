package pl.edu.icm.oxides.portal.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

public class OxidesSimulation {
    @NotNull
    private final String name;

    @JsonCreator
    public OxidesSimulation(@JsonProperty("name") String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return String.format("OxidesSimulation{name='%s'}", name);
    }
}

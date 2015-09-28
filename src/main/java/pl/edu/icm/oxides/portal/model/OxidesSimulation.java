package pl.edu.icm.oxides.portal.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OxidesSimulation {
    @NotNull
    @Size(max = SIMULATION_RESOURCE_MAX_LENGTH)
    private final String name;
    @Size(max = SIMULATION_RESOURCE_MAX_LENGTH)
    private final String project;
    @Size(max = SIMULATION_RESOURCE_MAX_LENGTH)
    private final String queue;
    private final Integer memory;
    private final Integer nodes;
    private final Integer cpus;
    @Size(max = SIMULATION_RESOURCE_MAX_LENGTH)
    private final String reservation;
    @Size(max = SIMULATION_RESOURCE_MAX_LENGTH)
    private final String property;
    @NotNull
    @Size(max = SIMULATION_SCRIPT_MAX_LENGTH)
    private final String script;
    private final List<String> files;

    @JsonCreator
    public OxidesSimulation(@JsonProperty("name") String name,
                            @JsonProperty("project") String project,
                            @JsonProperty("queue") String queue,
                            @JsonProperty("memory") Integer memory,
                            @JsonProperty("nodes") Integer nodes,
                            @JsonProperty("cpus") Integer cpus,
                            @JsonProperty("reservation") String reservation,
                            @JsonProperty("property") String property,
                            @JsonProperty("script") String script,
                            @JsonProperty("files") List<String> files) {
        this.name = name;
        this.project = project;
        this.queue = queue;
        this.memory = memory;
        this.nodes = nodes;
        this.cpus = cpus;
        this.reservation = reservation;
        this.property = property;
        this.script = script;
        this.files = files == null ? Collections.emptyList() : Collections.unmodifiableList(files);
    }

    public String getName() {
        return name;
    }

    public String getProject() {
        return project;
    }

    public String getQueue() {
        return queue;
    }

    public Integer getMemory() {
        return memory;
    }

    public Integer getNodes() {
        return nodes;
    }

    public Integer getCpus() {
        return cpus;
    }

    public String getReservation() {
        return reservation;
    }

    public String getProperty() {
        return property;
    }

    public String getScript() {
        return script;
    }

    public List<String> getFiles() {
        return files;
    }

    @Override
    public String toString() {
        return String.format("OxidesSimulation{name='%s', project='%s', queue='%s', memory=%d, " +
                        "nodes=%d, cpus=%d, reservation='%s', property='%s', script='%s', files=%s}",
                name, project, queue, memory, nodes, cpus, reservation, property, script, files);
    }

    private static final int SIMULATION_RESOURCE_MAX_LENGTH = 32;
    private static final int SIMULATION_SCRIPT_MAX_LENGTH = 1024 * 1024;
}

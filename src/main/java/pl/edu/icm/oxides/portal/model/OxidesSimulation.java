package pl.edu.icm.oxides.portal.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

public class OxidesSimulation {
    @NotNull
    private final String name;
    @NotNull
    private final String project;
    private final String queue;
    private final String memory;
    private final String nodes;
    private final String cpus;

    @JsonCreator
    public OxidesSimulation(@JsonProperty("name") String name,
                            @JsonProperty("project") String project,
                            @JsonProperty("queue") String queue,
                            @JsonProperty("memory") String memory,
                            @JsonProperty("nodes") String nodes,
                            @JsonProperty("cpus") String cpus) {
        this.name = name;
        this.project = project;
        this.queue = queue;
        this.memory = memory;
        this.nodes = nodes;
        this.cpus = cpus;
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

    public String getMemory() {
        return memory;
    }

    public String getNodes() {
        return nodes;
    }

    public String getCpus() {
        return cpus;
    }

    @Override
    public String toString() {
        return "OxidesSimulation{" +
                "name='" + name + '\'' +
                ", project='" + project + '\'' +
                ", queue='" + queue + '\'' +
                ", memory='" + memory + '\'' +
                ", nodes='" + nodes + '\'' +
                ", cpus='" + cpus + '\'' +
                '}';
    }
}

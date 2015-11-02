package pl.edu.icm.oxides.unicore.simulation;

import java.util.List;

public class WorkAssignmentDescription {
    private final String name;
    private final String project;
    private final String queue;
    private final Integer memory;
    private final Integer nodes;
    private final Integer cpus;
    private final String reservation;
    private final String property;
    private final List<WorkAssignmentFile> files;

    public WorkAssignmentDescription(String name,
                                     String project,
                                     String queue,
                                     Integer memory,
                                     Integer nodes,
                                     Integer cpus,
                                     String reservation,
                                     String property,
                                     List<WorkAssignmentFile> files) {
        this.name = name;
        this.project = project;
        this.queue = queue;
        this.memory = memory;
        this.nodes = nodes;
        this.cpus = cpus;
        this.reservation = reservation;
        this.property = property;
        this.files = files;
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

    public List<WorkAssignmentFile> getFiles() {
        return files;
    }
}

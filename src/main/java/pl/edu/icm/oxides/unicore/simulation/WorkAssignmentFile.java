package pl.edu.icm.oxides.unicore.simulation;

public class WorkAssignmentFile {
    private final String sourceName;
    private final String destinationName;

    public WorkAssignmentFile(String sourceName, String destinationName) {
        this.sourceName = sourceName;
        this.destinationName = destinationName;
    }

    public String getSourceName() {
        return sourceName;
    }

    public String getDestinationName() {
        return destinationName;
    }
}

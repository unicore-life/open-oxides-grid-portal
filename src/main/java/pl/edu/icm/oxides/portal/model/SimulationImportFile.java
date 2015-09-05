package pl.edu.icm.oxides.portal.model;

public class SimulationImportFile {
    private final String name;
    private final long size;
    private final String uri;

    public SimulationImportFile(String name, long size, String uri) {
        this.name = name;
        this.size = size;
        this.uri = uri;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public String getUri() {
        return uri;
    }
}

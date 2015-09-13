package pl.edu.icm.oxides.portal.model;

public class SimulationGridFile {
    private final String path;
    private final boolean directory;
    private final String type;

    public SimulationGridFile(String path, boolean directory, String type) {
        this.path = path;
        this.directory = directory;
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public boolean isDirectory() {
        return directory;
    }

    public String getType() {
        return type;
    }
}

package pl.edu.icm.oxides.portal.model;

public class SimulationGridFile {
    private final String path;
    private final boolean directory;

    public SimulationGridFile(String path, boolean directory) {
        this.path = path;
        this.directory = directory;
    }

    public String getPath() {
        return path;
    }

    public boolean isDirectory() {
        return directory;
    }
}

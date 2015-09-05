package pl.edu.icm.oxides.user;

import de.fzj.unicore.uas.client.StorageClient;
import pl.edu.icm.oxides.portal.model.SimulationImportFile;

import java.util.HashMap;
import java.util.Map;

public class UserResources {
    private final Map<String, SimulationImportFile> importFiles = new HashMap<>();
    private StorageClient storageClient;

    public Map<String, SimulationImportFile> getImportFiles() {
        return importFiles;
    }

    public synchronized StorageClient getStorageClient() {
        return storageClient;
    }

    public synchronized void setStorageClient(StorageClient storageClient) {
        this.storageClient = storageClient;
    }
}

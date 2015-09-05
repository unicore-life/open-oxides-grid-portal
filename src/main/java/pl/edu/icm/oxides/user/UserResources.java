package pl.edu.icm.oxides.user;

import de.fzj.unicore.uas.client.StorageClient;

import java.util.HashMap;
import java.util.Map;

public class UserResources {
    private final Map<String, String> importFiles = new HashMap<>();
    private StorageClient storageClient;

    public Map<String, String> getImportFiles() {
        return importFiles;
    }

    public synchronized StorageClient getStorageClient() {
        return storageClient;
    }

    public synchronized void setStorageClient(StorageClient storageClient) {
        this.storageClient = storageClient;
    }
}

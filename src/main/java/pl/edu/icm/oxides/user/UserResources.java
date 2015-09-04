package pl.edu.icm.oxides.user;

import de.fzj.unicore.uas.client.StorageClient;

public class UserResources {
    private StorageClient storageClient;

    public synchronized StorageClient getStorageClient() {
        return storageClient;
    }

    public synchronized void setStorageClient(StorageClient storageClient) {
        this.storageClient = storageClient;
    }
}

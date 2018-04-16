package pl.edu.icm.oxides.unicore;

import de.fzj.unicore.uas.client.StorageClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.icm.oxides.user.OxidesPortalGridSession;
import pl.edu.icm.oxides.user.UserResources;
import pl.edu.icm.unicore.spring.util.GridClientHelper;

import java.io.InputStream;

import static org.unigrids.services.atomic.types.ProtocolType.BFT;
import static org.unigrids.services.atomic.types.ProtocolType.RBYTEIO;

@Service
public class GridFileUploader {
    private final GridClientHelper clientHelper;

    @Autowired
    public GridFileUploader(GridClientHelper clientHelper) {
        this.clientHelper = clientHelper;
    }

    public String uploadFileToGrid(MultipartFile file, OxidesPortalGridSession oxidesPortalGridSession) {
        UserResources userResources = oxidesPortalGridSession.getResources();

        String fileName = file.getName();
        String originalFilename = file.getOriginalFilename();
        log.info(String.format("Storing file with name '%s' and original filename '%s'", fileName, originalFilename));

        String name = originalFilename;
        if (file.isEmpty()) {
            final String message = "File upload failed of '" + name + "': it was empty!";

            log.warn(message);
            return message;
        }

        try {
            importFileToGrid(
                    userResources.getStorageClient(),
                    name,
                    file.getInputStream()
            );
            final String message = "File '" + name + "' uploaded successfully!";

            log.debug(message);
            return message;
        } catch (Exception e) {
            final String message = "File upload failed of '" + name + "'!";

            log.error(message, e);
            return message + " Reason: " + e.getMessage();
        }
    }

    public void importFileToGrid(StorageClient storageClient, String filename, InputStream source) throws Exception {
        storageClient
                .getImport(filename, BFT, RBYTEIO)
                .writeAllData(source);
    }

    private Log log = LogFactory.getLog(GridFileUploader.class);
}

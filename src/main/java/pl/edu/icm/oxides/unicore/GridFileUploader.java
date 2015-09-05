package pl.edu.icm.oxides.unicore;

import de.fzj.unicore.uas.client.StorageClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.unigrids.services.atomic.types.ProtocolType;
import pl.edu.icm.oxides.portal.model.SimulationImportFile;
import pl.edu.icm.oxides.user.AuthenticationSession;
import pl.edu.icm.oxides.user.UserResources;

import java.io.InputStream;

@Service
public class GridFileUploader {
    private final GridClientHelper clientHelper;

    @Autowired
    public GridFileUploader(GridClientHelper clientHelper) {
        this.clientHelper = clientHelper;
    }

    public String uploadFileToGrid(MultipartFile file, AuthenticationSession authenticationSession) {
        UserResources userResources = authenticationSession.getResources();

        String fileName = file.getName();
        String originalFilename = file.getOriginalFilename();
        log.info(String.format("Storing file with name '%s' and original filename '%s'", fileName, originalFilename));

        String name = originalFilename;
        if (!file.isEmpty()) {
            try {
//                byte[] bytes = file.getBytes();
//                BufferedOutputStream stream =
//                        new BufferedOutputStream(new FileOutputStream(new File(name)));
//                stream.write(bytes);
//                stream.close();

                String uri = importFileToGrid(
                        userResources.getStorageClient(),
                        name,
                        file.getInputStream()
                );

                userResources.getImportFiles().put(name, new SimulationImportFile(name, file.getSize(), uri));

                return "You successfully uploaded " + name + "!";
            } catch (Exception e) {
                return "You failed to upload " + name + " => " + e.getMessage();
            }
        } else {
            return "You failed to upload " + name + " because the file was empty.";
        }
    }

    private String importFileToGrid(StorageClient storageClient, String filename, InputStream source) throws Exception {
//        InputStream source = new ByteArrayInputStream(content.getBytes());
        storageClient.getImport(filename, ProtocolType.BFT,
                ProtocolType.RBYTEIO).writeAllData(source);
        return ProtocolType.BFT + ":"
                + storageClient.getEPR().getAddress().getStringValue()
                + "#" + filename;
    }

    private Log log = LogFactory.getLog(GridFileUploader.class);
}

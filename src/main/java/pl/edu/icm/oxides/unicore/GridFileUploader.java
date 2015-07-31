package pl.edu.icm.oxides.unicore;

import eu.unicore.util.httpclient.IClientConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.icm.oxides.user.AuthenticationSession;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

@Service
public class GridFileUploader {
    private final GridClientHelper clientHelper;

    @Autowired
    public GridFileUploader(GridClientHelper clientHelper) {
        this.clientHelper = clientHelper;
    }

    public String uploadFileToGrid(MultipartFile file, String uri, AuthenticationSession authenticationSession) {
        IClientConfiguration clientConfiguration = clientHelper.createClientConfiguration(authenticationSession);

        String fileName = file.getName();
        String originalFilename = file.getOriginalFilename();
        log.info(String.format("Storing file with name '%s' and original filename '%s'", fileName, originalFilename));

        String name = originalFilename;
        if (!file.isEmpty()) {
            try {
                byte[] bytes = file.getBytes();
                BufferedOutputStream stream =
                        new BufferedOutputStream(new FileOutputStream(new File(name)));
                stream.write(bytes);
                stream.close();
                return "You successfully uploaded " + name + "!";
            } catch (Exception e) {
                return "You failed to upload " + name + " => " + e.getMessage();
            }
        } else {
            return "You failed to upload " + name + " because the file was empty.";
        }
    }

    private Log log = LogFactory.getLog(GridFileUploader.class);
}

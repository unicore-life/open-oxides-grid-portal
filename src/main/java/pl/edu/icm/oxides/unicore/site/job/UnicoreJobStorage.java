package pl.edu.icm.oxides.unicore.site.job;

import de.fzj.unicore.uas.client.StorageClient;
import de.fzj.unicore.wsrflite.xmlbeans.BaseFault;
import eu.unicore.security.etd.TrustDelegation;
import eu.unicore.util.httpclient.IClientConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.unigrids.services.atomic.types.GridFileType;
import org.unigrids.services.atomic.types.ProtocolType;
import org.w3.x2005.x08.addressing.EndpointReferenceType;
import pl.edu.icm.oxides.portal.model.SimulationGridFile;
import pl.edu.icm.oxides.unicore.GridClientHelper;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;

@Component
class UnicoreJobStorage {
    private final UnicoreJobOperations unicoreJobOperations;
    private final GridClientHelper clientHelper;

    @Autowired
    UnicoreJobStorage(UnicoreJobOperations unicoreJobOperations, GridClientHelper clientHelper) {
        this.unicoreJobOperations = unicoreJobOperations;
        this.clientHelper = clientHelper;
    }

    public List<SimulationGridFile> listFiles(Optional<EndpointReferenceType> epr,
                                              Optional<String> path,
                                              TrustDelegation trustDelegation) {
        Optional<StorageClient> storageClient = getStorageClient(epr, trustDelegation);

        List<SimulationGridFile> listing = new ArrayList<>();
        storageClient.ifPresent(client -> {
            try {
                GridFileType[] gridFileTypes = client.listDirectory(path.orElse("/"));

                listing.addAll(
                        Arrays.stream(gridFileTypes)
                                .map(this::toSimulationGridFile)
                                .collect(Collectors.toList())
                );
            } catch (BaseFault baseFault) {
                baseFault.printStackTrace();
            }
        });

        Comparator<SimulationGridFile> simulationGridFileComparator = (o1, o2) -> {
            if (o1.isDirectory() != o2.isDirectory()) {
                return o2.isDirectory() ? 1 : -1;
            }
            return o1.getPath().compareToIgnoreCase(o2.getPath());
        };
        return listing.stream()
                .sorted(simulationGridFileComparator)
                .collect(Collectors.toList());
    }

    public void downloadFile(Optional<EndpointReferenceType> epr,
                             Optional<String> path,
                             HttpServletResponse response,
                             TrustDelegation trustDelegation) {
        Optional<StorageClient> storageClient = getStorageClient(epr, trustDelegation);

        path.ifPresent(filePath -> {
            storageClient.ifPresent(sms -> {
                avoidXssFromStoredFileByContentDispositionHeader(filePath, response);
                try {
                    sms.getImport(filePath, ProtocolType.BFT, ProtocolType.RBYTEIO)
                            .readAllData(response.getOutputStream());
                } catch (Exception e) {
                    log.error("Problem with file download!", e);
                }
            });
        });

        try {
            response.flushBuffer();
        } catch (IOException e) {
            log.error("problem with buffer flush", e);
        }
    }

    private void avoidXssFromStoredFileByContentDispositionHeader(String filePath, HttpServletResponse response) {
        String filename = Paths.get(filePath)
                .getFileName()
                .toString();
        String filenameWithRemovedNonPrintableChars = filename
                .replaceAll("[^\\p{Print}]", "");
//                .replaceAll("[\\p{C}\\p{Z}]", "");
        response.addHeader(CONTENT_DISPOSITION, "attachment; filename=\"" + filenameWithRemovedNonPrintableChars + "\"");
    }

//    public static void main(String[] args) {
//        // FIXME: remove me!
//        String q = "ad fa.dff";
//        String qq = q.replaceAll("[^\\p{Print}]", "");
//        System.out.println(qq);
//        String qqq = q.replaceAll("[\\p{C}\\p{Z}]", "");
//        System.out.println(qqq);
//    }

    private SimulationGridFile toSimulationGridFile(GridFileType gridFileType) {
        String filePath = gridFileType.getPath();
        boolean isDirectory = gridFileType.getIsDirectory();
        if (isDirectory) {
            filePath += "/";
        }

        String extension = null;
        if (!isDirectory) {
            int lastIndexOfSlash = filePath.lastIndexOf('/');
            int lastIndexOfPeriod = filePath.lastIndexOf('.');
            log.info("processing file path: " + filePath + ", / - " + lastIndexOfSlash + ", . - " + lastIndexOfPeriod);

            String filename = Paths.get(filePath)
                    .getFileName()
                    .toString();
            int indexOf = filename.lastIndexOf('.');
            if (indexOf > 0) {
                extension = filename.substring(indexOf + 1);
            }
        }
        return new SimulationGridFile(filePath, isDirectory, extension);
    }

    private Optional<StorageClient> getStorageClient(Optional<EndpointReferenceType> simulationEpr, TrustDelegation trustDelegation) {
        IClientConfiguration clientConfiguration = clientHelper.createClientConfiguration(trustDelegation);
        return simulationEpr
                .map(epr -> unicoreJobOperations.retrieveJobProperties(epr, trustDelegation))
                .map(jobProperties -> jobProperties.getWorkingDirectoryReference())
                .map(endpointReferenceType -> {
                    try {
                        return new StorageClient(endpointReferenceType, clientConfiguration);
                    } catch (Exception e) {
                        log.error("Could not create storage client for simulation "
                                + simulationEpr, e);
                        return null;
                    }
                });
    }

    private Log log = LogFactory.getLog(UnicoreJobStorage.class);
}

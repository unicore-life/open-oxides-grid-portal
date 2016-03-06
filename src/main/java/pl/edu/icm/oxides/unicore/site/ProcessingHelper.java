package pl.edu.icm.oxides.unicore.site;

import de.fzj.unicore.uas.client.TSFClient;
import eu.unicore.util.httpclient.IClientConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3.x2005.x08.addressing.EndpointReferenceType;
import pl.edu.icm.unicore.spring.central.tss.UnicoreSiteEntity;

import java.util.List;

import static pl.edu.icm.unicore.spring.util.EndpointReferenceHelper.toEndpointReference;

public final class ProcessingHelper {
    private ProcessingHelper() {
    }

    public static List<EndpointReferenceType> toAccessibleTargetSystems(UnicoreSiteEntity unicoreSiteEntity,
                                                                        IClientConfiguration clientConfiguration) {
        final EndpointReferenceType endpointReference = toEndpointReference(unicoreSiteEntity.getUri());
        try {
            return new TSFClient(endpointReference, clientConfiguration)
                    .getAccessibleTargetSystems();
        } catch (Exception e) {
            LOGGER.warn(String.format("Could not get accessible target systems from site <%s>!",
                    unicoreSiteEntity.getUri()), e);
        }
        return null;
    }

    private static final Log LOGGER = LogFactory.getLog(ProcessingHelper.class);
}

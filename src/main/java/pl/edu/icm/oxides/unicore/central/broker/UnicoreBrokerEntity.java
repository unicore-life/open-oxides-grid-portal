package pl.edu.icm.oxides.unicore.central.broker;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.fzj.unicore.wsrflite.xmlbeans.WSUtilities;
import eu.unicore.security.wsutil.client.UnicoreWSClientFactory;
import eu.unicore.util.httpclient.IClientConfiguration;
import org.chemomentum.common.ws.IServiceOrchestrator;
import org.w3.x2005.x08.addressing.EndpointReferenceType;

import javax.security.auth.x500.X500Principal;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.Optional;

import static java.util.Optional.ofNullable;

public class UnicoreBrokerEntity implements Serializable {
    @JsonIgnore
    private final EndpointReferenceType epr;

    public UnicoreBrokerEntity(EndpointReferenceType epr) {
        this.epr = epr;
    }

    public String getUri() {
        return epr.getAddress().getStringValue();
    }

    @JsonIgnore
    public EndpointReferenceType getEpr() {
        return epr;
    }

    @JsonIgnore
    public Optional<IServiceOrchestrator> createBrokerClient(IClientConfiguration clientConfiguration) {
        try {
            return ofNullable(initializeClient(IServiceOrchestrator.class, getEpr(), clientConfiguration));
        } catch (MalformedURLException e) {
            throw new UnavailableBrokerException(e);
        }
    }

    private <T> T initializeClient(Class<T> clazz,
                                   EndpointReferenceType epr,
                                   IClientConfiguration clientConfig) throws MalformedURLException {
        String receiverDn = WSUtilities.extractServerIDFromEPR(epr);
        if (receiverDn != null) {
            clientConfig.getETDSettings().setReceiver(
                    new X500Principal(receiverDn));
        }
        return new UnicoreWSClientFactory(clientConfig)
                .createPlainWSProxy(clazz, epr
                        .getAddress()
                        .getStringValue());
        // TODO: think of retry feature (included in WSRFClientFactory)
//        return wsrfClientFactory.createPlainWSProxy(clazz, serviceEpr
//                .getAddress()
//                .getStringValue());
    }
}

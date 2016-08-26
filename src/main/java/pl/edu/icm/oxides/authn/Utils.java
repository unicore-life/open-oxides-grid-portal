package pl.edu.icm.oxides.authn;

import eu.unicore.samly2.SAMLConstants;
import eu.unicore.samly2.elements.NameID;
import eu.unicore.samly2.exceptions.SAMLValidationException;
import org.apache.commons.logging.Log;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.util.Base64;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import xmlbeans.org.oasis.saml2.assertion.NameIDType;

import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

final class Utils {

    private Utils() {
    }

    static void configureHttpResponse(HttpServletResponse response) {
        response.setContentType(String.format("%s; charset=%s", MediaType.TEXT_HTML, StandardCharsets.UTF_8));
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache,no-store,must-revalidate");
        response.setHeader(HttpHeaders.PRAGMA, "no-cache");
        response.setDateHeader(HttpHeaders.EXPIRES, -1);
    }

    static NameIDType convertDistinguishedNameToNameID(String distinguishedName) {
        return new NameID(distinguishedName, SAMLConstants.NFORMAT_DN)
                .getXBean();
    }

    static <T extends XmlObject> T decodeMessage(String message, Log log) throws SAMLValidationException {
        byte[] decoded = Base64.decode(message.getBytes());
        if (decoded == null) {
            throw new SAMLValidationException("The SAML message is not properly Base64 encoded");
        }
        String messageString = new String(decoded, StandardCharsets.UTF_8);
        log.trace(String.format("Decoded message: %s", messageString));
        try {
            return (T) T.Factory.parse(messageString);
        } catch (XmlException e) {
            throw new SAMLValidationException(e.getMessage());
        }
    }

    static final long DEFAULT_LOGOUT_REQUEST_VALIDITY_IN_MILLIS = 60000;
}

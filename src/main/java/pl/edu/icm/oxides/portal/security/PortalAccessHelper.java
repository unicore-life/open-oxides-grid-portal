package pl.edu.icm.oxides.portal.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.edu.icm.oxides.config.GridOxidesConfig;
import pl.edu.icm.oxides.user.AuthenticationSession;

import static pl.edu.icm.oxides.portal.security.PortalAccess.NO_TRUST_DELEGATION;
import static pl.edu.icm.oxides.portal.security.PortalAccess.PAGE_FORBIDDEN;
import static pl.edu.icm.oxides.portal.security.PortalAccess.PAGE_UNAUTHORIZED;
import static pl.edu.icm.oxides.portal.security.PortalAccess.VALID;

@Component
public class PortalAccessHelper {
    private final GridOxidesConfig oxidesConfig;

    @Autowired
    public PortalAccessHelper(GridOxidesConfig oxidesConfig) {
        this.oxidesConfig = oxidesConfig;
    }

    public PortalAccess determineSessionAccess(AuthenticationSession authenticationSession) {
        if (authenticationSession == null || authenticationSession.getTrustDelegations() == null) {
            return PAGE_UNAUTHORIZED;
        }
        if (!isPageNotForbidden(authenticationSession)) {
            return PAGE_FORBIDDEN;
        }
        if (authenticationSession.getTrustDelegations().isEmpty()) {
            return NO_TRUST_DELEGATION;
        }
        return VALID;
    }

    private boolean isPageNotForbidden(AuthenticationSession authenticationSession) {
        if (authenticationSession.isGroupMember(oxidesConfig.getAccessGroup())) {
            return true;
        }
        // Workaround for old Unity IDM version:
        //
        String name = authenticationSession.getCommonName();
        String oxidesAttribute = authenticationSession.getAttribute("oxides");

        return name != null && name.equals(oxidesAttribute);
    }
}

package pl.netia.troubleticket.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class TenantContext {

    private static final String TENANT_ID = "tenantId";

    public String getTenantId() {
        Jwt jwt = (Jwt) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        String tenantId = jwt.getClaimAsString(TENANT_ID);

        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalStateException(
                    "Token does not contain tenantId"
            );
        }

        return tenantId;
    }
}
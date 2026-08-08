package com.procure.common.audit;

import com.procure.security.SecurityUtils;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Spring Data Auditing — supplies the current authenticated user's email
 * as the auditor for created_by / updated_by fields.
 */
@Component("auditorAware")
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.ofNullable(SecurityUtils.getCurrentUserEmail())
                .or(() -> Optional.of("system"));
    }
}

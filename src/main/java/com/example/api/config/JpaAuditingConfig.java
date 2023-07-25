package com.example.api.config;

import com.example.api.model.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditingConfig {
    @Bean
    public AuditorAware<String> auditorAware(){
        return new AuditorAwareImpl();
    }

    public static class AuditorAwareImpl implements AuditorAware<String>{
        @Override
        @NonNull
        public Optional<String> getCurrentAuditor() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()
                    || authentication instanceof AnonymousAuthenticationToken){
                return Optional.empty();
//                return Optional.of("97299043-f36c-485f-b9a4-21f5948325ed");
            }

            User user = (User) authentication.getPrincipal();
            return Optional.ofNullable(((User) authentication.getPrincipal()).getUuid());
//            return Optional.of(authentication.getName());
        }
    }
}


//@Configuration
//@EnableJpaAuditing
//public class JpaAuditingConfig {
//
//    @Bean
//    public AuditorAware<Long> auditorProvider() {
//        return new SpringSecurityAuditAwareImpl();
//    }
//}
//
//class SpringSecurityAuditAwareImpl implements AuditorAware<String> {
//
//    @Override
//    public Optional<String> getCurrentAuditor() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//        if (authentication == null || !authentication.isAuthenticated()
//                || authentication instanceof AnonymousAuthenticationToken) {
//            return Optional.empty();
//        }
//
//        User user = (User) authentication.getPrincipal();
//
//        return Optional.ofNullable(user.getUuid());
//    }
//}
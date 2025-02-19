package com.czertainly.core.auth.oauth2;

import com.czertainly.api.model.core.auth.Resource;
import com.czertainly.api.model.core.logging.enums.*;
import com.czertainly.api.model.core.logging.enums.Module;
import com.czertainly.api.model.core.settings.SettingsSection;
import com.czertainly.api.model.core.settings.authentication.AuthenticationSettingsDto;
import com.czertainly.api.model.core.settings.authentication.OAuth2ProviderSettingsDto;
import com.czertainly.core.logging.LoggingHelper;
import com.czertainly.core.security.authn.CzertainlyAuthenticationException;
import com.czertainly.core.service.AuditLogService;
import com.czertainly.core.settings.SettingsCache;
import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.Duration;
import java.util.List;

@Component
public class CzertainlyJwtDecoder implements JwtDecoder {

    private static final Logger logger = LoggerFactory.getLogger(CzertainlyJwtDecoder.class);

    private AuditLogService auditLogService;

    @Autowired
    public void setAuditLogService(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        if (!isAuthenticationNeeded()) {
            return null;
        }
        String issuerUri;
        LoggingHelper.putActorInfoWhenNull(ActorType.USER, AuthMethod.TOKEN);
        try {
            issuerUri = SignedJWT.parse(token).getJWTClaimsSet().getIssuer();
        } catch (ParseException e) {
            String message = "Could not extract issuer from JWT token";
            auditLogService.log(Module.AUTH, Resource.USER, Operation.AUTHENTICATION, OperationResult.FAILURE, message);
            throw new CzertainlyAuthenticationException(message);
        }
        if (issuerUri == null) {
            String message = "Issuer URI is not present in JWT token";
            auditLogService.log(Module.AUTH, Resource.USER, Operation.AUTHENTICATION, OperationResult.FAILURE, message);
            throw new CzertainlyAuthenticationException(message);
        }

        AuthenticationSettingsDto authenticationSettings = SettingsCache.getSettings(SettingsSection.AUTHENTICATION);
        OAuth2ProviderSettingsDto providerSettings = authenticationSettings.getOAuth2Providers().values().stream().filter(p -> p.getIssuerUrl().equals(issuerUri)).findFirst().orElse(null);

        if (providerSettings == null) {
            String message = "No OAuth2 Provider with issuer URI '%s' configured for authentication with JWT token".formatted(issuerUri);
            auditLogService.log(Module.AUTH, Resource.USER, Operation.AUTHENTICATION, OperationResult.FAILURE, message);
            throw new CzertainlyAuthenticationException(message);
        }

        int skew = providerSettings.getSkew();
        List<String> audiences = providerSettings.getAudiences();

        NimbusJwtDecoder jwtDecoder = null;
        try {
            jwtDecoder = JwtDecoders.fromIssuerLocation(issuerUri);
        } catch (Exception e) {
            String message = "Could not authenticate user using JWT token: %s".formatted(e.getMessage());
            auditLogService.log(Module.AUTH, Resource.USER, Operation.AUTHENTICATION, OperationResult.FAILURE, message);
            throw e;
        }
        OAuth2TokenValidator<Jwt> clockSkewValidator = new JwtTimestampValidator(Duration.ofSeconds(skew));
        OAuth2TokenValidator<Jwt> audienceValidator = new DelegatingOAuth2TokenValidator<>();
        // Add audience validation
        if (!audiences.isEmpty()) {
            audienceValidator = new JwtClaimValidator<List<String>>("aud", aud -> aud.stream().anyMatch(audiences::contains));
        }

        OAuth2TokenValidator<Jwt> combinedValidator = JwtValidators.createDefaultWithValidators(List.of(new JwtIssuerValidator(issuerUri), clockSkewValidator, audienceValidator));
        jwtDecoder.setJwtValidator(combinedValidator);
        try {
            return jwtDecoder.decode(token);
        } catch (JwtException e) {
            String message = "Could not authenticate user using JWT token: %s".formatted(e.getMessage());
            auditLogService.log(Module.AUTH, Resource.USER, Operation.AUTHENTICATION, OperationResult.FAILURE, message);
            logger.error(message);
            throw e;
        }
    }

    private boolean isAuthenticationNeeded() {
        SecurityContext context = SecurityContextHolder.getContext();
        return (context == null || context.getAuthentication() == null || context.getAuthentication() instanceof AnonymousAuthenticationToken);
    }
}

package com.czertainly.core.auth.oauth2;

import com.czertainly.api.model.core.settings.authentication.AuthenticationSettingsDto;
import com.czertainly.api.model.core.settings.authentication.OAuth2ProviderSettingsDto;
import com.czertainly.api.model.core.settings.SettingsSection;
import com.czertainly.core.security.authn.CzertainlyAuthenticationException;
import com.czertainly.core.settings.SettingsCache;
import com.czertainly.core.util.OAuth2Constants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.List;

@Controller
public class LoginController {

    private static final String ERROR_ATTRIBUTE_NAME = "error";

    @GetMapping("/login")
    public String loginPage(Model model, @RequestParam(value = "redirect", required = false) String redirectUrl, HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "error", required = false) String error) {

        request.getSession().setAttribute(OAuth2Constants.SERVLET_CONTEXT_SESSION_ATTRIBUTE, ServletUriComponentsBuilder.fromCurrentContextPath().build().getPath());

        if (error != null) {
            model.addAttribute(ERROR_ATTRIBUTE_NAME, "An error occurred: " + error);
            request.getSession().invalidate();
            return ERROR_ATTRIBUTE_NAME;
        }

        String baseUrl = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .replacePath(null)
                .build()
                .toUriString();

        if (redirectUrl != null && !redirectUrl.isEmpty()) {
            request.getSession().setAttribute(OAuth2Constants.REDIRECT_URL_SESSION_ATTRIBUTE, baseUrl + redirectUrl);
        } else {
            model.addAttribute(ERROR_ATTRIBUTE_NAME, "No redirect URL provided");
            return ERROR_ATTRIBUTE_NAME;
        }

        AuthenticationSettingsDto authenticationSettings = SettingsCache.getSettings(SettingsSection.AUTHENTICATION);
        if (authenticationSettings.getOAuth2Providers().isEmpty()) return "no-login-options";

        // Because of validation of OAuth2ProviderSettingsDto, when client ID is null, all other client related properties will be null too, meaning this provider is only for JWT token validation
        List<OAuth2ProviderSettingsDto> oauth2Providers = authenticationSettings.getOAuth2Providers().values().stream().filter(dto -> dto.getClientId() != null).toList();
        if (oauth2Providers.size() == 1) {
            request.getSession().setMaxInactiveInterval(oauth2Providers.getFirst().getSessionMaxInactiveInterval());
            try {
                response.sendRedirect("oauth2/authorization/" + oauth2Providers.getFirst().getName());
            } catch (IOException e) {
                throw new CzertainlyAuthenticationException("Error when redirecting to OAuth2 Provider with name " + oauth2Providers.getFirst() + " : " + e.getMessage());
            }
        }

        model.addAttribute("providers", oauth2Providers.stream().map(OAuth2ProviderSettingsDto::getName).toList());
        return "login-options";
    }

    @GetMapping("/oauth2/authorization/{provider}/prepare")
    public void loginWithProvider(@PathVariable String provider, HttpServletResponse response, HttpServletRequest request) throws IOException {
        AuthenticationSettingsDto authenticationSettings = SettingsCache.getSettings(SettingsSection.AUTHENTICATION);
        OAuth2ProviderSettingsDto providerSettings = authenticationSettings.getOAuth2Providers().get(provider);

        if (providerSettings == null) {
            throw new CzertainlyAuthenticationException("Unknown OAuth2 Provider with name '%s'".formatted(provider));
        }

        request.getSession().setMaxInactiveInterval(providerSettings.getSessionMaxInactiveInterval());
        response.sendRedirect(ServletUriComponentsBuilder.fromCurrentContextPath().build().getPath() + "/oauth2/authorization/" + provider);
    }

}

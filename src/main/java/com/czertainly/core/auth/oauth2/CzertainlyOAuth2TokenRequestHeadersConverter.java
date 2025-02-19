package com.czertainly.core.auth.oauth2;

import com.czertainly.core.security.authn.CzertainlyAuthenticationException;
import com.czertainly.core.util.OAuth2Constants;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.endpoint.AbstractOAuth2AuthorizationGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2RefreshTokenGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.List;


/**
 * Customizing request headers can be improved after upgrading to <a href="https://docs.spring.io/spring-security/reference/servlet/oauth2/client/authorization-grants.html#oauth2-client-client-credentials-access-token-request">Spring Security 6.4.1</a>
 * In new version, RestClientRefreshTokenTokenResponseClient is available and custom headers can be added directly using its addHeadersConverter() method
 */
public final class CzertainlyOAuth2TokenRequestHeadersConverter<T extends AbstractOAuth2AuthorizationGrantRequest> implements Converter<T, HttpHeaders> {

    private static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON,
            StandardCharsets.UTF_8);

    private static final MediaType APPLICATION_FORM_URLENCODED_UTF8 = new MediaType(
            MediaType.APPLICATION_FORM_URLENCODED, StandardCharsets.UTF_8);

    private List<MediaType> accept = List.of(MediaType.APPLICATION_JSON);

    private MediaType contentType = MediaType.APPLICATION_FORM_URLENCODED;

    private boolean encodeClientCredentials = true;

    @Override
    public HttpHeaders convert(T grantRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(this.accept);
        headers.setContentType(this.contentType);
        ClientRegistration clientRegistration = grantRequest.getClientRegistration();
        if (ClientAuthenticationMethod.CLIENT_SECRET_BASIC.equals(clientRegistration.getClientAuthenticationMethod())) {
            String clientId = encodeClientCredentialIfRequired(clientRegistration.getClientId());
            String clientSecret = encodeClientCredentialIfRequired(clientRegistration.getClientSecret());
            headers.setBasicAuth(clientId, clientSecret);
        }

        if (clientRegistration.getRegistrationId().equals(OAuth2Constants.INTERNAL_OAUTH2_PROVIDER_RESERVED_NAME)) {
            URI issuerUrl;
            try {
                String issuerUrlString = SignedJWT.parse(((OAuth2RefreshTokenGrantRequest) grantRequest).getAccessToken().getTokenValue()).getJWTClaimsSet().getIssuer();
                issuerUrl = new URI(issuerUrlString);
            } catch (URISyntaxException | ParseException e) {
                throw new CzertainlyAuthenticationException("Could not parse issuer URL in token: " + e.getMessage());
            }
            headers.set("X-Forwarded-Host", issuerUrl.getHost());
            if (issuerUrl.getPort() > 0) headers.set("X-Forwarded-Port", String.valueOf(issuerUrl.getPort()));
            headers.set("X-Forwarded-Proto", issuerUrl.getScheme());
        }

        return headers;
    }

    private String encodeClientCredentialIfRequired(String clientCredential) {
        if (!this.encodeClientCredentials) {
            return clientCredential;
        }
        return URLEncoder.encode(clientCredential, StandardCharsets.UTF_8);
    }

    public void setEncodeClientCredentials(boolean encodeClientCredentials) {
        this.encodeClientCredentials = encodeClientCredentials;
    }

    public static <T extends AbstractOAuth2AuthorizationGrantRequest> CzertainlyOAuth2TokenRequestHeadersConverter<T> withCharsetUtf8() {
        CzertainlyOAuth2TokenRequestHeadersConverter<T> converter = new CzertainlyOAuth2TokenRequestHeadersConverter<>();
        converter.accept = List.of(APPLICATION_JSON_UTF8);
        converter.contentType = APPLICATION_FORM_URLENCODED_UTF8;
        return converter;
    }

}

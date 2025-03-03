package es.upm.api.configurations;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import es.upm.api.domain.model.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.UUID;

@Configuration
public class AuthorizationServerConfig {

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthorizationServerConfig(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {

        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                OAuth2AuthorizationServerConfigurer.authorizationServer();
        // Habilitar OIDC: expone endpoints como .well-known/openid-configuration, /userinfo, etc.
        authorizationServerConfigurer.oidc(Customizer.withDefaults());
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(Customizer.withDefaults())
                .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
                .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
                .with(authorizationServerConfigurer, Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        TokenSettings tokenSettings = TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofMinutes(60))
                .refreshTokenTimeToLive(Duration.ofDays(30))
                .build();
        RegisteredClient client =
                RegisteredClient.withId(UUID.randomUUID().toString())
                        .clientId("client")
                        .clientSecret(passwordEncoder.encode("client-secret"))
                        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                        .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                        .redirectUri("http://localhost:8080/login/oauth2/code/cliente-oidc")
                        .scope("admin")
                        .scope("manager")
                        .scope("operator")
                        .scope("customer")
                        .tokenSettings(tokenSettings)
                        .build();

        return new InMemoryRegisteredClientRepository(client);
    }

    // Flujo de funcionamiento
    //1º : Se accede a la ruta: http://localhost:8080/oauth2/authorize?response_type=code&client_id=client
    //2º se redirige a la ruta programada en el client con el code
    //3º Con el code, se solicita un token de acceso
    // $clientId = "client" & $clientSecret = "client-secret" & $authHeader = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("$clientId`:$clientSecret"))
    // $code = "BjCaMqVDZNhuguw5fHAz0MUJKzScK3NRa_vNYvV_WMOpotx4G10OKAHBCJsGLV6_9OTxNXNO-fsAsbwmWZqfrNC218IKMICeDUoWTnvNaZybU0AH81LLFX3_wmr9xUds"
    // $response = Invoke-RestMethod -Uri $tokenUrl -Method Post -Headers @{ "Authorization" = "Basic $authHeader"
    // "Content-Type" = "application/x-www-form-urlencoded"
    // } -Body "grant_type=authorization_code&code=$code"
    // $token = response.token
    // 4º se invoca un recurso
    // $apiUrl = http://localhost:8080/users
    // Invoke-RestMethod -Uri $apiUrl -Method Get -Headers @{"Authorization" = "Bearer $token"}

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        RSAKey rsaKey = generateRsa(); // Generas el par de claves
        JWKSet jwkSet = new JWKSet(rsaKey);
        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }

    private RSAKey generateRsa() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

            return new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyID(UUID.randomUUID().toString())
                    .build();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        // Usa valores por defecto; se puede personalizar con .issuer("...")
        return AuthorizationServerSettings.builder()
                .issuer("http://localhost:8080")
                .build();
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizerRoleByScope() {
        return context -> {
            if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
                String scope = context.getPrincipal().getAuthorities().stream()
                        .findFirst()
                        .map(GrantedAuthority::getAuthority)
                        .map(r -> r.substring(Role.PREFIX.length()).toLowerCase())
                        .orElse("");
                context.getClaims().claim("scope", scope);
            }
        };
    }

}


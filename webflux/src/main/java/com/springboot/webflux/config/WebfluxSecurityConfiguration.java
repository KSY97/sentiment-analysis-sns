package com.springboot.webflux.config;

import com.springboot.webflux.security.AuthenticationManager;
import com.springboot.webflux.security.SecurityContextRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;



import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class WebfluxSecurityConfiguration {

    private AuthenticationManager authenticationManager;
    private SecurityContextRepository securityContextRepository;

    public WebfluxSecurityConfiguration(AuthenticationManager authenticationManager,
                                        SecurityContextRepository securityContextRepository) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http.authorizeExchange((exchanges) -> exchanges
                        .pathMatchers("/member/signin", "/member/signup").permitAll()
                        .anyExchange().authenticated()).formLogin().disable().csrf()
                .disable().cors().and().exceptionHandling()
                .authenticationEntryPoint((swe, e) -> Mono
                        .fromRunnable(() -> swe.getResponse().setStatusCode(
                                HttpStatus.UNAUTHORIZED)))
                .accessDeniedHandler((swe, e) -> Mono.fromRunnable(() -> swe
                        .getResponse().setStatusCode(HttpStatus.FORBIDDEN)))
                .and().authenticationManager(authenticationManager)
                .securityContextRepository(securityContextRepository);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
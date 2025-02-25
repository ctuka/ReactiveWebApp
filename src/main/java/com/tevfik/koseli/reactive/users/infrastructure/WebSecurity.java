package com.tevfik.koseli.reactive.users.infrastructure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class WebSecurity {

    @Bean
    SecurityWebFilterChain httpSecurityFilterChain(ServerHttpSecurity http,
                                                   ReactiveAuthenticationManager authenticationManager) {
        return http
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.POST, "/users").permitAll()
                        .pathMatchers(HttpMethod.POST, "/login").permitAll()
                        .anyExchange()
                        .authenticated())
                .csrf(ServerHttpSecurity.CsrfSpec::disable) //this is for session based applications since
                // it is statefull microservice it is diabled
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)// Because we created our custon security apies we make it disable
                .authenticationManager(authenticationManager)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

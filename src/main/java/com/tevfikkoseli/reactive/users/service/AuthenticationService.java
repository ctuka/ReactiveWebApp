package com.tevfikkoseli.reactive.users.service;

import reactor.core.publisher.Mono;

import java.util.Map;

public interface AuthenticationService {
    Mono<Map<String, String>> authenticate(String username, String password);
}

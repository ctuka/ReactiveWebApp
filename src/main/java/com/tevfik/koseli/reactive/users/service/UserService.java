package com.tevfik.koseli.reactive.users.service;

import com.tevfik.koseli.reactive.users.presentation.model.CreateUserRequest;
import com.tevfik.koseli.reactive.users.presentation.model.UserRest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserService {

    Mono<UserRest> createUser(Mono<CreateUserRequest> createUserRequestMono);
    Mono<UserRest> getUserById(UUID id);
    Flux<UserRest> findAll(int page, int limit);
}

package com.tevfikkoseli.reactive.users.presentation;

import com.tevfikkoseli.reactive.users.presentation.model.CreateUserRequest;
import com.tevfikkoseli.reactive.users.presentation.model.UserRest;
import com.tevfikkoseli.reactive.users.service.UserService;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // This is for returning specific http status code for creating resources
    public Mono<ResponseEntity<UserRest>> createUser(@RequestBody @Valid CreateUserRequest createUserRequest) {
        //takes new creating user request and return response entity together with status of the request
        return userService.createUser(Mono.just
                        (createUserRequest))
                .map(userRest ->
                ResponseEntity
                        .status(HttpStatus.CREATED)
                        .location(URI.create("/users/" + userRest.getId()))
                        .body(userRest));

    }

    @GetMapping("/{userId}")
    //@PreAuthorize("authentication.principal.equals(#userId.toString() or hasRole('ROLE_ADMIN'))") //this is used before method executed
    @PostAuthorize("returnObject.body != null and (returnObject.body.id.toString().equals(authentication.principal))")
    public Mono<ResponseEntity<UserRest>> getUser(@PathVariable("userId") UUID userId,
                                                  @RequestParam(name = "include",required = false) String include,
                                                  @RequestHeader(name = "Authorization") String jwt) {

        return userService
                .getUserById(userId, include, jwt)
                .map(userRest -> ResponseEntity.status(HttpStatus.OK).body(userRest))
                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build()));

    }

    @GetMapping
    public Flux<UserRest> getUsers(@RequestParam(value = "page", defaultValue = "0") int page,
                                   @RequestParam(value = "limit", defaultValue = "50") int limit) {
//
        return userService.findAll(page, limit);
    }
    @GetMapping (value = "/stream" , produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<UserRest > streamUsers() {

        return userService.streamUser();
    }


}

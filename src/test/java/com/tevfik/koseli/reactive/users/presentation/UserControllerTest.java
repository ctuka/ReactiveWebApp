package com.tevfik.koseli.reactive.users.presentation;

import com.tevfik.koseli.reactive.users.infrastructure.TestSecurityConfig;
import com.tevfik.koseli.reactive.users.presentation.model.CreateUserRequest;
import com.tevfik.koseli.reactive.users.presentation.model.UserRest;
import com.tevfik.koseli.reactive.users.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@WebFluxTest(UserController.class)
@Import(TestSecurityConfig.class)
class UserControllerTest {

    @MockitoBean
    UserService userService;

    @Autowired
    WebTestClient webTestClient;

    @Test
    void testCreateUser_withValidRequest_returnsCreatedStatusAndUserDetails()
    {

        //Arrance

        CreateUserRequest  createUserRequest = new CreateUserRequest(
                "Tevfik",
                "Koseli",
                "tkoseli@hotmail.com",
                "123456789"
        );

        UUID userId = UUID.randomUUID();
        String expectedLocation = "/users/" + userId;
        UserRest expecteduserRest = new UserRest(
                userId,
                createUserRequest.getFirstName(),
                createUserRequest.getLastName(),
                createUserRequest.getEmail(), null
        );

        when(userService.createUser(Mockito.<Mono<CreateUserRequest>>any())).thenReturn(Mono.just(expecteduserRest));


        //Act

        webTestClient.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createUserRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().location(expectedLocation)
                .expectBody(UserRest.class)
                .value(( response ) -> {
                            assertEquals(expecteduserRest.getId(), response.getId());
                            assertEquals(expecteduserRest.getFirstName(), response.getFirstName());
                            assertEquals(expecteduserRest.getLastName(), response.getLastName());
                            assertEquals(expecteduserRest.getEmail(), response.getEmail());
                        });

        //Assert
        verify(userService, times(1)).createUser(Mockito.<Mono<CreateUserRequest>> any());



    }


    @Test
    void testCreateUser_whenServiceThrowsException_returnsInternalServerErrorWithExpectedStructure() {
        // Arrange
        CreateUserRequest validRequest = new CreateUserRequest(
                "Sergey",
                "Kargopolov",
                "user@example.com",
                "123456789"
        );

        when(userService.createUser(any())).thenReturn(Mono.error(new RuntimeException("Service error")));

        // Act & Assert
        webTestClient
                .post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validRequest)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
                .expectBody()
                .jsonPath("$.instance").isEqualTo("/users")
                .jsonPath("$.status").isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .jsonPath("$.detail").isEqualTo("Service error");

        verify(userService, times(1)).createUser(any());
    }

}
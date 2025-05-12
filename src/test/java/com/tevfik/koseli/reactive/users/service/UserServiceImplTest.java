package com.tevfik.koseli.reactive.users.service;

import com.tevfik.koseli.reactive.users.data.UserEntity;
import com.tevfik.koseli.reactive.users.data.UserRepository;
import com.tevfik.koseli.reactive.users.presentation.model.CreateUserRequest;
import com.tevfik.koseli.reactive.users.presentation.model.UserRest;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;


import java.util.UUID;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) //reson for this anatation make waokable mock anatation in this class
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository ;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private WebClient webClient;

    private Sinks.Many<UserRest> userSinks;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userSinks = Sinks.many().multicast().onBackpressureBuffer();
        userService = new UserServiceImpl(userRepository, passwordEncoder, userSinks, webClient);
    }
    @Test
    void testCreateUser_withValidRequest_returnsCreateUserDetails() {

        //Arrange
        CreateUserRequest request = new CreateUserRequest(
                "Tevfik",
                "Koseli",
                "tkoseli@hotmail.com",
                "123456789"
        );
        UserEntity savedEntity =  new UserEntity();
        savedEntity.setId(UUID.randomUUID());
        savedEntity.setFirstName(request.getFirstName());
        savedEntity.setLastName(request.getLastName());
        savedEntity.setEmail(request.getEmail());
        savedEntity.setPassword(request.getPassword());






        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userRepository.save(any(UserEntity.class))).thenReturn(Mono.just(savedEntity));
        //Act


        Mono<UserRest> result = userService.createUser(Mono.just(request));

        //Assert
        StepVerifier //used for reactive test assert
                .create(result)
                .expectNextMatches(userRest -> userRest.getId().equals(savedEntity.getId()) &&
                        userRest.getFirstName().equals(savedEntity.getFirstName()) &&
                        userRest.getLastName().equals(savedEntity.getLastName()) &&
                        userRest.getEmail().equals(savedEntity.getEmail()))
                .verifyComplete(); //make user reactive method complete successfully

        verify(userRepository, times(1)).save(any(UserEntity.class));


//            UserRest user = result.block();
//            assertEquals(savedEntity.getId(),user.getId());
//            assertEquals(savedEntity.getFirstName(), user.getFirstName());



    }

    @Test
    void testCreateUser_withValidRequest_EmitsEventToSink() {
        //Arrange
        CreateUserRequest request = new CreateUserRequest(
                "Tevfik",
                "Koseli",
                "tkoseli@hotmail.com",
                "123456789"
        );
        UserEntity savedEntity =  new UserEntity();
        savedEntity.setId(UUID.randomUUID());
        savedEntity.setFirstName("Habele");
        savedEntity.setLastName("Hubele");
        savedEntity.setEmail("hubtub@gmail.com");
        savedEntity.setPassword("encodedPassword");


        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userRepository.save(any(UserEntity.class))).thenReturn(Mono.just(savedEntity));
        //Act


        Flux<UserRest> sinkFlux = userSinks.asFlux();

        //Assert
        StepVerifier //used for reactive test assert
                .create(
                        userService.createUser(Mono.just(request))
                                .thenMany(userSinks.asFlux().take(1))
                )
                .expectNextMatches(userRest -> userRest.getId().equals(savedEntity.getId()) &&
                        userRest.getFirstName().equals(savedEntity.getFirstName()) &&
                        userRest.getLastName().equals(savedEntity.getLastName()) &&
                        userRest.getEmail().equals(savedEntity.getEmail()))
                .verifyComplete(); //make user reactive method complete successfully

        //verify(userRepository, times(1)).save(any(UserEntity.class));



    }

    @Test
    void testGetUserById_WithExistingUser_ReturnsUserRest() {
        //Arrange

        UUID userId = UUID.randomUUID();
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setFirstName("Sergey");
        userEntity.setLastName("Kargopolov");
        userEntity.setEmail("test@test.com");

        when(userRepository.findById(userId)).thenReturn(Mono.just(userEntity));

        //Act

        Mono<UserRest> result = userService.getUserById(userId, null, "jwt-token");
        //Assert

        StepVerifier.create(result)
                .expectNextMatches(userRest -> userRest.getId().equals(userId) &&
                        userRest.getFirstName().equals(userEntity.getFirstName()) &&
                        userRest.getLastName().equals(userEntity.getLastName()) &&
                        userRest.getEmail().equals(userEntity.getEmail()))
                .verifyComplete();
        //Verify that findById method was called once with the correct userId
        verify(userRepository, times(1)).findById(userId);

        //Verify that webClient is not invoked when include is null
        verify(webClient, never()).get();
    }
}
package com.tevfik.koseli.reactive.users.service;

import com.tevfik.koseli.reactive.users.data.UserEntity;
import com.tevfik.koseli.reactive.users.data.UserRepository;
import com.tevfik.koseli.reactive.users.presentation.model.CreateUserRequest;
import com.tevfik.koseli.reactive.users.presentation.model.UserRest;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Mono<UserRest> createUser(Mono<CreateUserRequest> createUserRequestMono) {

        return createUserRequestMono
                .flatMap(this::convertToEntity)
                .flatMap(userRepository::save)
                .mapNotNull(this::convertToRest);
    }

    @Override
    public Mono<UserRest> getUserById(UUID id) {
        return userRepository
                .findById(id)
                .mapNotNull(this::convertToRest);
    }

    @Override
    public Flux<UserRest> findAll(int page, int limit) {
        if (page>0) page = page - 1;
        Pageable pageable = PageRequest.of(page, limit);
        return userRepository.findAllBy(pageable)
                .mapNotNull(this::convertToRest);

    }

    private Mono<UserEntity> convertToEntity(CreateUserRequest createUserRequest) {
        return Mono.fromCallable(()-> {  //This code prevent blocking the thread of crypting of password
            UserEntity userEntity = new UserEntity();
            BeanUtils.copyProperties(createUserRequest, userEntity); // if the property names are the same you can use BeanUtils
            userEntity.setPassword(passwordEncoder.encode(createUserRequest.getPassword())); //initilazed bcrypted password to user entity for saving database
            return userEntity;
        }).subscribeOn(Schedulers.boundedElastic());

    }

    private UserRest convertToRest(UserEntity userEntity) {
        UserRest userRest = new UserRest();
        BeanUtils.copyProperties(userEntity, userRest);
        return userRest;
    }
}

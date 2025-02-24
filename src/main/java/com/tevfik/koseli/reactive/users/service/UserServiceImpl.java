package com.tevfik.koseli.reactive.users.service;

import com.tevfik.koseli.reactive.users.data.UserEntity;
import com.tevfik.koseli.reactive.users.data.UserRepository;
import com.tevfik.koseli.reactive.users.presentation.CreateUserRequest;
import com.tevfik.koseli.reactive.users.presentation.UserRest;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Mono<UserRest> createUser(Mono<CreateUserRequest> createUserRequestMono) {

        return createUserRequestMono
                .mapNotNull(this::convertToEntity)
                .flatMap(userRepository::save)
                .mapNotNull(this::convertToRest)
                .onErrorMap(
                        throwable -> {
                            if (throwable instanceof DuplicateKeyException) {
                                return new ResponseStatusException(HttpStatus.CONFLICT,
                                        throwable.getMessage());
                            } else if(throwable instanceof DataIntegrityViolationException) {
                                return new ResponseStatusException(HttpStatus.BAD_REQUEST, throwable.getMessage());
                            } else {
                                return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, throwable.getMessage());
                            }
                        });
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

    private UserEntity convertToEntity(CreateUserRequest createUserRequest) {
        UserEntity userEntity = new UserEntity();
        BeanUtils.copyProperties(createUserRequest, userEntity); // if the property names are the same you can use BeanUtils
        return userEntity;
    }

    private UserRest convertToRest(UserEntity userEntity) {
        UserRest userRest = new UserRest();
        BeanUtils.copyProperties(userEntity, userRest);
        return userRest;
    }
}

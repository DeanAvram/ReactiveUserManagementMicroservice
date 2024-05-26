package cloud.user.reactiveusermanagementmicroservice.services;

import cloud.user.reactiveusermanagementmicroservice.boundries.UserBoundary;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserService {

    Mono<UserBoundary> getUser(String email, String password);
    Flux<UserBoundary> getUsersByFilters(String email, String criteria, String value);
    Flux<UserBoundary> getAllUsers();
    Mono<UserBoundary> createUser(UserBoundary userBoundary);
    Mono<Void> updateUser(String email, String password, UserBoundary userBoundary);
    Mono<Void> deleteAllUsers();
}

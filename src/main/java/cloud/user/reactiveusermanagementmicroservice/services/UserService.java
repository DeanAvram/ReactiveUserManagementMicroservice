package cloud.user.reactiveusermanagementmicroservice.services;

import cloud.user.reactiveusermanagementmicroservice.boundries.UserBoundary;
import reactor.core.publisher.Mono;

public interface UserService {

    Mono<UserBoundary> createUser(UserBoundary userBoundary);
}

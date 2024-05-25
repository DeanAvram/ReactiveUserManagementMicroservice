package cloud.user.reactiveusermanagementmicroservice.controllers;

import cloud.user.reactiveusermanagementmicroservice.boundries.UserBoundary;
import cloud.user.reactiveusermanagementmicroservice.services.UserService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping(path = {"/people"})
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(
            produces = {MediaType.APPLICATION_JSON_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE}
    )
    public Mono<UserBoundary> createUser(
            @RequestBody UserBoundary user){
        return this.userService.createUser(user);
    }
}

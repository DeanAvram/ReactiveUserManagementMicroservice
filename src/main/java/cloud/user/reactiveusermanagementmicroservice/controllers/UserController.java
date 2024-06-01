package cloud.user.reactiveusermanagementmicroservice.controllers;

import cloud.user.reactiveusermanagementmicroservice.boundries.UserBoundary;
import cloud.user.reactiveusermanagementmicroservice.services.UserService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping(path = {"/people"})
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(
            path = {"/{email}"},
            produces = {MediaType.TEXT_EVENT_STREAM_VALUE}
    )
    public Mono<UserBoundary> getUser(@PathVariable("email") String email, @RequestParam("password") String password){
        return this.userService.getUser(email, password);
    }

    @GetMapping(
            produces = {MediaType.TEXT_EVENT_STREAM_VALUE}
    )
    public Flux<UserBoundary> getAllUsers(){
        return this.userService.getAllUsers();
    }

    @GetMapping(
            path = {"/{criteria}/{value}"},
            produces = {MediaType.TEXT_EVENT_STREAM_VALUE}
    )
    public Mono<UserBoundary> getUsersByFilters(@PathVariable("criteria") String criteria, @PathVariable("value") String value){
        return null;
    }

    @PostMapping(
            produces = {MediaType.APPLICATION_JSON_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE}
    )
    public Mono<UserBoundary> createUser(
            @RequestBody UserBoundary user){
        return this.userService.createUser(user);
    }

    @PutMapping("/{email}")
    public Mono<Void> updateUser(
            @PathVariable String email,
            @RequestParam String password,
            @RequestBody UserBoundary userBoundary) {
        return userService.updateUser(email, password, userBoundary);
    }

    @DeleteMapping
    public Mono<Void> deleteAllUsers(){
        return this.userService.deleteAllUsers();
    }
}

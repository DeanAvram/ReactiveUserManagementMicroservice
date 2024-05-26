package cloud.user.reactiveusermanagementmicroservice.services;

import cloud.user.reactiveusermanagementmicroservice.boundries.UserBoundary;
import cloud.user.reactiveusermanagementmicroservice.entities.UserEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class UserServiceImplementation implements UserService {

        private ReactiveUserCrud users;

        public UserServiceImplementation(ReactiveUserCrud users) {
            this.users = users;
        }


    @Override
    public Mono<UserBoundary> getUser(String email, String password) {
        return null;
    }

    @Override
    public Flux<UserBoundary> getUsersByFilters(String email, String criteria, String value) {
        return null;
    }

    @Override
    public Flux<UserBoundary> getAllUsers() {
        return null;
    }

    @Override
        public Mono<UserBoundary> createUser(UserBoundary userBoundary) {
            return Mono.just(userBoundary)
                    .map(user -> {
                        UserEntity entity = this.convertToEntity(user);
                        return entity;
                    })
                    .flatMap(this.users::save)
                    .map(this::convertToBoundary);
        }

    @Override
    public Mono<Void> updateUser(String email, String password, UserBoundary userBoundary) {
        return null;
    }

    @Override
    public Mono<Void> deleteAllUsers() {
        return this.users.deleteAll();
    }


    private UserBoundary convertToBoundary(UserEntity userEntity){
             UserBoundary userBoundary = new UserBoundary();
             userBoundary.setEmail(userEntity.getEmail());
             userBoundary.setName(userEntity.getName());
             userBoundary.setPassword(userEntity.getPassword());
             userBoundary.setBirthdate(userEntity.getBirthdate());
             userBoundary.setRole(userEntity.getRole());
             userBoundary.setAddress(userEntity.getAddress());
             return userBoundary;
        }

        private UserEntity convertToEntity(UserBoundary userBoundary) {
             UserEntity userEntity = new UserEntity();
             userEntity.setEmail(userBoundary.getEmail());
             userEntity.setName(userBoundary.getName());
             userEntity.setPassword(userBoundary.getPassword());
             userEntity.setBirthdate(userBoundary.getBirthdate());
             userEntity.setRole(userBoundary.getRole());
             userEntity.setAddress(userBoundary.getAddress());
             return userEntity;
        }
}

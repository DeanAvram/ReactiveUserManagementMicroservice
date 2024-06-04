package cloud.user.reactiveusermanagementmicroservice.services;

import cloud.user.reactiveusermanagementmicroservice.boundries.UserBoundary;
import cloud.user.reactiveusermanagementmicroservice.entities.UserEntity;
import cloud.user.reactiveusermanagementmicroservice.exceptions.InvalidCriteriaException;
import cloud.user.reactiveusermanagementmicroservice.exceptions.InvalidDateException;
import cloud.user.reactiveusermanagementmicroservice.exceptions.UserNotFoundException;
import cloud.user.reactiveusermanagementmicroservice.exceptions.WrongPasswordException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class UserServiceImplementation implements UserService {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private ReactiveUserCrud users;

   public UserServiceImplementation(ReactiveUserCrud users) {
       this.users = users;
   }


    @Override
    public Mono<UserBoundary> getUser(String email, String password) {
       return this.users.findById(email).map(this::convertToBoundary).map(Optional::get)
               .switchIfEmpty(Mono.error(new UserNotFoundException("User with email: " + email + " not found.")))
               .filter(user -> user.getPassword().equals(password))
               .map(this::removePassword)
               .switchIfEmpty(Mono.error(new WrongPasswordException("Wrong password.")));
    }

    @Override
    public Flux<UserBoundary> getUsersByCriteria(String criteria, String value) {
        return switch (criteria) {
            case ("email") -> users.findByEmail(value).map(this::convertToBoundary).map(Optional::get).map(this::removePassword);
            case ("country") -> users.findAllByAddress_Country(value).map(this::convertToBoundary).map(Optional::get).map(this::removePassword);
            case ("last") -> users.findAllByName_Last(value).map(this::convertToBoundary).map(Optional::get).map(this::removePassword);
            default -> throw new InvalidCriteriaException("Invalid criteria. Please use email, country or last.");
        };
    }

    @Override
    public Flux<UserBoundary> getAllUsers() {
        return users.findAll()
                .map(this::convertToBoundary)
                .map(Optional::get)
                .map(this::removePassword);
    }

    @Override
    public Mono<UserBoundary> createUser(UserBoundary userBoundary) {
        return Mono.just(userBoundary)
                .map(user -> convertToEntity(user)
                        .orElseThrow(() -> new InvalidDateException("Invalid date format. Please use dd-mm-yyyy format.")))
                .flatMap(users::save)
                .map(this::convertToBoundary)
                .map(Optional::get);
    }

    @Override
    public Mono<Void> updateUser(String email, String password, UserBoundary userBoundary) {
       return this.users
               .findById(email)
               .map(userEntity -> {
                   if (userBoundary.getPassword().equals(password)) {
                       userBoundary.setEmail(userBoundary.getEmail());
                       userBoundary.setPassword(userBoundary.getPassword());
                   }
                   return userEntity;
               })
               .flatMap(this.users::save)
               .log()
               .then();

   }




    @Override
    public Mono<Void> deleteAllUsers() {
        return this.users.deleteAll();
    }


    private Optional<UserBoundary> convertToBoundary(UserEntity userEntity){
       System.out.println("convertToBoundary" + userEntity.getPassword());
       UserBoundary userBoundary = new UserBoundary();
       userBoundary.setEmail(userEntity.getEmail());
       userBoundary.setName(userEntity.getName());
       userBoundary.setPassword(userEntity.getPassword());
       userBoundary.setBirthdate(userEntity.getBirthdate().format(formatter));
       userBoundary.setRole(userEntity.getRole());
       userBoundary.setAddress(userEntity.getAddress());
       return Optional.of(userBoundary);
   }

   private Optional<UserEntity> convertToEntity(UserBoundary userBoundary){
            System.out.println("convertToEntity" + userBoundary.getPassword());
            UserEntity userEntity = new UserEntity();
             userEntity.setEmail(userBoundary.getEmail());
             userEntity.setName(userBoundary.getName());
             userEntity.setPassword(userBoundary.getPassword());
             try {
                 userEntity.setBirthdate(convertToDate(userBoundary.getBirthdate()));
             }
             catch (InvalidDateException e){
                 return Optional.empty();
             }
             userEntity.setRole(userBoundary.getRole());
             userEntity.setAddress(userBoundary.getAddress());
             return Optional.of(userEntity);
        }

        private LocalDate convertToDate(String dateString) throws InvalidDateException {
            try {
                return LocalDate.parse(dateString, formatter);
            }
            catch (Exception e){
                throw new InvalidDateException("Invalid date format.");
            }

        }

        private UserBoundary removePassword(UserBoundary userBoundary){
            userBoundary.setPassword(null);
            return userBoundary;
        }
}

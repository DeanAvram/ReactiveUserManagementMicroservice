package cloud.user.reactiveusermanagementmicroservice.services;

import cloud.user.reactiveusermanagementmicroservice.boundries.UserBoundary;
import cloud.user.reactiveusermanagementmicroservice.entities.UserEntity;
import cloud.user.reactiveusermanagementmicroservice.exceptions.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.regex.Pattern;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


@Service
public class UserServiceImplementation implements UserService {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private ReactiveUserCrud users;

    public UserServiceImplementation(ReactiveUserCrud users) {
        this.users = users;
    }


    @Override
    public Mono<UserBoundary> getUser(String email, String password) {
        return this.users.findById(email)
                .map(this::convertToBoundary)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User with email: " + email + " not found.")))
                .filter(user -> user.getPassword().equals(password))
                .switchIfEmpty(Mono.error(new WrongPasswordException("Wrong password.")))
                .map(userBoundary -> {
                    userBoundary.setPassword(null);
                    return userBoundary;
                });

    }

    public Flux<UserBoundary> getUsersByCriteria(String criteria, String value) {
        Flux<UserEntity> fluxEntity;

        try {
            fluxEntity = switch (criteria) {
                case ("email") -> users.findByEmail(value);
                case ("country") -> users.findAllByAddress_Country(value);
                case ("last") -> users.findAllByName_Last(value);
                case ("minimumAge") -> {
                    int minAge = Integer.parseInt(value);
                    yield users.findAllByBirthdateBefore(LocalDate.now().minusYears(minAge));
                }
                case ("maximumAge") -> {
                    int maxAge = Integer.parseInt(value);
                    yield users.findAllByBirthdateAfter(LocalDate.now().minusYears(maxAge));
                }
                default ->
                        throw new InvalidCriteriaException("Invalid criteria. Please use email, country, last, minimumAge or maximumAge.");
            };
        } catch (NumberFormatException e) {
            throw new InvalidCriteriaException("Invalid value for age criteria. Please provide a valid integer.");
        }

        return fluxEntity.map(this::convertToBoundary).map(userBoundary -> {
            userBoundary.setPassword(null);
            return userBoundary;
        });
    }


    @Override
    public Flux<UserBoundary> getAllUsers() {
        return users.findAll()
                .map(this::convertToBoundary)
                .map(userBoundary -> {
                    userBoundary.setPassword(null);
                    return userBoundary;
                });
    }

    @Override
    public Mono<UserBoundary> createUser(UserBoundary userBoundary) {
        Mono<UserEntity> entity = this.users.findById(userBoundary.getEmail());
        return entity.hasElement()
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new UserAlreadyExistsException("User with email: " + userBoundary.getEmail() + " already exists."));
                    }
                    return Mono.just(userBoundary);
                })
                .map(this::convertToEntity)
                .flatMap(users::save)
                .map(this::convertToBoundary);
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

    private UserBoundary convertToBoundary(UserEntity userEntity) {
        UserBoundary userBoundary = new UserBoundary();
        userBoundary.setEmail(userEntity.getEmail());
        userBoundary.setName(userEntity.getName());
        userBoundary.setPassword(userEntity.getPassword());
        userBoundary.setBirthdate(userEntity.getBirthdate().format(formatter));
        userBoundary.setRole(userEntity.getRole());
        userBoundary.setAddress(userEntity.getAddress());
        return userBoundary;
    }

    private UserEntity convertToEntity(UserBoundary userBoundary) {
        UserEntity userEntity = new UserEntity();
        if (!isValidEmail(userBoundary.getEmail())) {
            throw new InvalidMailException("Invalid email format.");
        }
        userEntity.setEmail(userBoundary.getEmail());
        userEntity.setName(userBoundary.getName());
        userEntity.setPassword(userBoundary.getPassword());
        try {
            userEntity.setBirthdate(convertToDate(userBoundary.getBirthdate()));
        } catch (InvalidDateException e) {
            throw new InvalidDateException("Invalid date format. Please use dd-mm-yyyy format.");
        }
        userEntity.setRole(userBoundary.getRole());
        userEntity.setAddress(userBoundary.getAddress());
        return userEntity;
    }

    private LocalDate convertToDate(String dateString) throws InvalidDateException {
        try {
            return LocalDate.parse(dateString, formatter);
        } catch (Exception e) {
            throw new InvalidDateException("Invalid date format.");
        }

    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}(\\.[A-Za-z]{2,})?$";
        Pattern pattern = Pattern.compile(emailRegex);
        return email != null && pattern.matcher(email).matches();
    }
}

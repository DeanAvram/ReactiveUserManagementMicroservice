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
                .switchIfEmpty(Mono.error(new GeneralBadRequestException("User with email: " + email + " not found.")))
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
                        throw new GeneralBadRequestException("Invalid criteria. Please use email, country, last, minimumAge or maximumAge.");
            };
        } catch (NumberFormatException e) {
            throw new GeneralBadRequestException("Invalid value for age criteria. Please provide a valid integer.");
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
                        return Mono.error(new GeneralBadRequestException("User with email: " + userBoundary.getEmail() + " already exists."));
                    }
                    return Mono.just(userBoundary);
                })
                .map(this::convertToEntity)
                .flatMap(users::save)
                .map(this::convertToBoundary)
                .map(user -> {
                    user.setPassword(null);
                    return user;
                });
    }

    @Override
    public Mono<Void> updateUser(String email, String password, UserBoundary userBoundary) {
        return this.users.findById(email)
                .map(userEntity -> {
                    if (userBoundary.getPassword() != null)
                        userEntity.setPassword(userBoundary.getPassword());
                    if (userBoundary.getName() != null) {
                        if (userBoundary.getName().getFirst() != null)
                            userEntity.getName().setFirst(userBoundary.getName().getFirst());
                        if (userBoundary.getName().getLast() != null)
                            userEntity.getName().setLast(userBoundary.getName().getLast());
                    }
                    if (userBoundary.getBirthdate() != null){
                        userEntity.setBirthdate(convertToDate(userBoundary.getBirthdate()));
                    }
                    if (userBoundary.getRole() != null)
                        userEntity.setRole(userBoundary.getRole());
                    if (userBoundary.getAddress() != null) {
                        if (userBoundary.getAddress().getCountry() != null)
                            userEntity.getAddress().setCountry(userBoundary.getAddress().getCountry());
                        if (userBoundary.getAddress().getCity() != null)
                            userEntity.getAddress().setCity(userBoundary.getAddress().getCity());
                        if (userBoundary.getAddress().getZip() != null)
                            userEntity.getAddress().setZip(userBoundary.getAddress().getZip());
                    }
                    return userEntity;
                })

                .flatMap(this.users::save)
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
        if (userBoundary.getEmail() == null)
            throw new GeneralBadRequestException("Email must be provided.");
        if (!isValidEmail(userBoundary.getEmail()))
            throw new GeneralBadRequestException("Invalid email format.");
        userEntity.setEmail(userBoundary.getEmail());
        if (userBoundary.getPassword() == null)
            throw new GeneralBadRequestException("Password must be provided.");
        if (userBoundary.getPassword().length() < 3)
            throw new GeneralBadRequestException("Password must be at least 3 characters long.");
        userEntity.setPassword(userBoundary.getPassword());
        if (userBoundary.getName() == null || userBoundary.getName().getFirst() == null || userBoundary.getName().getLast() == null)
            throw new GeneralBadRequestException("Name must have first and last name.");
        userEntity.setName(userBoundary.getName());
        if (userBoundary.getBirthdate() == null)
            throw new GeneralBadRequestException("Birthdate must be provided.");
        try {
            userEntity.setBirthdate(convertToDate(userBoundary.getBirthdate()));
        } catch (GeneralBadRequestException e) {
            throw new GeneralBadRequestException("Invalid date format. Please use dd-mm-yyyy format.");
        }
        if (userBoundary.getRole() == null)
            throw new GeneralBadRequestException("Role must be provided.");
        userEntity.setRole(userBoundary.getRole());
        if (userBoundary.getAddress() == null)
            throw new GeneralBadRequestException("Address must be provided.");
        if (userBoundary.getAddress().getCountry() == null || userBoundary.getAddress().getCity() == null || userBoundary.getAddress().getZip() == null)
            throw new GeneralBadRequestException("Address must have a country, city and a zip.");
        if (userBoundary.getAddress().getCountry().length() != 2)
            throw new GeneralBadRequestException("Country must be a 2 letter abbreviation.");
        userEntity.setAddress(userBoundary.getAddress());

        return userEntity;
    }

    private LocalDate convertToDate(String dateString) throws GeneralBadRequestException {
        try {
            return LocalDate.parse(dateString, formatter);
        } catch (Exception e) {
            throw new GeneralBadRequestException("Invalid date format. please use dd-mm-yyyy format.");
        }

    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}(\\.[A-Za-z]{2,})?$";
        Pattern pattern = Pattern.compile(emailRegex);
        return email != null && pattern.matcher(email).matches();
    }
}

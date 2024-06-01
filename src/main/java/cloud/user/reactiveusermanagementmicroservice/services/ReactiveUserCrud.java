package cloud.user.reactiveusermanagementmicroservice.services;

import cloud.user.reactiveusermanagementmicroservice.boundries.UserBoundary;
import cloud.user.reactiveusermanagementmicroservice.entities.UserEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;


public interface ReactiveUserCrud extends ReactiveMongoRepository<UserEntity, String> {
    public Flux<UserEntity> findByEmail(@Param("value") String email);
    public Flux<UserEntity> findAllByAddress_Country(@Param("value") String country);
    public Flux<UserEntity> findAllByName_Last(@Param("value") String last);
}

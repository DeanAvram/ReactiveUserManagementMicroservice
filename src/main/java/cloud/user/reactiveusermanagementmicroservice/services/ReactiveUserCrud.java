package cloud.user.reactiveusermanagementmicroservice.services;

import cloud.user.reactiveusermanagementmicroservice.entities.UserEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;


public interface ReactiveUserCrud extends ReactiveMongoRepository<UserEntity, String> {
}

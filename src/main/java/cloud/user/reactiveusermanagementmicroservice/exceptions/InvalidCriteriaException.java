package cloud.user.reactiveusermanagementmicroservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidCriteriaException extends RuntimeException{
    public InvalidCriteriaException(String message) {
        super(message);
    }
}

package cloud.user.reactiveusermanagementmicroservice.entities;

import cloud.user.reactiveusermanagementmicroservice.boundries.RolesEnum;
import cloud.user.reactiveusermanagementmicroservice.boundries.AddressBoundary;
import cloud.user.reactiveusermanagementmicroservice.boundries.NameBoundary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.Date;

@Document(collection = "USERS")
public class UserEntity {
    @Id private String email;
    private NameBoundary name;
    private String password;
    private LocalDate birthdate;
    private RolesEnum role;
    private AddressBoundary address;

    public UserEntity() {

    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public NameBoundary getName() {
        return name;
    }

    public void setName(NameBoundary name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(LocalDate birthdate) {
        this.birthdate = birthdate;
    }

    public RolesEnum getRole() {
        return role;
    }

    public void setRole(RolesEnum role) {
        this.role = role;
    }

    public AddressBoundary getAddress() {
        return address;
    }

    public void setAddress(AddressBoundary address) {
        this.address = address;
    }


}

package models;


import jakarta.persistence.*;

import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "users")
public class User implements Serializable {

    @Id
    @Column(name="user_id", unique = true, nullable = false)
    private Long userId;

    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "password", nullable = false, columnDefinition = "TEXT")
    private String password = "";

    @Column(name="first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "role", nullable = false, length = 20)
    @Pattern(regexp = "professor|student|admin", message = "Role must be 'professor' or 'student' or 'admin'")
    private String role;

    @OneToMany(mappedBy = "student")
    private List<Enrollment> enrollments;

    public User(){}

    public User(Long userId, String email, String password, String firstName, String lastName, String role){
        this.userId = userId;
        this.email = email;
        this.password = (password != null) ? password : "";
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }

    public User(Long username, String role) {
        this.userId = username;
        this.role = role;
    }

    public Long getUserId(){
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName(){
        return firstName;
    }

    public String getLastName(){
        return lastName;
    }

    public String getRole() {
        return role;
    }

    public List<Enrollment> getEnrollments() {
        return enrollments;
    }

    public String getPassword() {
        return password;
    }

    public String getUserName() {
        return (firstName + " " + lastName).trim();
    }

    public void setPassword(String hashedPassword) {
        this.password = hashedPassword;
    }
}

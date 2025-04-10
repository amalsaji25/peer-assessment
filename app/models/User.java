package models;


import jakarta.persistence.*;
import java.io.Serializable;
import java.util.List;
import javax.validation.constraints.Pattern;

/**
 * User is an entity class that represents a user in the system. It contains fields for the user's ID,
 * email, password, first name, last name, and role. The class also includes methods for getting and
 * setting these fields.
 */
@Entity
@Table(name = "users")
public class User implements Serializable {

    @Id
    @Column(name="user_id", unique = true, nullable = false)
    private Long userId;

    @Column(name = "email", length = 100)
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

    public void setUserId(long userId) {
        this.userId = userId;
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

    public void setRole(String role) {
        this.role = role;}

    public List<Enrollment> getEnrollments() {
        return enrollments;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String hashedPassword) {
        this.password = hashedPassword;
    }

    public String getUserName() {
        return (firstName + " " + lastName).trim();
    }
}

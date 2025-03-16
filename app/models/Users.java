package models;


import jakarta.persistence.*;

import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "users")
public class Users implements Serializable {

    @Id
    @Column(name="user_id", unique = true, nullable = false, length = 100)
    private String userId;

    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "password", nullable = false, columnDefinition = "TEXT")
    private String password;

    @Column(name="first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name="middle_name", length = 50)
    private String middleName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "role", nullable = false, length = 20)
    @Pattern(regexp = "professor|student", message = "Role must be 'professor' or 'student'")
    private String role;

    @OneToMany(mappedBy = "student", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Enrollments> enrollments;

    public Users(){}

    public Users(String userId, String email, String password, String firstName, String middleName, String lastName, String role){
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.role = role;
    }

    public String getUserId(){
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName(){
        return firstName;
    }

    public String getMiddleName(){
        return middleName;
    }

    public String getLastName(){
        return lastName;
    }

    public String getRole() {
        return role;
    }

    public List<Enrollments> getEnrollments() {
        return enrollments;
    }
}

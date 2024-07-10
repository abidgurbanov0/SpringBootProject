package az.CartEr.Model;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import java.util.List;

@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String surname;
    private String email;
    private String password;
    private String PPUrl;
    // store hashed password
    public User(String name, String surname, String email, String password, String PPUrl) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.password = password;
        this.PPUrl = PPUrl;
    }

    public User() {

    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    public  String getPPUrl() {
        return PPUrl;
    }
    public void setPPUrl(String PPUrl) {
        this.PPUrl = PPUrl;
    }

}

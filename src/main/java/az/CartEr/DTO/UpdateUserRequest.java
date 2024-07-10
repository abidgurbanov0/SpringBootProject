package az.CartEr.DTO;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String oldPassword;
    private String name;
    private String surname;
    private String password;
private  String PPUrl;
    // Getters and setters

    public String getPPUrl() {
        return PPUrl;
    }
    public void setPPUrl(String PPUrl) {
            this.PPUrl = PPUrl;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

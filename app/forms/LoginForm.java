package forms;

import play.data.validation.Constraints;

public class LoginForm {

    @Constraints.Required
    private Long userId;

    @Constraints.Required
    private String password;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}

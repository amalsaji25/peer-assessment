package forms;

import play.data.validation.Constraints;

/**
 * LoginForm is a data transfer object (DTO) that represents the form data for user login. It
 * contains fields for the user's ID and password.
 */
public class LoginForm {

  @Constraints.Required private Long userId;

  @Constraints.Required private String password;

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

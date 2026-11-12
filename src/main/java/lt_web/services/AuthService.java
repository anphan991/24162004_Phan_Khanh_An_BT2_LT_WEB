package lt_web.services;

import lt_web.models.LoginModel;
import lt_web.services.impl.UserServiceImpl;

public class AuthService {
    private final UserService userService = new UserServiceImpl();

    public boolean isValid(LoginModel login) {
        if (login == null) {
            return false;
        }
        return userService.login(login.username(), login.password()) != null;
    }
}

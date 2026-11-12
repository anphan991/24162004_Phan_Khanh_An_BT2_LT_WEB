package lt_web.services.impl;

import java.sql.Date;
import java.util.List;
import lt_web.entity.User;
import lt_web.repository.IUserRepository;
import lt_web.repository.impl.UserRepository;
import lt_web.services.UserService;
import lt_web.utils.PasswordHasher;

public class UserServiceImpl implements UserService {
    private final IUserRepository userRepo;

    public UserServiceImpl() {
        this(new UserRepository());
    }

    public UserServiceImpl(IUserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public User login(String username, String password) {
        User user = get(username);
        if (user == null || password == null) {
            return null;
        }

        if (PasswordHasher.matches(password, user.getPassWord())) {
            return user;
        }

        // Nâng cấp dữ liệu cũ: tài khoản plaintext đúng mật khẩu sẽ được hash ngay.
        if (!PasswordHasher.isHash(user.getPassWord()) && password.equals(user.getPassWord())) {
            userRepo.updatePassword(user.getUserName(), PasswordHasher.hash(password));
            return user;
        }
        return null;
    }

    @Override
    public User get(String username) {
        return username == null || username.isBlank() ? null : userRepo.get(username.trim());
    }

    @Override
    public User get(int id) {
        return userRepo.getById(id);
    }

    @Override
    public boolean register(String username, String password, String email, String fullname, String phone) {
        if (checkExistUsername(username)) {
            return false;
        }
        User user = new User(email, username, fullname, password, null, 5, phone, new Date(System.currentTimeMillis()));
        return create(user, password);
    }

    @Override
    public boolean create(User user, String rawPassword) {
        if (user == null || rawPassword == null || rawPassword.isBlank()) {
            return false;
        }
        user.setPassWord(PasswordHasher.hash(rawPassword));
        if (user.getRoleid() <= 0) {
            user.setRoleid(5);
        }
        if (user.getCreatedDate() == null) {
            user.setCreatedDate(new Date(System.currentTimeMillis()));
        }
        userRepo.insert(user);
        return true;
    }

    @Override
    public boolean update(User user, String rawPassword) {
        if (user == null || user.getId() <= 0) {
            return false;
        }
        User existing = userRepo.getById(user.getId());
        if (existing == null) {
            return false;
        }
        user.setUserName(existing.getUserName());
        user.setAvatar(existing.getAvatar());
        user.setCreatedDate(existing.getCreatedDate());
        user.setPassWord(rawPassword == null || rawPassword.isBlank()
                ? existing.getPassWord() : PasswordHasher.hash(rawPassword));
        if (user.getRoleid() <= 0) {
            user.setRoleid(existing.getRoleid());
        }
        userRepo.update(user);
        return true;
    }

    @Override
    public void delete(int id) {
        userRepo.delete(id);
    }

    @Override
    public int migrateLegacyPasswords() {
        return userRepo.migratePlaintextPasswords();
    }

    @Override
    public boolean checkExistEmail(String email) {
        return userRepo.checkExistEmail(email);
    }

    @Override
    public boolean checkExistEmail(String email, int excludedId) {
        return userRepo.checkExistEmail(email, excludedId);
    }

    @Override
    public boolean checkExistUsername(String username) {
        return userRepo.checkExistUsername(username);
    }

    @Override
    public boolean checkExistUsername(String username, int excludedId) {
        return userRepo.checkExistUsername(username, excludedId);
    }

    @Override
    public boolean checkExistPhone(String phone) {
        return userRepo.checkExistPhone(phone);
    }

    @Override
    public boolean checkExistPhone(String phone, int excludedId) {
        return userRepo.checkExistPhone(phone, excludedId);
    }

    @Override
    public List<User> getAll() {
        try {
            migrateLegacyPasswords();
        } catch (Exception ignored) {
            // Không chặn hiển thị danh sách tài khoản nếu dữ liệu cũ đang chuyển đổi.
        }
        return userRepo.findAll();
    }
}

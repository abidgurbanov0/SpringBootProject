package az.CartEr.Service;

import az.CartEr.Model.User;
import az.CartEr.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
  private PasswordEncoder passwordEncoder;
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean isEmailAlreadyTaken(String email) {
        return userRepository.findByEmail(email).isPresent();
    }




    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }



    public void saveUser(User user) {
        userRepository.save(user);
    }

    public User createUser(String name, String surname, String email, String password, String PPUrl) {
        User newUser = new User(name, surname, email,(password) , PPUrl );
        return userRepository.save(newUser);
    }


}

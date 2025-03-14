package sg.edu.ntu.gamify_demo.services;

import java.util.List;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sg.edu.ntu.gamify_demo.exceptions.UserNotFoundException;
import sg.edu.ntu.gamify_demo.exceptions.UserValidationException;
import sg.edu.ntu.gamify_demo.interfaces.UserService;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.repositories.UserRepository;

@Service
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final UserValidator userValidator;
    private final PasswordEncoder encoder;

    public UserServiceImpl(UserRepository userRepository, UserValidator userValidator, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.userValidator = userValidator;
        this.encoder = encoder;
    }

    @Override
    public User createUser(User user) throws UserValidationException {
        userValidator.validateUser(user);
        user.setPasswordHash(encoder.encode(user.getPasswordHash()));
        return userRepository.save(user);
    }

    @Override
    public User getUserById(String id) throws UserNotFoundException {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            throw new UserNotFoundException("User not found with id: " + id);
        }
        return optionalUser.get();
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User updateUser(String id, User user) throws UserNotFoundException, UserValidationException {
        userValidator.validateUser(user);
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with id: " + id);
        }
        user.setId(id);
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(String id) throws UserNotFoundException {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public void deductPoints(String id, int points) {
        User user = userRepository.findById(id).orElse(null);

        if (user != null) {
            int updatedPoints = user.getEarnedPoints() - points;
            user.setEarnedPoints(updatedPoints);
            userRepository.save(user);
        }
    }
}

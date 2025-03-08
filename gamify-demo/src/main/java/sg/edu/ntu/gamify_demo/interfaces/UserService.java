package sg.edu.ntu.gamify_demo.interfaces;

import java.util.List;

import sg.edu.ntu.gamify_demo.exceptions.UserNotFoundException;
import sg.edu.ntu.gamify_demo.exceptions.UserValidationException;
import sg.edu.ntu.gamify_demo.models.User;

public interface UserService {
    User createUser(User user) throws UserValidationException;
    User getUserById(String id) throws UserNotFoundException;
    List<User> getAllUsers();
    User updateUser(String id, User user) throws UserNotFoundException, UserValidationException;
    void deleteUser(String id) throws UserNotFoundException;
}

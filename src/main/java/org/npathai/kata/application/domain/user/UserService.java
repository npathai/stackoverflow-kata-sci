package org.npathai.kata.application.domain.user;

import org.npathai.kata.application.domain.services.IdGenerator;
import org.npathai.kata.application.domain.user.dto.User;
import org.npathai.kata.application.domain.user.persistence.UserRepository;
import org.npathai.kata.application.domain.user.request.RegisterUserRequest;

public class UserService {

    private final UserRepository userRepository;
    private final IdGenerator idGenerator;

    public UserService(UserRepository userRepository,
                       IdGenerator idGenerator) {
        this.userRepository = userRepository;
        this.idGenerator = idGenerator;
    }

    public User register(RegisterUserRequest registerUserRequest) {
        User user = new User();
        user.setId(idGenerator.get());
        user.setUsername(registerUserRequest.getUsername());
        user.setEmail(registerUserRequest.getEmail());
        user.setReputation(1);

        userRepository.save(user);
        return user;
    }

    public User getUserById(UserId userId) {
        return userRepository.findById(userId.getId()).get();
    }

    public void update(User user) {
        userRepository.save(user);
    }
}

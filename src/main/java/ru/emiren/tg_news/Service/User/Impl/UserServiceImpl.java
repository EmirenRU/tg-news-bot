package ru.emiren.tg_news.Service.User.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.emiren.tg_news.DTO.UserChatIDs;
import ru.emiren.tg_news.Model.User;
import ru.emiren.tg_news.Repository.UserRepository;
import ru.emiren.tg_news.Service.User.UserService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private List<String> premiumTags = Arrays.asList("PREMIUM","ADMIN");
    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void saveUser(User user) {
        userRepository.save(user);
    }

    @Override
    public void updateUser(User user) {
        userRepository.save(user);
    }

    @Override
    public void deleteUser(User user) {
        userRepository.delete(user);
    }

    @Override
    public User getUser(long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public List<User> getUsers() {
        return List.of();
    }

    @Override
    public String getRole(long id) {
        return "";
    }

    @Override
    public Optional<User> findById(Long userId) {
        return userRepository.findByUserId(userId);
    }

    @Override
    public List<UserChatIDs> getAllUsersWithRole(String role) {
        return userRepository.getChatIdByRole(role);
    }

    @Override
    public List<UserChatIDs> getAllPremiumUsers() {
        return userRepository.getAllPremiumUsers(premiumTags);
    }

    @Override
    public boolean ifExists(Long id) {
        return userRepository.findById(id).isPresent();
    }
}

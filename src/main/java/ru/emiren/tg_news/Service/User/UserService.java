package ru.emiren.tg_news.Service.User;

import org.springframework.transaction.annotation.Transactional;
import ru.emiren.tg_news.DTO.UserChatIDs;
import ru.emiren.tg_news.Model.User;

import java.util.List;
import java.util.Optional;

public interface UserService{
    @Transactional
    void saveUser(User user);
    void updateUser(User user);
    void deleteUser(User user);
    User getUser(long id);
    List<User> getUsers();
    String getRole(long id);

    Optional<User> findById(Long userId);
    List<UserChatIDs> getAllUsersWithRole(String role);
    List<UserChatIDs> getAllPremiumUsers();

    boolean ifExists(Long id);


}

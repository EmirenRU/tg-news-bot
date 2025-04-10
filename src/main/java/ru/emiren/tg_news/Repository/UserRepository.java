package ru.emiren.tg_news.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.emiren.tg_news.DTO.UserChatIDs;
import ru.emiren.tg_news.Model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserId(Long userId);

    @Query("SELECT u.userId FROM users_tg u WHERE u.role.role = :role")
    List<UserChatIDs> getChatIdByRole(String role);

    @Query("SELECT u.userId FROM users_tg u WHERE u.role.role in :tags")
    List<UserChatIDs> getAllPremiumUsers(List<String> tags);

}

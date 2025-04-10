package ru.emiren.tg_news.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.emiren.tg_news.Model.Roles;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Roles, Long> {

    @Query("SELECT r FROM Roles r WHERE r.role = :name")
    Optional<Roles> findByRole(String name);
}

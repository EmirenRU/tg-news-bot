package ru.emiren.tg_news.Repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.emiren.tg_news.Model.News;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {

    @Query("SELECT n FROM News n WHERE n.timeOfProcessing < :now ORDER BY n.id DESC")
    Optional<List<News>> findLastNewsForUser(LocalDateTime now, Pageable pageable);


    @Query("SELECT n FROM News n ORDER BY n.id DESC")
    Optional<List<News>> getLastNews(Pageable of);

    @Query("SELECT n FROM News n WHERE n.isCritical = true")
    Optional<List<News>> findLastCritical();

    @Query("SELECT n.title FROM News n WHERE n.title LIKE CONCAT('%', :title, '%')")
    Optional<String> findByTitle(String title);
}

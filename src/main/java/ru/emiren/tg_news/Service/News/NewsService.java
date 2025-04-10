package ru.emiren.tg_news.Service.News;

import ru.emiren.tg_news.Model.News;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface NewsService {
    void addNews(News news);
    void deleteNews(News news);
    void updateNews(News news);
    List<News> getAllNews();

    Optional<List<News>> getLastNewsForUser(LocalDateTime now);
    void sendNewsForPremiumUsers();
    void sendNewsForTypicalUsers();

    Optional<List<News>> getLastNewsForPremiumUsers();

    void saveNews(News news);

    void saveData(CompletableFuture<String> content,
                  String url,
                  String title,
                  String author,
                  String date,
                  LocalDateTime now);

    Boolean isInDbByTitle(String text);
}

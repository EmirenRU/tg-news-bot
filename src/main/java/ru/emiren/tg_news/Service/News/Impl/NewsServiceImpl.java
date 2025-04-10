package ru.emiren.tg_news.Service.News.Impl;

import com.google.gson.*;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.emiren.tg_news.DTO.NewsDTO;
import ru.emiren.tg_news.DTO.PredictionData;
import ru.emiren.tg_news.DTO.UserChatIDs;
import ru.emiren.tg_news.Mapper.NewsMapper;
import ru.emiren.tg_news.Model.News;
import ru.emiren.tg_news.Repository.NewsRepository;
import ru.emiren.tg_news.Service.News.NewsService;
import ru.emiren.tg_news.Service.User.UserService;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class NewsServiceImpl implements NewsService {

    private final NewsRepository newsRepository;
    private final KafkaTemplate<String, News> kafkaTemplate;
    private LocalDateTime now;
    private final TelegramBot bot;
    private static final String ADMIN_ROLE   = "ADMIN";
    private static final String USER_ROLE    = "USER";
    private static final String PREMIUM_ROLE = "PREMIUM";
    private final UserService userService;
    private final String newsText;
    private final Gson gson;
    private final List<DateTimeFormatter> formatters = Arrays.asList(
            DateTimeFormatter.ofPattern("MMM d yyyy"),
            DateTimeFormatter.ofPattern("MMM dd yyyy"),
            DateTimeFormatter.ofPattern("MMMM d yyyy"),
            DateTimeFormatter.ofPattern("MMMM dd yyyy"),
            DateTimeFormatter.ofPattern("MMM d'th', yyyy")
    );


    @Autowired
    public NewsServiceImpl(NewsRepository newsRepository, TelegramBot telegramBot, UserService userService, String newsText, Gson gson, KafkaTemplate<String, News> kafkaTemplate) {
        this.newsRepository = newsRepository;
        this.bot = telegramBot;
        this.userService = userService;
        this.newsText = newsText;
        this.gson = gson;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void addNews(News news) {
        newsRepository.save(news);
    }

    @Override
    public void deleteNews(News news) {
        newsRepository.delete(news);
    }

    @Override
    public void updateNews(News news) {
        newsRepository.save(news);
    }

    @Override
    public List<News> getAllNews() {
        return newsRepository.findAll();
    }

    @Override
    public Optional<List<News>> getLastNewsForUser(LocalDateTime now) {
        return newsRepository.findLastNewsForUser(now, PageRequest.of(0,5));
    }

    @Override
    @Scheduled(fixedRate = 300000, initialDelay = 300000)
    public void sendNewsForPremiumUsers() {
        List<UserChatIDs> userToSend = new ArrayList<>(userService.getAllPremiumUsers());
        List<NewsDTO> premiumNews =
                Objects.requireNonNull(getLastNewsForUser(LocalDateTime.now().minusMinutes(5))
                                .orElse(null))
                        .stream()
                        .map(NewsMapper::mapToNewsDTO)
                        .toList();
        sendNews(userToSend, premiumNews);
    }

    @Override
    @Scheduled(fixedRate = 1800000, initialDelay = 300000)
    public void sendNewsForTypicalUsers() {
        List<UserChatIDs> userToSend = new ArrayList<>(userService.getAllUsersWithRole(USER_ROLE));
        List<NewsDTO> slowNews =
                Objects
                        .requireNonNull
                                (
                                    getLastNewsForUser
                                        (
                                            LocalDateTime.now().minusMinutes(30)
                                        )
                                    .orElse(null)
                                )
                        .stream()
                        .map(NewsMapper::mapToNewsDTO)
                        .toList();
        sendNews(userToSend, slowNews);

    }

    @Override
    public Optional<List<News>> getLastNewsForPremiumUsers() {
        return newsRepository.getLastNews(PageRequest.of(0,3));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveNews(News news) {
        newsRepository.save(news);
    }


    private void sendNews(List<UserChatIDs> users, List<NewsDTO> news){
        for (UserChatIDs user : users) {
            for (NewsDTO n : news) {
                bot.execute(new SendMessage(user.getChatId(), editNewsText(n)));
            }
        }
    }

    @Async
    public void saveData(CompletableFuture<String> content, String url, String title, String author, String date, LocalDateTime now) {
        content.thenApply(json ->
                {
                    log.info("Raw JSON: {}", json);
                    JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

                    PredictionData pd = PredictionData.builder()
                            .summary(getStringOrNull(jsonObject, "summary"))
                            .prediction(getStringOrNull(jsonObject, "prediction"))
                            .isCritical(getBooleanOrFalse(jsonObject, "isCritical"))
                            .isCriticalSummary(getStringOrNull(jsonObject, "isCriticalSummary"))
                            .build();

                    log.info("PD: {}", pd);
                    News news = new News();
                    news.setTitle(title);
                    if (author != null) news.setAuthor(author);
                    news.setDate(convertToLocalDateTime(date));
                    news.setPrediction(pd.getPrediction());
                    news.setIsCritical(pd.isCritical());
                    if (author != null) news.setAuthor(author);
                    else news.setAuthor(URI.create(url).getHost());
                    news.setContent(pd.getSummary());
                    news.setUrl(url);
                    news.setTimeOfProcessing(now);

                    return news;
                })
                .thenAccept(this::publishToKafka)
                .exceptionally(ex -> {
                    log.error("Failed to process news: {}", ex.getMessage());
                    return null;
                });
    }

    private CompletableFuture<Void> publishToKafka(News news) {
        return CompletableFuture.runAsync(() -> kafkaTemplate.send("news", news));
    }

    @KafkaListener(topics = "news")
    private void getNewsFromTopicToSave(News news) {
        saveNews(news);
    }

    @Override
    public Boolean isInDbByTitle(String text) {
        return newsRepository.findByTitle(text).orElse(null) != null;
    }

    private String editNewsText(NewsDTO n) {
        return newsText.formatted(n.getContent(), n.getPrediction(), n.getUrl(), n.getDate());
    }

    private LocalDateTime convertToLocalDateTime(String dateString) {


        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(dateString.replaceAll("(?<=\\d)(st|nd|rd|th)", ""),
                                formatter.withLocale(Locale.US))
                        .atStartOfDay();
            } catch (DateTimeParseException ignored) {
                log.warn(ignored.getMessage());
            }
        }
        return null;
    }

    private String getStringOrNull(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : null;
    }

    private boolean getBooleanOrFalse(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() && obj.get(key).getAsBoolean();
    }
}

package ru.emiren.tg_news.Config;

import com.google.gson.Gson;
import com.pengrad.telegrambot.TelegramBot;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;
import ru.emiren.tg_news.Model.Roles;
import ru.emiren.tg_news.Repository.RoleRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


@Configuration
@Slf4j
public class AppConfig {
    private final RoleRepository rolesRepository;

    private final Environment env;
    private final List<String> sites = Arrays.asList(
//            "https://www.bbc.com/news",
//            "https://www.apnews.com/",
//            "https://www.forbes.com/",
//            "https://www.npr.org/",
//            "https://www.nytimes.com/",
//            "https://www.bloomberg.com/europe",
//            "https://www.bloomberg.com",
//            "https://www.wsj.com/",
//            "https://www.reuters.com/"//,
//            "https://www.ft.com/world"//, // ?page=2 ; ?page=3 ...
            "https://www.economist.com/topics/economy",
            "https://www.economist.com/topics/business",
            "https://www.economist.com/topics/artificial-intelligence",
//            "https://www.economist.com/weeklyedition/2025-03-15", // 2025 - 03 - 15(+-7)
            "https://www.economist.com/graphic-detail"//,
//            "https://markets.ft.com/data",
//
//            "https://economictimes.indiatimes.com/news"
    );

    private final String newsText = """
            Title: %s
            Summary: %s
            Prediction: %s
            URL: %s
            Time: %s
            Disclaimer: The responsibility for your actions and the interpretation of news fall on you.
            """;

    private final String receipt = """
            ✅ Payment Successful!
            Amount: %s
            Subscription valid until: %s.
            Transaction ID: %s
            Thanks for your subscription!
            """;

    @Bean
    public String receipt(){
        return receipt;
    }

    @Bean
    public ClassPathResource imgNewsCrownClassPathResource() {
        return new ClassPathResource("img/news-crown.png");
    }

    @Bean
    public String bootstrapServers() {
        return env.getProperty("spring.kafka.bootstrap-servers");
    }

    @Autowired
    public AppConfig(Environment env,
                     RoleRepository rolesRepository) {
        this.env = env;
        this.rolesRepository = rolesRepository;
    }

    @Bean
    public List<String> sitesToParse(){
        return sites;
    }

    @Bean
    public TelegramBot telegramBot(){
        return new TelegramBot(env.getProperty("telegram.bot.token"));
    }

    @Bean
    public String newsText(){
        return newsText;
    }

    @Bean
    public String winDriverPath(){
        return env.getProperty("selenium.firefox.driver.win");
    }

    @Bean
    public String linDriverPath(){
        return env.getProperty("selenium.firefox.driver.lin");
    }

    @Bean
    public String driverPath(){
        String osName = getOsName();
        if (osName.contains("windows")) {
            return winDriverPath();
        } else if (osName.contains("linux")) {
            return linDriverPath();
        } else {
            log.warn("Unsupported OS: " + osName);
        }
        return "";

    }

    @Bean
    public String getOsName(){
        return System.getProperty("os.name").toLowerCase();
    }

    @Bean
    public File driverFile(){
        if (!driverPath().isEmpty()) {
            ClassPathResource classPathResource = new ClassPathResource(driverPath());
            File tempFile = null;
            try (InputStream inputStream = classPathResource.getInputStream()) {
                if (getOsName().contains("linux")) {
                    tempFile = File.createTempFile("geckodriver", "");
                } else if (getOsName().contains("windows")) {
                    tempFile = File.createTempFile("geckodriver", ".exe");
                } else {
                    log.error("Unsupported OS: " + getOsName());
                }
                try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                    FileCopyUtils.copy(inputStream, outputStream);
                    if (getOsName().contains("linux")) {
                        log.info("Setting tempFile to executable is {}", tempFile.setExecutable(true));
                    }
                }
                log.info("Temporary file created: {}", tempFile);
            } catch (IOException e) {
                log.warn("Failed to create temporary file from resource: {}", e.getMessage());
            }
            System.setProperty("webdriver.chrome.driver", Objects.requireNonNull(tempFile).getAbsolutePath());
            return tempFile;
        }
        return null;
    }

    @Bean
    public Gson gson(){
        return new Gson();
    }

    @PostConstruct
    public void init(){
        if (rolesRepository.count() == 0) {
            rolesRepository.save(new Roles("ADMIN"));
            rolesRepository.save(new Roles("USER"));
            rolesRepository.save(new Roles("PREMIUM"));
        }
    }

}

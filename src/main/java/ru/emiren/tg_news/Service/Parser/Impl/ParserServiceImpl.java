package ru.emiren.tg_news.Service.Parser.Impl;

import com.google.gson.Gson;
import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.OperatingSystem;
import eu.bitwalker.useragentutils.UserAgent;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.emiren.tg_news.Service.AI.AiService;
import ru.emiren.tg_news.Service.Bot.CommandHandler;
import ru.emiren.tg_news.Service.News.NewsService;
import ru.emiren.tg_news.Service.Parser.ParserService;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class ParserServiceImpl implements ParserService {
    private final List<String> sites;
    private final NewsService newsService;
    private final AiService aiService;
    private final UserAgent userAgent;


    private String driverPath;
    private File driverFile;
    @Autowired
    private Gson gson;
    @Autowired
    private CommandHandler commandHandler;

    /*
            "https://www.ft.com/world", // ?page=2 ; ?page=3 ...
            "https://www.economist.com/topics/economy",
            "https://www.economist.com/topics/business",
            "https://www.economist.com/topics/artificial-intelligence",
            "https://www.economist.com/weeklyedition/2025-03-15", // 2025 - 03 - 15(+-7)
            "https://www.economist.com/graphic-detail",
            "https://markets.ft.com/data",

            "https://economictimes.indiatimes.com/news"
     */


    public ParserServiceImpl(List<String> sitesToParse, NewsService newsService, AiService aiService,    String driverPath, File driverFile) {
        this.sites = sitesToParse;
        this.newsService = newsService;
        this.aiService = aiService;
        this.driverPath = driverPath;
        this.driverFile = driverFile;
        this.userAgent = new UserAgent(OperatingSystem.WINDOWS_10, Browser.FIREFOX);

        log.info("DriverPath: {}", driverPath);
        log.info("driverFile: {}", driverFile);
    }

    @Override
    @Async
    public Map<String, Map<String, String>> fetchContent(String site) {
        Document doc = null;
        WebDriver driver = null;
        String title = null;
        if (driverFile != null) {
            FirefoxOptions options = new FirefoxOptions().addArguments("--headless");
            driver = new FirefoxDriver(options);
        }
        try{
            if (!site.contains("reuters")) {
                doc = Jsoup.connect(site).userAgent("Mozilla/5.0").get();
                title = doc.title();
//                log.info("doc html {}", doc.html());
//                log.info("doc title {}", title);
            } else {
                Objects.requireNonNull(driver).get(site);
                new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
                String bodyText = driver.getPageSource();
                log.info("Selenium text {}", bodyText);
                doc = Jsoup.parse(bodyText);
                title = doc.title();
//                log.info("doc html {}", doc.html());
//                log.info("doc title {}", title);
            }


            Elements elements = null;
            if (doc != null && title.equals("Financial time") || title.equals("World")) {
                elements = doc.select("a.js-teaser-heading-link");
                // todo handle it
            } else if (doc != null && title.contains("Latest news and analysis from The Economist")) {
                Elements temp = doc.select("h3.css-ayljkz.esjbanf0, h3.css-1awed3c.esjbanf0");
                elements = new Elements();
                for (Element t : temp){
                    Elements aElements = t.select("a");
                    // checking if the text is in title
                    if (newsService.isInDbByTitle(aElements.getFirst().text().trim())) {
                        continue;
                    }
                    elements.addAll(aElements);
                }
            } else if (title.contains("Reuters")) {
                elements = new Elements();
                // todo handle it
            }else {
                elements = null;
            }

            if (elements != null) {
                for (Element element : elements) {
                    Map<String, String> innerMap = new HashMap<>();
                    String href = element.attr("href");
                    log.info("href is {}", href);
                    URI uri = URI.create(site).resolve(href);
                    log.info("uri is {}", uri);
                    log.info("Resolved URI is {}", uri.resolve(href));
                    Document innerDoc = Jsoup.connect(uri.toString()).get();
                    log.info("URI Host is {}", uri.getHost());
//                    log.info("innerDoc: {}", innerDoc.html());

                    if (uri.getHost().contains("economist")) {
                        Elements docTitle = innerDoc.select("h1.css-1tik00t.e1c1hwj10");
                        if (newsService.isInDbByTitle(docTitle.getFirst().text())) {
                            continue;
                        }
                        //                        log.info("InnerDoc Title: {}", docTitle.getFirst().text());
                        Elements time = innerDoc.select("time.css-1rlefqb.e1t74fno0");
                        Elements content = innerDoc.select("p.css-1l5amll.e1y9q0ei0"); // size - 1 (The last paragraph is an ad)
                        Element eZero = content.getFirst();
                        StringBuilder builder = new StringBuilder();
                        builder.append(processZeroElementInEconomist(eZero));
                        for (int i = 1; i < content.size()-1; i++) {
                            builder.append(content.get(i).ownText()).append("\n");
                        }
                        newsService.saveData(aiService.makePrediction(builder.toString()), uri.toString(), docTitle.getFirst().text(), null, time.getFirst().text(), LocalDateTime.now());
                    }
                }
            }
        } catch (IOException e) {
            log.error("Error: {}, {}",e.getMessage(), e.getStackTrace());
        }

        return Collections.emptyMap();
    }

    @Override
    @Scheduled(fixedRate = 100000, initialDelay = 100000)
//    @Async
    public void parseSites() {
        log.info("Starting parsing sites by scheduler");
        for (String site : sites) {
            try{
                log.info("Parsing {}", site);
                fetchContent(site);
                break;
            } catch (Exception e){
                log.error("Error in scheduled parsing sites with error: {}", e.getMessage());
            }
        }
    }




    private String processZeroElementInEconomist(Element element) {
        StringBuilder builder = new StringBuilder();
        Elements spans = element.select("span");
        for (Element span : spans) {
            builder.append(span.ownText());
        }

        Elements smalls = element.select("small");
        for (Element small : smalls) {
            builder.append(small.text()).append(" ");
        }
        builder.append(element.ownText().trim());
//        log.info("Element Text: {}", element.text());

        return builder.toString().trim();
    }


}

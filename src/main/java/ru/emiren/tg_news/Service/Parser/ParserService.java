package ru.emiren.tg_news.Service.Parser;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface ParserService {
    Map<String, Map<String, String>> fetchContent(String site);
    void parseSites();

}

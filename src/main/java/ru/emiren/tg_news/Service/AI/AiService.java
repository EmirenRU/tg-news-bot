package ru.emiren.tg_news.Service.AI;

import java.util.concurrent.CompletableFuture;

public interface AiService {
    CompletableFuture<String> makePrediction(String text);

}

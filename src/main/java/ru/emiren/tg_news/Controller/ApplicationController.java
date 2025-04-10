package ru.emiren.tg_news.Controller;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.emiren.tg_news.Service.AI.AiService;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Slf4j
@RestController
public class ApplicationController {

    private final AiService aiService;
    private final Resource imgNewsCrownResource;

    @Autowired
    public ApplicationController(AiService aiService, ClassPathResource imgNewsCrownClassPathResource) {
        this.aiService = aiService;
        imgNewsCrownResource = imgNewsCrownClassPathResource;
    }

    @GetMapping("/chat/{prompt}")
    public String chat(@PathVariable String prompt) {
        try {
            return aiService.makePrediction(prompt).get();
        } catch (InterruptedException | ExecutionException e) {
            log.warn(e.getMessage());
        }
        return null;
    }

    @PostMapping("/site")
    public ResponseEntity<String> site(@RequestBody Map<String, String> formdata) {
        log.info("Requested site: {}", formdata);
        String site = formdata.get("url");
        if (!site.startsWith("http://") && !site.startsWith("https://")) {
            return ResponseEntity.badRequest().body("Invalid URL: must start with http:// or https://");
        }
        try {
            Document doc = Jsoup.connect(site).get();
            log.info("Html: {}",doc.html());
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return ResponseEntity.ok().build();
    }
}

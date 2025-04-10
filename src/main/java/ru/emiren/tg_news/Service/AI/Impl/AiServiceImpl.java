package ru.emiren.tg_news.Service.AI.Impl;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.emiren.tg_news.DTO.PredictionData;
import ru.emiren.tg_news.Service.AI.AiService;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class AiServiceImpl implements AiService {

    private Gson gson;
    private static final String jsonPattern = "\\`\\`\\`json\\\\n(.*?)\\\\n\\`\\`\\`";
    private final Pattern pattern = Pattern.compile(jsonPattern, Pattern.DOTALL);

    private final PromptTemplate pt = new PromptTemplate(
            """
            Analyze this news and provide strictly formatted JSON output:
            (
                "summary": "[1-paragraph concise English summary]",
                "prediction": "[Stock predictions for Nvidia/Apple/TSMC with percentages. Example: 'Nvidia: -10%, Apple: -5%, TSMC: -8%']",
                "isCritical": [true/false],
                "isCriticalSummary": "[1-sentence reason why this is critical]"
            )
            Requirements:
            1. ONLY output raw JSON (no markdown, no ```json```)
            2. Remove all line breaks and special symbols
            3. isCritical must be boolean (true/false)
            4. All text must be in English
            5. Final output must be single-line valid JSON
            
            News to analyze: {news}
            """
    );

    private final ChatModel chatModel;

    @Autowired
    AiServiceImpl(Gson gson, ChatModel chatModel) {
        this.gson = gson;
        this.chatModel = chatModel;
    }

    @Async
    @Override
    public CompletableFuture<String> makePrediction(String text) {
        Message prompt = pt.createMessage(Map.of("news", text));
        String message = chatModel.call(prompt);
        Pattern jsonPattern = Pattern.compile("```json\\s*(\\{.*?\\})\\s*```", Pattern.DOTALL);
        Matcher matcher = jsonPattern.matcher(message);

        if (matcher.find()) {
            String jsonContent = matcher.group(1);
            log.info("JSON Content: {}", jsonContent);
            return (jsonContent != null) ?
                    CompletableFuture.completedFuture(jsonContent) :
                    null;
        }
        return null;
    }
}

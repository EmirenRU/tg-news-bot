package ru.emiren.tg_news.DTO;

import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PredictionData {
    private String summary;
    private String prediction;
    private boolean isCritical;
    private String isCriticalSummary;
}

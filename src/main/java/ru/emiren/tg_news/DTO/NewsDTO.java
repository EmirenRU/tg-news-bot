package ru.emiren.tg_news.DTO;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NewsDTO {
    private Long id;
    private String title;
    private String content;
    private String author;
    private LocalDateTime date;
    private String url;
    private String prediction;
    private LocalDateTime timeOfProcessing;
    private Boolean isCritical;


}

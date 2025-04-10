package ru.emiren.tg_news.Model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class News {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    @Column(columnDefinition = "TEXT")
    private String content; // summary
    private String author;
    private LocalDateTime date;
    private String url;
    private String prediction;
    private Boolean isCritical;
    private LocalDateTime timeOfProcessing;
}

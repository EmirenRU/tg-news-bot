package ru.emiren.tg_news.Mapper;


import ru.emiren.tg_news.DTO.NewsDTO;
import ru.emiren.tg_news.Model.News;

public class NewsMapper {
    public static News mapToNews(NewsDTO news) {
        if (news == null) return null;
        return News.builder()
                .id(news.getId())
                .title(news.getTitle())
                .author(news.getAuthor())
                .content(news.getContent())
                .url(news.getUrl())
                .date(news.getDate())
                .prediction(news.getPrediction())
                .isCritical(news.getIsCritical())
                .timeOfProcessing(news.getTimeOfProcessing())
                .build();
    }

    public static NewsDTO mapToNewsDTO(News news) {
        if (news == null) return null;
        return NewsDTO.builder()
                .id(news.getId())
                .title(news.getTitle())
                .author(news.getAuthor())
                .content(news.getContent())
                .url(news.getUrl())
                .date(news.getDate())
                .prediction(news.getPrediction())
                .isCritical(news.getIsCritical())
                .timeOfProcessing(news.getTimeOfProcessing())
                .build();
    }
}

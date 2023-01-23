package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {
    private static final int PAGE_COUNT = 5;
    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    @Override
    public List<Post> list(String link) {
        List<Post> result = new ArrayList<>();
        try {
            for (int i = 1; i <= PAGE_COUNT; i++) {
                Connection connection = Jsoup.connect(link + i);
                Document document = connection.get();
                Elements rows = document.select(".vacancy-card__inner");
                rows.forEach(row -> result.add(getPost(row, link)));
            }
        } catch (IOException e) {
            throw new IllegalArgumentException();
        }
        return result;
    }

    private String retrieveDescription(String link) {
        String result = "";
        try {
            Connection connection = Jsoup.connect(link);
            Document document = connection.get();
            Element description = document.select(".vacancy-description__text").first();
            result = description.text();
        } catch (IOException e) {
            throw new IllegalArgumentException();
        }
        return result;
    }

    private Post getPost(Element element, String link) {
        Element titleElement = element.select(".vacancy-card__title").first();
        Element linkElement = titleElement.child(0);
        String vacancyName = titleElement.text();
        String vacancyLink = String.format("%s%s", link.substring(0, link.indexOf("/vacancies")), linkElement.attr("href"));
        Element dateElement = element.select(".vacancy-card__date").first();
        Element datetimeElement = dateElement.child(0);
        String vacancyDate = datetimeElement.attr("datetime");
        return new Post(vacancyName, vacancyLink, retrieveDescription(vacancyLink), dateTimeParser.parse(vacancyDate));
    }
}
package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class HabrCareerParse implements Parse {
    private static final String SOURCE_LINK = "https://career.habr.com";
    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);
    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    public static void main(String[] args) throws IOException {
        for (int i = 1; i <= 5; i++) {
            Connection connection = Jsoup.connect(PAGE_LINK + "?page=" + i);
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element titleElement = row.select(".vacancy-card__title").first();
                Element linkElement = titleElement.child(0);
                String vacancyName = titleElement.text();
                String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                Element dateElement = row.select(".vacancy-card__date").first();
                Element datetimeElement = dateElement.child(0);
                String date = datetimeElement.attr("datetime");
                System.out.printf("%s %s %s%n", vacancyName, link, date);
            });
        }
        List<Post> posts = new HabrCareerParse(new HabrCareerDateTimeParser()).list(SOURCE_LINK);
    }

    @Override
    public List<Post> list(String link) {
        List<Post> result = new ArrayList<>();
        try {
            AtomicInteger id = new AtomicInteger();
            for (int i = 1; i <= 5; i++) {
                Connection connection = Jsoup.connect(getPageLink(link) + "?page=" + i);
                Document document = connection.get();
                Elements rows = document.select(".vacancy-card__inner");
                rows.forEach(row -> {
                    Element titleElement = row.select(".vacancy-card__title").first();
                    Element linkElement = titleElement.child(0);
                    String vacancyName = titleElement.text();
                    String vacancyLink = String.format("%s%s", link, linkElement.attr("href"));
                    Element dateElement = row.select(".vacancy-card__date").first();
                    Element datetimeElement = dateElement.child(0);
                    String vacancyDate = datetimeElement.attr("datetime");
                    result.add(new Post(id.incrementAndGet(), vacancyName, vacancyLink, retrieveDescription(vacancyLink), dateTimeParser.parse(vacancyDate)));
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }
        return result;
    }

    private String getPageLink(String link) {
        return String.format("%s/vacancies/java_developer", link);
    }
}
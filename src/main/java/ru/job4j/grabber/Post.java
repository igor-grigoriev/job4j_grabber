package ru.job4j.grabber;

import java.time.LocalDateTime;
import java.util.Objects;

public record Post(int id, String title, String link, String description, LocalDateTime created) {

    public Post(String title, String link, String description, LocalDateTime created) {
        this(0, title, link, description, created);
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Post post = (Post) o;
        return id == post.id && Objects.equals(link, post.link);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, link);
    }

    @Override
    public String toString() {
        return "Post{id=" + id + ", title='" + title + '\'' + ", link='" + link + '\''
                + ", description='" + description + '\'' + ", created=" + created + '}';
    }
}
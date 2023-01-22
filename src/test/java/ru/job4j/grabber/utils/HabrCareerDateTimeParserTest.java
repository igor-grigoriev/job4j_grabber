package ru.job4j.grabber.utils;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.*;

class HabrCareerDateTimeParserTest {
    @Test
    public void testParser() {
        String date = "2023-01-01T01:01:01+03:00";
        String expected = "2023-01-01T01:01:01";
        LocalDateTime result = new HabrCareerDateTimeParser().parse(date);
        assertThat(result.toString()).isEqualTo(expected);
    }
}
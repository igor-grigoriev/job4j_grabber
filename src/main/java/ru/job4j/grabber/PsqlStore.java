package ru.job4j.grabber;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store {
    private Connection cnn;

    public PsqlStore(Properties cfg) {
        try {
            Class.forName(cfg.getProperty("jdbc.driver"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        cnn = getConnection(cfg);
    }

    public static void main(String[] args) {
        Properties config = new Properties();
        try (InputStream in = PsqlStore.class.getClassLoader().getResourceAsStream("store.properties")) {
            config.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Store store = new PsqlStore(config);
        store.save(new Post("name1", "link1", "text1", LocalDateTime.now()));
        System.out.println(store.findById(1));
        store.save(new Post("name2", "link2", "text2", LocalDateTime.now()));
        store.save(new Post("name3", "link2", "text3", LocalDateTime.now()));
        store.getAll().forEach(System.out::println);
    }

    @Override
    public void save(Post post) {
        try (PreparedStatement statement =
                     cnn.prepareStatement("insert into post(name, text, link, created) values (?, ?, ?, ?)"
                                     + " on conflict (link) do nothing",
                             Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, post.title());
            statement.setString(2, post.description());
            statement.setString(3, post.link());
            statement.setTimestamp(4, Timestamp.valueOf(post.created()));
            statement.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> rsl = new ArrayList<>();
        try (PreparedStatement statement =
                     cnn.prepareStatement("select * from post")) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    rsl.add(createPost(resultSet));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rsl;
    }

    @Override
    public Post findById(int id) {
        Post rsl = null;
        try (PreparedStatement statement =
                     cnn.prepareStatement("select * from post where id = ?")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    rsl = createPost(resultSet);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rsl;
    }

    @Override
    public void close() throws Exception {
        if (cnn != null) {
            cnn.close();
        }
    }

    private Connection getConnection(Properties cfg) {
        Connection connection = null;
        try {
            String url = cfg.getProperty("url");
            String login = cfg.getProperty("username");
            String password = cfg.getProperty("password");
            connection = DriverManager.getConnection(url, login, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    private Post createPost(ResultSet resultSet) throws SQLException {
        return new Post(resultSet.getInt("id"), resultSet.getString("name"),
                resultSet.getString("text"), resultSet.getString("link"),
                resultSet.getTimestamp("created").toLocalDateTime());
    }
}
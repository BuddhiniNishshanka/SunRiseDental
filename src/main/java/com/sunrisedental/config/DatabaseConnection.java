package com.sunrisedental.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Singleton Database Connection Pool Manager.
 * Implements the Singleton Design Pattern with double-checked locking for thread-safe access.
 */
public class DatabaseConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConnection.class);
    private static volatile DatabaseConnection instance;
    private HikariDataSource dataSource;

    private DatabaseConnection() {
        initDataSource();
    }

    private void initDataSource() {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (is != null) {
                props.load(is);
            } else {
                LOGGER.warn("application.properties not found, falling back to default database properties");
                props.setProperty("db.driver", "com.mysql.cj.jdbc.Driver");
                props.setProperty("db.url", "jdbc:mysql://localhost:3306/sunrise_dental_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
                props.setProperty("db.user", "root");
                props.setProperty("db.password", "");
            }

            HikariConfig config = new HikariConfig();
            config.setDriverClassName(props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver"));
            config.setJdbcUrl(props.getProperty("db.url", "jdbc:mysql://localhost:3306/sunrise_dental_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"));
            config.setUsername(props.getProperty("db.user", "root"));
            config.setPassword(props.getProperty("db.password", ""));
            config.setMaximumPoolSize(Integer.parseInt(props.getProperty("db.pool.maximumPoolSize", "10")));
            config.setMinimumIdle(Integer.parseInt(props.getProperty("db.pool.minimumIdle", "2")));
            config.setIdleTimeout(Long.parseLong(props.getProperty("db.pool.idleTimeout", "30000")));
            config.setConnectionTimeout(Long.parseLong(props.getProperty("db.pool.connectionTimeout", "20000")));
            config.setPoolName("SunriseDentalHikariPool");

            this.dataSource = new HikariDataSource(config);
            LOGGER.info("HikariCP Database Connection Pool successfully initialized.");
        } catch (IOException e) {
            LOGGER.error("Failed to load database configuration: {}", e.getMessage(), e);
            throw new RuntimeException("Database initialization failure", e);
        }
    }

    /**
     * Thread-safe Singleton access point.
     * @return DatabaseConnection instance
     */
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    /**
     * Get a connection from the pool.
     * @return java.sql.Connection
     * @throws SQLException on connection error
     */
    public Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            initDataSource();
        }
        return dataSource.getConnection();
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            LOGGER.info("HikariCP Database Connection Pool closed.");
        }
    }
}

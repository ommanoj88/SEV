package com.evfleet.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Multi-DataSource Configuration
 *
 * Configures 8 separate PostgreSQL databases for each module:
 * - evfleet_auth: User authentication and authorization
 * - evfleet_fleet: Vehicles, trips, telemetry
 * - evfleet_charging: Charging stations and sessions
 * - evfleet_maintenance: Maintenance schedules and battery health
 * - evfleet_driver: Driver management and performance
 * - evfleet_analytics: Cost analytics and reporting
 * - evfleet_notification: Notifications and alerts
 * - evfleet_billing: Billing and subscriptions
 *
 * Keeping databases separate allows for easier future extraction to microservices.
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "com.evfleet",
    entityManagerFactoryRef = "entityManagerFactory",
    transactionManagerRef = "transactionManager"
)
public class DataSourceConfig {

    // ===== Auth DataSource (Primary) =====

    @Primary
    @Bean
    @ConfigurationProperties("spring.datasource.auth")
    public DataSourceProperties authDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Primary
    @Bean
    @ConfigurationProperties("spring.datasource.auth.hikari")
    public DataSource authDataSource() {
        return authDataSourceProperties()
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    // ===== Fleet DataSource =====

    @Bean
    @ConfigurationProperties("spring.datasource.fleet")
    public DataSourceProperties fleetDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.fleet.hikari")
    public DataSource fleetDataSource() {
        return fleetDataSourceProperties()
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    // ===== Charging DataSource =====

    @Bean
    @ConfigurationProperties("spring.datasource.charging")
    public DataSourceProperties chargingDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.charging.hikari")
    public DataSource chargingDataSource() {
        return chargingDataSourceProperties()
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    // ===== Maintenance DataSource =====

    @Bean
    @ConfigurationProperties("spring.datasource.maintenance")
    public DataSourceProperties maintenanceDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.maintenance.hikari")
    public DataSource maintenanceDataSource() {
        return maintenanceDataSourceProperties()
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    // ===== Driver DataSource =====

    @Bean
    @ConfigurationProperties("spring.datasource.driver")
    public DataSourceProperties driverDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.driver.hikari")
    public DataSource driverDataSource() {
        return driverDataSourceProperties()
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    // ===== Analytics DataSource =====

    @Bean
    @ConfigurationProperties("spring.datasource.analytics")
    public DataSourceProperties analyticsDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.analytics.hikari")
    public DataSource analyticsDataSource() {
        return analyticsDataSourceProperties()
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    // ===== Notification DataSource =====

    @Bean
    @ConfigurationProperties("spring.datasource.notification")
    public DataSourceProperties notificationDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.notification.hikari")
    public DataSource notificationDataSource() {
        return notificationDataSourceProperties()
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    // ===== Billing DataSource =====

    @Bean
    @ConfigurationProperties("spring.datasource.billing")
    public DataSourceProperties billingDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.billing.hikari")
    public DataSource billingDataSource() {
        return billingDataSourceProperties()
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    // ===== Entity Manager Factory (Primary for all modules) =====

    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            EntityManagerFactoryBuilder builder) {

        // For now, we'll use the auth datasource as primary
        // Later, we'll configure specific entity managers per module
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.jdbc.batch_size", 20);
        properties.put("hibernate.order_inserts", true);
        properties.put("hibernate.order_updates", true);
        properties.put("hibernate.jdbc.batch_versioned_data", true);

        return builder
                .dataSource(authDataSource())
                .packages("com.evfleet") // Scan all modules
                .persistenceUnit("default")
                .properties(properties)
                .build();
    }

    @Primary
    @Bean
    public PlatformTransactionManager transactionManager(
            LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory.getObject());
    }
}

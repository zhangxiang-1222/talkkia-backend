package com.talkkia.api.config;

import ac.simons.neo4j.migrations.core.Migrations;
import ac.simons.neo4j.migrations.core.MigrationsConfig;
import org.neo4j.driver.Driver;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "org.neo4j.migrations", name = "enabled", havingValue = "true", matchIfMissing = true)
public class Neo4jMigrationsConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MigrationsConfig neo4jMigrationsConfig(
            @Value("${org.neo4j.migrations.locations-to-scan:classpath:neo4j/migrations}") String locationsToScan) {
        return MigrationsConfig.builder()
                .withLocationsToScan(locationsToScan.split("\\s*,\\s*"))
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public Migrations neo4jMigrations(MigrationsConfig neo4jMigrationsConfig, Driver driver) {
        return new Migrations(neo4jMigrationsConfig, driver);
    }

    @Bean
    @ConditionalOnMissingBean(name = "neo4jMigrationsInitializer")
    public InitializingBean neo4jMigrationsInitializer(Migrations neo4jMigrations) {
        return neo4jMigrations::apply;
    }
}

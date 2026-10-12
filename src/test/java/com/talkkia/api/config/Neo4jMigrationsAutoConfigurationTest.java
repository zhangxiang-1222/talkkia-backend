package com.talkkia.api.config;

import ac.simons.neo4j.migrations.core.Migrations;
import ac.simons.neo4j.migrations.core.MigrationsConfig;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class Neo4jMigrationsAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(Neo4jMigrationsConfiguration.class))
            .withBean(Driver.class, () -> mock(Driver.class))
            .withBean("neo4jMigrationsInitializer", InitializingBean.class, () -> () -> { })
            .withPropertyValues("org.neo4j.migrations.enabled=true");

    @Test
    void shouldCreateMigrationBeansWithCurrentSpringBootVersion() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(MigrationsConfig.class);
            assertThat(context).hasSingleBean(Migrations.class);
        });
    }
}

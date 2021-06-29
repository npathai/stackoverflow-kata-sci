package org.npathai.kata.acceptance.base;

import org.npathai.kata.acceptance.application.StackOverflowKataApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;

@SpringBootTest(classes = StackOverflowKataApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AcceptanceTestBase {

    private static final MySQLContainer<?> mySQLContainer;

    static {
        System.out.println("Starting DB container");

        mySQLContainer = new MySQLContainer<>("mysql:8.0.20")
                .withUsername("root")
                .withPassword("root")
                .withInitScript("schema.sql")
                .withReuse(true);
        mySQLContainer.start();

        System.out.println("Started DB container");
    }

    @DynamicPropertySource
    public static void setDatasourceProperties(final DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mySQLContainer::getJdbcUrl);
        registry.add("spring.datasource.password", mySQLContainer::getPassword);
        registry.add("spring.datasource.username", mySQLContainer::getUsername);
    }

    @Autowired
    protected TestRestTemplate restTemplate;
}

package com.education.rpg.rpglearningbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;
import java.util.TimeZone;


@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
@EnableScheduling
public class RpgLearningBackendApplication {

    public static void main(String[] args) {
        // Must be set before Spring starts so all JPA/timestamp handling uses UTC
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        SpringApplication.run(RpgLearningBackendApplication.class, args);
    }
}
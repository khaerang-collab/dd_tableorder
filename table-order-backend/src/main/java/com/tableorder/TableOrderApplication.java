package com.tableorder;

import com.tableorder.table.repository.TableSessionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.Transactional;

@SpringBootApplication
@EnableScheduling
public class TableOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(TableOrderApplication.class, args);
    }

    @Bean
    CommandLineRunner resetActiveUserCounts(TableSessionRepository sessionRepository) {
        return args -> {
            sessionRepository.findAll().forEach(session -> {
                if (session.getActiveUserCount() > 0) {
                    session.setActiveUserCount(0);
                    sessionRepository.save(session);
                }
            });
        };
    }
}

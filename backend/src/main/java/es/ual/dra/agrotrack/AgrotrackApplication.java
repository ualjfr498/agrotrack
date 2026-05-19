package es.ual.dra.agrotrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AgrotrackApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgrotrackApplication.class, args);
    }
}

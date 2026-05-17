package es.ual.dra.agrotrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AgroTrackApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgroTrackApplication.class, args);
    }
}

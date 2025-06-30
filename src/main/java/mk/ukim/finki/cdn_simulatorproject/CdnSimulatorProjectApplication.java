package mk.ukim.finki.cdn_simulatorproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class CdnSimulatorProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(CdnSimulatorProjectApplication.class, args);
        System.out.println("CDN simulator has started.");
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder(10);
    }

}
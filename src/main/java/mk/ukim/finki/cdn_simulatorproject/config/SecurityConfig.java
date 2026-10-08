package mk.ukim.finki.cdn_simulatorproject.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final PasswordEncoder passwordEncoder;
    private final String demoUsername;
    private final String demoPassword;

    public SecurityConfig(
            PasswordEncoder passwordEncoder,
            @Value("${app.security.username:user}") String demoUsername,
            @Value("${app.security.password:user}") String demoPassword
    ) {
        this.passwordEncoder = passwordEncoder;
        this.demoUsername = demoUsername;
        this.demoPassword = demoPassword;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/login",
                                "/style.css",
                                "/app.js",
                                "/images/**",
                                "/img/**",
                                "/favicon.ico",
                                "/error"
                        )
                        .permitAll()
                        .requestMatchers(
                                "/api/**",
                                "/cache/**"
                        )
                        .authenticated()
                        .anyRequest()
                        .authenticated()
                )
                .formLogin(form -> form
                        .permitAll()
                        .failureUrl("/login?error")
                        .defaultSuccessUrl("/", true)
                )
                .logout(logout -> logout
                        .clearAuthentication(true)
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessUrl("/login?logout")
                );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.builder()
                .username(demoUsername)
                .password(passwordEncoder.encode(demoPassword))
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(user);
    }
}
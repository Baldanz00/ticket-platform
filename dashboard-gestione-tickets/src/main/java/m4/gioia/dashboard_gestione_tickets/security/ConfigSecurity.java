package m4.gioia.dashboard_gestione_tickets.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class ConfigSecurity {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests()
                .requestMatchers("/tickets/create").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.POST, "/tickets/edit/**").hasAnyAuthority("OPERATOR", "ADMIN")
                .requestMatchers("/categories", "/categories/**").hasAuthority("ADMIN")
                .requestMatchers("/tickets/edit/**").hasAnyAuthority("OPERATOR", "ADMIN")
                .requestMatchers("/tickets", "/tickets/**").hasAnyAuthority("OPERATOR", "ADMIN")
                .requestMatchers("/**").permitAll()
                .and()
                .formLogin(form -> form
                        .defaultSuccessUrl("/tickets", true)
                        .permitAll()
                )
                .logout(logout -> logout.permitAll());
              //  .csrf(csrf -> csrf.disable()); -> tocken per evitare che un sito x possa mandare richieste GET ed eliminarmi un ticket a caso

        http.authenticationProvider(authenticationProvider());

        return http.build();
    }

    @Bean
    DBUserDetailService userDetailService() {
        return new DBUserDetailService();
    }

    // Password in chiaro (NoOp)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
}
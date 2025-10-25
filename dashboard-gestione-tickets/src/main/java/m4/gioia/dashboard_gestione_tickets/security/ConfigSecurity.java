package m4.gioia.dashboard_gestione_tickets.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
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
                .and().formLogin()
                .and().logout()
                .and().csrf().disable();
        return http.build();
    }

    @Bean
    DBUserDetailService DBUserDetailService() {
        return new DBUserDetailService();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
    //codifica della pw

    @Bean
    DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(DBUserDetailService());
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }
}

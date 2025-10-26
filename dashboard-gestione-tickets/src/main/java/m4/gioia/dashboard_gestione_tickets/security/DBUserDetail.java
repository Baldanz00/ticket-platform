package m4.gioia.dashboard_gestione_tickets.security;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import m4.gioia.dashboard_gestione_tickets.model.Role;
import m4.gioia.dashboard_gestione_tickets.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class DBUserDetail implements UserDetails {

    private String username;

    private String password;

    private Set<GrantedAuthority> authorities;

    public DBUserDetail(User user) {
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.authorities = new HashSet<>();

        System.out.println("=== CARICAMENTO RUOLI PER: " + user.getUsername() + " ===");
        System.out.println("Numero ruoli trovati: " + user.getRoles().size());

        for (Role role : user.getRoles()) {
            System.out.println("Ruolo dal DB: '" + role.getName() + "'");
            SimpleGrantedAuthority sGA = new SimpleGrantedAuthority(role.getName());
            this.authorities.add(sGA);
        }

        System.out.println("Authorities finali: " + this.authorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }
}

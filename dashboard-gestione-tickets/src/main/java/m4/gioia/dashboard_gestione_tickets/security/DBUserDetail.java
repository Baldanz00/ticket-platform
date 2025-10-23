package m4.gioia.dashboard_gestione_tickets.security;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.esercizio.milestone.ticket_platform.model.Role;
import com.esercizio.milestone.ticket_platform.model.User;

import m4.gioia.dashboard_gestione_tickets.model.User;

public class DBUserDetail implements UserDetails {

    private String username;

    private String password;

    private Set<GrantedAuthority> authorities;

    public DBUserDetail(User user) {
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.authorities = new HashSet<>();
        for (Role role : user.getRoles()) {
            SimpleGrantedAuthority sGA = new SimpleGrantedAuthority(role.getName());
            this.authorities.add(sGA);
        }
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

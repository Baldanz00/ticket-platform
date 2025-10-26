package m4.gioia.dashboard_gestione_tickets.security;

import java.util.Optional;

import m4.gioia.dashboard_gestione_tickets.model.DataBase;
import m4.gioia.dashboard_gestione_tickets.model.User;
import m4.gioia.dashboard_gestione_tickets.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DBUserDetailService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("=== LOGIN DEBUG ===");
        System.out.println("Username ricercato: " + username);

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            System.out.println("Utente trovato!");
            System.out.println("Password dal DB: " + user.getPassword());
            System.out.println("Ruoli: " + user.getRoles());
            return new DBUserDetail(user);
        } else {
            System.out.println("Utente NON trovato!");
            throw new UsernameNotFoundException("Username not found");
        }
    }
}

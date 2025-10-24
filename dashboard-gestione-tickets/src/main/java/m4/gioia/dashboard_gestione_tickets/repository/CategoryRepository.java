package m4.gioia.dashboard_gestione_tickets.repository;

import m4.gioia.dashboard_gestione_tickets.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<User,Long> {
}

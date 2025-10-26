package m4.gioia.dashboard_gestione_tickets.repository;

import m4.gioia.dashboard_gestione_tickets.model.Category;
import m4.gioia.dashboard_gestione_tickets.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // opzionale, se vuoi solo i nomi
    @Query("SELECT c.name FROM Category c")
    List<String> findAllCategoryNames();
}
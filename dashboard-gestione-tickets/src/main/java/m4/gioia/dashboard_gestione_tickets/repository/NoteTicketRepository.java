package m4.gioia.dashboard_gestione_tickets.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import m4.gioia.dashboard_gestione_tickets.model.NoteTicket;

@Repository
public interface NoteTicketRepository extends JpaRepository<NoteTicket, Long> {
}
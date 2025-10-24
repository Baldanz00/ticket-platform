package m4.gioia.dashboard_gestione_tickets.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import m4.gioia.dashboard_gestione_tickets.model.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    public List<Ticket> findTicketByStato(Ticket.Status ticketStato);

    public List<Ticket> findByTitoloContainingIgnoreCase(String keyword);

    public Optional<Ticket> findByTitolo(String titolo);

    List<Ticket> findByUser_Username(String username);

    List<Ticket> findTicketByStatoAndUser_Username(Ticket.Status ticketStato, String username);
}

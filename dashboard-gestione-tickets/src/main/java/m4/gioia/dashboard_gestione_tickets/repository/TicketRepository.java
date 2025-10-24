package m4.gioia.dashboard_gestione_tickets.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import m4.gioia.dashboard_gestione_tickets.model.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    public List<Ticket> findTicketByStatus(Ticket.Status ticketStato);

    public List<Ticket> findByTitleContainingIgnoreCase(String keyword);

    public Optional<Ticket> findByTitle(String title);

    List<Ticket> findByUser_Username(String username);

    List<Ticket> findTicketByStatusAndUser_Username(Ticket.Status ticketStato, String username);
}

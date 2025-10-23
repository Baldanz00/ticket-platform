package m4.gioia.dashboard_gestione_tickets.model;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.FutureOrPresent;

@Entity
@Table(name = "note_ticket")
public class NoteTicket extends DataBase {

    /* può essere assegnata ad uno o più ticket dall'admin */

    @ManyToOne
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User autore;

    @NotBlank
    @Column(nullable = false, length = 2000)
    private String contenuto;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public User getAutore() {
        return autore;
    }

    public void setAuthor(User autore) {
        this.autore = autore;
    }

    public String getContenuto() {
        return contenuto;
    }

    public void setContent(String contenuto) {
        this.contenuto = contenuto;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

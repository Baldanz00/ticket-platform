package m4.gioia.dashboard_gestione_tickets.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

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
    private LocalDate createdAt = LocalDate.now();

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
    public void setContenuto(String contenuto) {
        this.contenuto = contenuto;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }
}

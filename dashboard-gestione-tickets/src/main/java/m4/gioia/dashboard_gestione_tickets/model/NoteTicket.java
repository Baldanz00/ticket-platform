package m4.gioia.dashboard_gestione_tickets.model;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.FutureOrPresent;

@Entity
public class NoteTicket {

    /* può essere assegnata ad uno o più ticket dall'admin */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idNote;

    private String contenutoNota;

    @FutureOrPresent("La data deve essere odierna o futura")
    private LocalDate dataCreazioneNota;

    private String autoreNota;

    @ManyToOne(optional = false)
    private Ticket ticket;
}

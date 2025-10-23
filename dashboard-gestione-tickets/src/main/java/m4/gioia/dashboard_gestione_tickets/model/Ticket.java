package m4.gioia.dashboard_gestione_tickets.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
public class Ticket {

    /*
     * id -> PK , unique,
     * /titolo -> varhar(255), not null
     * /descrizione -> text
     * /stato -> enum (DA_FARE, IN_CORSO, COMPLETATO) , not null
     * /data_creazione_ticket -> datetime
     * FK -> id_autore 1->n
     * FK -> id_operatore 1->n
     * FK -> id_categoria 1->n
     * 
     * il ticket ha:
     * - una categoria
     * - è assegnato ad un operatore disponibile
     * - è creato da un admin
     * - può avere note scritte dall'admin
     * 
     */

    @Id // PK
    @GeneratedValue(strategy = GenerationType.IDENTITY) // autoincrementale
    private Integer idTicket;

    @NotBlank("Il titolo non può essere vuoto")
    @Column(nullable = false)
    private String titolo;

    private String descrizione;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TicketStato stato = TicketStato.DA_FARE;

    // data di creazione
    @FutureOrPresent("La data deve essere odierna o futura")
    private LocalDate dataCreazioneTicket = LocalDate.now();

    // data di modifica del ticket
    @FutureOrPresent("La data deve essere odierna o futura")
    private LocalDate dataModificaTicket = LocalDate.now();

    // FK -> id_autore

    // FK -> id_operatore
    @ManyToOne(optional = false)
    private Operatore operatore;

    // FK -> id_categoria
    @ManyToOne(optional = false)
    private Categoria categoria;

    @OneToMany(mappedBy = "ticket")
    private List<NoteTicket> notes = new ArrayList<>();

    public enum TicketStato {

        DA_FARE,
        IN_CORSO,
        COMPLETATO;
    }

    public Integer getIdTicket() {
        return idTicket;
    }

    public void setId(Integer idTicket) {
        this.idTicket = idTicket;
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public TicketStato getStato() {
        return stato;
    }

    public void setStato(TicketStato stato) {
        this.stato = stato;
    }

    public LocalDate getDataCreazioneTicket() {
        return dataCreazioneTicket;
    }

    public void setDataCreazioneTicket(LocalDate dataCreazioneTicket) {
        this.dataCreazioneTicket = dataCreazioneTicket;
    }

    public LocalDate getDataModificaTicket() {
        return dataModificaTicket;
    }

    public void setDataModificaTicket(LocalDate dataModificaTicket) {
        this.dataModificaTicket = dataModificaTicket;
    }

    public Operatore getOperatore() {
        return operatore;
    }

    public void setOperatore(Operatore operatore) {
        this.operatore = operatore;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public List<NoteTicket> getNotes() {
        return notes;
    }

    public void setNotes(List<NoteTicket> notes) {
        this.notes = notes;
    }
}

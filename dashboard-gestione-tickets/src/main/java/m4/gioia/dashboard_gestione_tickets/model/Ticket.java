package m4.gioia.dashboard_gestione_tickets.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "tickets")
public class Ticket extends DataBase {

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

    public enum Status {

        DA_FARE,
        IN_CORSO,
        COMPLETATO;
    }

    @Column(nullable = false)
    private LocalDate dataCreazione;

    @NotBlank
    @Column(nullable = false, length = 255)
    private String titolo;

    @NotBlank
    @Column(nullable = false, length = 2000)
    private String descrizione;

    @NotNull
    @Enumerated(EnumType.STRING) // salva nel db il nome dell'enum invece del suo valore
    @Column(nullable = false)
    private Status stato = Status.DA_FARE;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true) // se rimuovo una nota dal ticket,
                                                                                     // la rimuovo anche dal db
    @OrderBy("createdAt ASC")
    private List<NoteTicket> notes = new ArrayList<>();

    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public LocalDate getDataCreazione() {
        return dataCreazione;
    }

    public void setCreationDate(LocalDate dataCreazione) {
        this.dataCreazione = dataCreazione;
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitle(String titolo) {
        this.titolo = titolo;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescription(String descrizione) {
        this.descrizione = descrizione;
    }

    public Status getStato() {
        return stato;
    }

    public void setStatus(Status stato) {
        this.stato = stato;
    }

    public Category getCategoria() {
        return category;
    }

    public void setCategoria(Category category) {
        this.category = category;
    }

    public List<NoteTicket> getNotes() {
        return notes;
    }

    public void setNotes(List<NoteTicket> notes) {
        this.notes = notes;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}

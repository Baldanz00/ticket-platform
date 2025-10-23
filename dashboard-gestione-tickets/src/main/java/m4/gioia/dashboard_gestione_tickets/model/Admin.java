package m4.gioia.dashboard_gestione_tickets.model;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
public class Admin {

    /*
     * PK -> id (unique)
     * / nome -> varchar, not null, not blank
     * / email -> varchar, not null, not blank , unique
     * /password -> varchar, not null, not blank
     * 
     * /-> può creare tickets
     * /-> può modificare un ticket (salvataggio della modifica)
     * /->assegnare il ticket ad un operatore
     * /-> può aggiungere note al ticket
     */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idAdmni;

    @NotBlank("Il nome non può essere vuoto")
    @NotNull("Il nome non può essere nullo")
    private String nomeAdmin;

    @NotBlank("L'email non può essere vuota")
    @NotNull("L'email non può essere nulla")
    private String emailAdmin;

    @NotBlank("La password non può essere vuota")
    @NotNull("La password non può essere nulla")
    private String passwordAdmin;

    @OneToMany(mappedBy = "autoreNota")
    private List<NoteTicket> noteTickets;
}

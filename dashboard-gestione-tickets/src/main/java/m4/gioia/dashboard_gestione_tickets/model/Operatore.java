package m4.gioia.dashboard_gestione_tickets.model;

import java.util.List;

import jakarta.annotation.Generated;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
public class Operatore {

    /*
     * - vede la lista dei ticket a lui assegnati
     * / - può modificare lo stato del ticket (DA_FARE, IN_CORSO, COMPLETATO)
     * / - PK -> id (unique)
     * / nome -> varchar, not null, not blank
     * / email -> varchar, not null, not blank , unique
     * /password -> varchar, not null, not blank
     * / disponibilità -> boolean (disponibile/non disponibile)
     * 
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idOperatore;

    @NotBlank("Il nome non può essere vuoto")
    @NotNull("Il nome non può essere nullo")
    private String nomeOperatore;

    @NotBlank("L'email non può essere vuota")
    @Column(nullable = false, unique = true)
    private String emailOperatore;

    @NotBlank("La password non può essere vuota")
    @Column(nullable = false, unique = true)
    private String passwordOperatore;

    private boolean disponibilitàOperatore;

    @OneToMany(mappedBy = "operatore")
    private List<Ticket> tickets;
}

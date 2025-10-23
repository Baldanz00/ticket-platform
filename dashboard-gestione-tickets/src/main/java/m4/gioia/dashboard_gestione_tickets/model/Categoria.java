package m4.gioia.dashboard_gestione_tickets.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
public class Categoria {

    /*
     * PK -> id (unique)
     * / nome -> varchar, not null, not blanck
     * 
     * relazoione 1 -> n ticket
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCategoria;

    @NotBlank("Il nome non può essere vuoto")
    @NotNull("Il nome non può essere nullo")
    private String nomeCategoria;

    @OneToMany(mappedBy = "categoria")
    private List<Ticket> tickets;
}

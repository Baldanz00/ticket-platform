package m4.gioia.dashboard_gestione_tickets.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "categorie")
public class Categoria extends DataBase {

    /*
     * PK -> id (unique)
     * / nome -> varchar, not null, not blanck
     * 
     * relazoione 1 -> n ticket
     */

    @NotNull
    @NotBlank
    @Column(unique = true, nullable = false)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

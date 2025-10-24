package m4.gioia.dashboard_gestione_tickets.model;

import jakarta.persistence.*;

@MappedSuperclass
public abstract class DataBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

}

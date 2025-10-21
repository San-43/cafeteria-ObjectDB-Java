package org.cafeteria.cafeteria.model;

import jakarta.persistence.*;

@Entity
@Table(name = "proporcion_ingrediente",
        uniqueConstraints = @UniqueConstraint(name="uk_receta_ingrediente",
                columnNames = {"id_receta","id_ingrediente"}))
public class ProporcionIngrediente {
    @Id
    @Column(name = "id_proporcion", length = 36)
    public String idProporcion;

    @ManyToOne(optional = false) @JoinColumn(name = "id_receta", nullable = false)
    public Receta receta;

    @ManyToOne(optional = false) @JoinColumn(name = "id_ingrediente", nullable = false)
    public Ingrediente ingrediente;

    public Double proporcion;

    @PrePersist
    public void prePersist() {
        if (idProporcion == null || idProporcion.isBlank()) {
            idProporcion = java.util.UUID.randomUUID().toString();
        }
    }

    public ProporcionIngrediente() {}
}

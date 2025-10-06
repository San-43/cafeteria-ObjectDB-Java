package org.cafeteria.cafeteria.model;

import jakarta.persistence.*;

@Entity
@Table(name = "proporcion_ingrediente",
        uniqueConstraints = @UniqueConstraint(name="uk_receta_ingrediente",
                columnNames = {"id_receta","id_ingrediente"}))
public class ProporcionIngrediente {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long idProporcion;

    @ManyToOne(optional = false) @JoinColumn(name = "id_receta", nullable = false)
    public Receta receta;

    @ManyToOne(optional = false) @JoinColumn(name = "id_ingrediente", nullable = false)
    public Ingrediente ingrediente;

    public String proporcion; // p.ej. "12 g"

    public ProporcionIngrediente() {}
}

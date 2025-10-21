package org.cafeteria.cafeteria.model;

import jakarta.persistence.*;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity @Table(name = "ingrediente")
public class Ingrediente {
    @Id
    @Column(name = "id_ingrediente", length = 36)
    public String idIngrediente;

    public String nombre;
    @Lob public String descripcion;

    @OneToMany(mappedBy = "ingrediente")
    public Set<ProporcionIngrediente> enRecetas = new LinkedHashSet<>();

    @OneToMany(mappedBy = "ingrediente")
    public Set<InventarioIngredientes> lotes = new LinkedHashSet<>();

    @PrePersist
    public void prePersist() {
        if (idIngrediente == null || idIngrediente.isBlank()) {
            idIngrediente = java.util.UUID.randomUUID().toString();
        }
    }

    public Ingrediente() {}
}

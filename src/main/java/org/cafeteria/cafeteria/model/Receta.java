package org.cafeteria.cafeteria.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity @Table(name = "receta")
public class Receta {
    @Id
    @Column(name = "id_receta", length = 36)
    public String idReceta;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_producto", nullable = false)
    public Producto producto;

    public String nombre;
    public String tamano;

    @Column(name = "costo_preparacion", precision = 12, scale = 2)
    public BigDecimal costoPreparacion;

    @OneToMany(mappedBy = "receta", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "orden")
    public List<Paso> pasos = new ArrayList<>();

    @OneToMany(mappedBy = "receta")
    public Set<ProporcionIngrediente> ingredientes = new LinkedHashSet<>();

    @PrePersist
    public void prePersist() {
        if (idReceta == null || idReceta.isBlank()) {
            idReceta = UUID.randomUUID().toString();
        }
    }

    public Receta() {}
}

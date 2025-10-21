package org.cafeteria.cafeteria.model;

import jakarta.persistence.*;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity @Table(name = "tienda")
public class Tienda {
    @Id
    @Column(name = "id_tienda", length = 36)
    public String idTienda;

    public String telefono;
    public String direccion;

    @Column(name = "empleado_responsable")
    public String empleadoResponsable;

    @OneToMany(mappedBy = "tienda")
    public Set<Inventario> inventarios = new LinkedHashSet<>();

    @OneToMany(mappedBy = "tienda")
    public Set<InventarioIngredientes> inventarioIng = new LinkedHashSet<>();

    @OneToMany(mappedBy = "tienda")
    public Set<Venta> ventas = new LinkedHashSet<>();

    @PrePersist
    public void prePersist() {
        if (idTienda == null || idTienda.isBlank()) {
            idTienda = UUID.randomUUID().toString();
        }
    }

    public Tienda() {}
}

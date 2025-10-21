package org.cafeteria.cafeteria.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity @Table(name = "productos")
public class Producto {
    @Id
    @Column(name = "id_producto", length = 36)
    public String idProducto;

    public String nombre;
    public String descripcion;

    @Column(precision = 12, scale = 2)
    public BigDecimal costo;

    @Column(name = "precio_venta", precision = 12, scale = 2)
    public BigDecimal precioVenta;

    @OneToMany(mappedBy = "producto")
    public Set<Inventario> stocks = new LinkedHashSet<>();

    @OneToMany(mappedBy = "producto")
    public Set<Venta> ventas = new LinkedHashSet<>();

    @OneToMany(mappedBy = "producto")
    public Set<Receta> recetas = new LinkedHashSet<>();

    @PrePersist
    public void prePersist() {
        if (idProducto == null || idProducto.isBlank()) {
            idProducto = UUID.randomUUID().toString();
        }
    }

    public Producto() {}
}

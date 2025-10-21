package org.cafeteria.cafeteria.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "inventario_ingredientes",
        uniqueConstraints = @UniqueConstraint(name="uk_tienda_ingrediente",
                columnNames = {"id_tienda","id_ingrediente"}))
public class InventarioIngredientes {
    @Id
    @Column(name = "id_inventario_ingredientes", length = 36)
    public String idInventarioIngredientes;

    @ManyToOne(optional = false) @JoinColumn(name = "id_tienda", nullable = false)
    public Tienda tienda;

    @ManyToOne(optional = false) @JoinColumn(name = "id_ingrediente", nullable = false)
    public Ingrediente ingrediente;

    @Column(name = "fecha_compra")
    public LocalDate fechaCompra;

    @Column(name = "fecha_caducidad")
    public LocalDate fechaCaducidad;

    @Column(name = "costo_compra", precision = 12, scale = 2)
    public BigDecimal costoCompra;

    @Column(name = "precio_venta_porcion", precision = 12, scale = 2)
    public BigDecimal precioVentaPorcion;

    @PrePersist
    public void prePersist() {
        if (idInventarioIngredientes == null || idInventarioIngredientes.isBlank()) {
            idInventarioIngredientes = java.util.UUID.randomUUID().toString();
        }
    }

    public InventarioIngredientes() {}
}

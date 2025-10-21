package org.cafeteria.cafeteria.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity @Table(name = "venta")
public class Venta {
    @Id
    @Column(name = "id_venta", length = 36)
    public String idVenta;

    @ManyToOne(optional = false) @JoinColumn(name = "id_tienda", nullable = false)
    public Tienda tienda;

    @ManyToOne(optional = false) @JoinColumn(name = "id_producto", nullable = false)
    public Producto producto;

    public LocalDate fecha;

    @Column(precision = 12, scale = 2)
    public BigDecimal total;

    @PrePersist
    public void prePersist() {
        if (idVenta == null || idVenta.isBlank()) {
            idVenta = UUID.randomUUID().toString();
        }
    }

    public Venta() {}
}

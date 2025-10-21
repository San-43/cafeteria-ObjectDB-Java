package org.cafeteria.cafeteria.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "producto_vendido",
        uniqueConstraints = @UniqueConstraint(name="uk_venta_producto",
                columnNames = {"id_venta","id_producto"}))
public class ProductoVendido {
    @Id
    @Column(name = "id_producto_vendido", length = 36)
    public String idProductoVendido;

    @ManyToOne(optional = false) @JoinColumn(name = "id_venta", nullable = false)
    public Venta venta;

    @ManyToOne(optional = false) @JoinColumn(name = "id_producto", nullable = false)
    public Producto producto;

    public Integer cantidad;

    @Column(precision = 12, scale = 2)
    public BigDecimal precio;

    @PrePersist
    public void prePersist() {
        if (idProductoVendido == null || idProductoVendido.isBlank()) {
            idProductoVendido = java.util.UUID.randomUUID().toString();
        }
    }

    public ProductoVendido() {}
}

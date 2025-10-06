package org.cafeteria.cafeteria.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "producto_vendido",
        uniqueConstraints = @UniqueConstraint(name="uk_venta_producto",
                columnNames = {"id_venta","id_producto"}))
public class ProductoVendido {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long idProductoVendido;

    @ManyToOne(optional = false) @JoinColumn(name = "id_venta", nullable = false)
    public Venta venta;

    @ManyToOne(optional = false) @JoinColumn(name = "id_producto", nullable = false)
    public Producto producto;

    public Integer cantidad;
    public BigDecimal precio;

    public ProductoVendido() {}
}

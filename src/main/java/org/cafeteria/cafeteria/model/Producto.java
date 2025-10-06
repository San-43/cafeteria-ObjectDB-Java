package org.cafeteria.cafeteria.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity @Table(name = "productos")
public class Producto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long idProducto;

    public String descripcion;
    public BigDecimal costo;
    public BigDecimal precioVenta;

    public Producto() {}
}

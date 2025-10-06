package org.cafeteria.cafeteria.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "venta")
public class Venta {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long idVenta;

    @ManyToOne(optional = false) @JoinColumn(name = "id_tienda", nullable = false)
    public Tienda tienda;

    public LocalDateTime fecha;
    public BigDecimal total;

    public Venta() {}
}

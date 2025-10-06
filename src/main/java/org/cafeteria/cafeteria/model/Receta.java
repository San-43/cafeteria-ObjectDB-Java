package org.cafeteria.cafeteria.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity @Table(name = "receta")
public class Receta {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long idReceta;

    @ManyToOne(optional = false) @JoinColumn(name = "id_producto", nullable = false)
    public Producto producto;

    public String tamano;
    public BigDecimal costoPreparacion;

    public Receta() {}
}

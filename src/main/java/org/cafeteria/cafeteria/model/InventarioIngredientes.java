package org.cafeteria.cafeteria.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "inventario_ingredientes",
        uniqueConstraints = @UniqueConstraint(name="uk_tienda_ingrediente",
                columnNames = {"id_tienda","id_ingrediente"}))
public class InventarioIngredientes {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long idInventarioIngredientes;

    @ManyToOne(optional = false) @JoinColumn(name = "id_tienda", nullable = false)
    public Tienda tienda;

    @ManyToOne(optional = false) @JoinColumn(name = "id_ingrediente", nullable = false)
    public Ingrediente ingrediente;

    public LocalDate fechaCompra;
    public LocalDate fechaCaducidad;
    public BigDecimal costoCompra;
    public BigDecimal precioVentaPorcion;

    public InventarioIngredientes() {}
}

package org.cafeteria.cafeteria.model;

import jakarta.persistence.*;

@Entity
@Table(name = "inventario",
        uniqueConstraints = @UniqueConstraint(name="uk_inventario_tienda_producto",
                columnNames = {"id_tienda","id_producto"}))
public class Inventario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long idInventario;

    @ManyToOne(optional = false) @JoinColumn(name = "id_tienda", nullable = false)
    public Tienda tienda;

    @ManyToOne(optional = false) @JoinColumn(name = "id_producto", nullable = false)
    public Producto producto;

    public Integer stock = 0;

    public Inventario() {}
}

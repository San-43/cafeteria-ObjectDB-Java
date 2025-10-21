package org.cafeteria.cafeteria.model;

import jakarta.persistence.*;

@Entity
@Table(name = "inventario",
        uniqueConstraints = @UniqueConstraint(name="uk_inventario_tienda_producto",
                columnNames = {"id_tienda","id_producto"}))
public class Inventario {
    @Id
    @Column(name = "id_inventario", length = 36)
    public String idInventario;

    @ManyToOne(optional = false) @JoinColumn(name = "id_tienda", nullable = false)
    public Tienda tienda;

    @ManyToOne(optional = false) @JoinColumn(name = "id_producto", nullable = false)
    public Producto producto;

    @Column(name = "fecha_ingreso")
    public java.time.LocalDate fechaIngreso;

    @Column(name = "fecha_consumo")
    public java.time.LocalDate fechaConsumo;

    public Long stock = 0L;

    @PrePersist
    public void prePersist() {
        if (idInventario == null || idInventario.isBlank()) {
            idInventario = java.util.UUID.randomUUID().toString();
        }
    }

    public Inventario() {}
}

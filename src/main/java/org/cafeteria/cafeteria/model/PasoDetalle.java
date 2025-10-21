package org.cafeteria.cafeteria.model;

import jakarta.persistence.*;

@Entity
@Table(name = "paso_detalle")
public class PasoDetalle {
    @Id
    @Column(name = "id_paso_detalle", length = 36)
    public String idPasoDetalle;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_paso", nullable = false)
    public Paso paso;

    @Lob
    @Column(name = "paso_detalle", nullable = false)
    public String pasoDetalle;

    @PrePersist
    public void prePersist() {
        if (idPasoDetalle == null || idPasoDetalle.isBlank()) {
            idPasoDetalle = java.util.UUID.randomUUID().toString();
        }
    }

    public PasoDetalle() {}
}

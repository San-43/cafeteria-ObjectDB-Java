package org.cafeteria.cafeteria.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "pasos")
public class Paso {
    @Id
    @Column(name = "id_paso", length = 36)
    public String idPaso;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_receta", nullable = false)
    public Receta receta;

    @Column(name = "numero_paso", nullable = false)
    public Long numeroPaso;

    @OneToMany(mappedBy = "paso", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "orden")
    public List<PasoDetalle> detalles = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (idPaso == null || idPaso.isBlank()) {
            idPaso = UUID.randomUUID().toString();
        }
    }

    public Paso() {}
}

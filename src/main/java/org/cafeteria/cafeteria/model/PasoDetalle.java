package org.cafeteria.cafeteria.model;

import jakarta.persistence.*;

@Entity
@Table(name = "pasodetalle")
public class PasoDetalle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long idPasoDetalle;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_paso", nullable = false)
    public Paso paso;

    @Lob
    @Column(name = "paso_detalle")
    public String detalle;

    public PasoDetalle() {}
}

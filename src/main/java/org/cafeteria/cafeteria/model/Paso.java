package org.cafeteria.cafeteria.model;

import jakarta.persistence.*;

@Entity @Table(name = "paso")
public class Paso {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long idPaso;

    @ManyToOne(optional = false) @JoinColumn(name = "id_receta", nullable = false)
    public Receta receta;

    @Lob
    public String pasoDescripcion;

    public Paso() {}
}

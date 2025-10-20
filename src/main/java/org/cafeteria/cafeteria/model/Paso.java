package org.cafeteria.cafeteria.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "paso")
public class Paso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long idPaso;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_receta", nullable = false)
    public Receta receta;

    public String nombre;

    @Lob
    public String descripcion;

    @OneToMany(mappedBy = "paso", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<PasoDetalle> detalles = new ArrayList<>();

    public Paso() {}
}

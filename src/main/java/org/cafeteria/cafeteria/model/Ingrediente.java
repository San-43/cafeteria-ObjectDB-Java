package org.cafeteria.cafeteria.model;

import jakarta.persistence.*;

@Entity @Table(name = "ingredientes")
public class Ingrediente {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long idIngrediente;

    public String nombre;
    @Lob public String descripcion;
    @Lob public String preparacion;

    public Ingrediente() {}
}

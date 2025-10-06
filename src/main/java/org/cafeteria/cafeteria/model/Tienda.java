package org.cafeteria.cafeteria.model;

import jakarta.persistence.*;

@Entity @Table(name = "tienda")
public class Tienda {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long idTienda;

    public String telefono;
    public String direccion;
    public String empleadoResponsable;

    public Tienda() {}
}

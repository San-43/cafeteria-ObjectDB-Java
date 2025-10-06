package org.cafeteria.cafeteria.model;

public class Inventario {
    public Long idInventario;
    public Tienda tienda;
    public Producto producto;
    public Integer stock = 0;

    public Inventario() {
    }
}

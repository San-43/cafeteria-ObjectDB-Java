package org.cafeteria.cafeteria.model;

import java.math.BigDecimal;

public class ProductoVendido {
    public Long idProductoVendido;
    public Venta venta;
    public Producto producto;
    public Integer cantidad;
    public BigDecimal precio;

    public ProductoVendido() {
    }
}

package org.cafeteria.cafeteria.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class InventarioIngredientes {
    public Long idInventarioIngredientes;
    public Tienda tienda;
    public Ingrediente ingrediente;
    public LocalDate fechaCompra;
    public LocalDate fechaCaducidad;
    public BigDecimal costoCompra;
    public BigDecimal precioVentaPorcion;

    public InventarioIngredientes() {
    }
}

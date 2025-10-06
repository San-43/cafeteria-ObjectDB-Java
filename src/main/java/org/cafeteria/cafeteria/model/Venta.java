package org.cafeteria.cafeteria.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Venta {
    public Long idVenta;
    public Tienda tienda;
    public LocalDateTime fecha;
    public BigDecimal total;

    public Venta() {
    }
}

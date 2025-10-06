package org.cafeteria.cafeteria.config;
import org.cafeteria.cafeteria.model.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class DbBootstrap {
    public static void init() {
        var em = JPAUtil.em();
        try {
            em.getTransaction().begin();

            // Crear tienda por defecto si no hay ninguna
            long tiendas = em.createQuery("select count(t) from Tienda t", Long.class).getSingleResult();
            if (tiendas == 0) {
                Tienda t = new Tienda();
                t.telefono = "771-000-0000";
                t.direccion = "Centro, Pachuca, Hgo.";
                t.empleadoResponsable = "Encargado/a";
                em.persist(t);

                Producto p = new Producto();
                p.descripcion = "Café americano";
                p.costo = new BigDecimal("20.00");
                p.precioVenta = new BigDecimal("35.00");
                em.persist(p);

                Inventario inv = new Inventario();
                inv.tienda = t;
                inv.producto = p;
                inv.stock = 50;
                em.persist(inv);

                Venta v = new Venta();
                v.tienda = t;
                v.fecha = LocalDateTime.now();
                v.total = new BigDecimal("0.00");
                em.persist(v);

                ProductoVendido pv = new ProductoVendido();
                pv.venta = v;
                pv.producto = p;
                pv.cantidad = 1;
                pv.precio = p.precioVenta;
                em.persist(pv);

                v.total = pv.precio.multiply(BigDecimal.valueOf(pv.cantidad));

                Receta r = new Receta();
                r.producto = p;
                r.tamano = "mediano";
                r.costoPreparacion = new BigDecimal("8.00");
                em.persist(r);

                Paso paso1 = new Paso();
                paso1.receta = r;
                paso1.pasoDescripcion = "Moler/granel y extraer";
                em.persist(paso1);

                Ingrediente ing = new Ingrediente();
                ing.nombre = "Café molido";
                ing.descripcion = "Arábica";
                ing.preparacion = "Molienda media";
                em.persist(ing);

                ProporcionIngrediente pi = new ProporcionIngrediente();
                pi.receta = r;
                pi.ingrediente = ing;
                pi.proporcion = "12 g";
                em.persist(pi);

                InventarioIngredientes ii = new InventarioIngredientes();
                ii.tienda = t;
                ii.ingrediente = ing;
                ii.costoCompra = new BigDecimal("120.00");
                ii.precioVentaPorcion = new BigDecimal("5.00");
                em.persist(ii);
            }

            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
    private DbBootstrap() {}
}

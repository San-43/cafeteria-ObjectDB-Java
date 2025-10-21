package org.cafeteria.cafeteria.config;
import org.cafeteria.cafeteria.model.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class DbBootstrap {
    public static void init() {
        var em = JPAUtil.em();
        try {
            em.getTransaction().begin();

            // Crear tienda por defecto si no hay ninguna
            long tiendas = em.createQuery("select count(t) from Tienda t", Long.class).getSingleResult();
            if (tiendas == 0) {
                // Tiendas
                Tienda tiendaCentro = new Tienda();
                tiendaCentro.telefono = "771-000-0000";
                tiendaCentro.direccion = "Centro, Pachuca, Hgo.";
                tiendaCentro.empleadoResponsable = "Ana Pineda";
                em.persist(tiendaCentro);

                Tienda tiendaSur = new Tienda();
                tiendaSur.telefono = "771-111-1111";
                tiendaSur.direccion = "Blvd. Everardo Márquez, Pachuca";
                tiendaSur.empleadoResponsable = "Luis Pérez";
                em.persist(tiendaSur);

                Tienda tiendaNorte = new Tienda();
                tiendaNorte.telefono = "771-222-2222";
                tiendaNorte.direccion = "Plaza Galerías, Pachuca";
                tiendaNorte.empleadoResponsable = "María López";
                em.persist(tiendaNorte);

                // Productos
                Producto cafeAmericano = new Producto();
                cafeAmericano.nombre = "Café americano";
                cafeAmericano.descripcion = "Taza de café filtrado";
                cafeAmericano.costo = new BigDecimal("20.00");
                cafeAmericano.precioVenta = new BigDecimal("35.00");
                em.persist(cafeAmericano);

                Producto capuchino = new Producto();
                capuchino.nombre = "Capuchino";
                capuchino.descripcion = "Espresso con leche espumada";
                capuchino.costo = new BigDecimal("22.00");
                capuchino.precioVenta = new BigDecimal("38.00");
                em.persist(capuchino);

                Producto panini = new Producto();
                panini.nombre = "Panini de jamón";
                panini.descripcion = "Panini caliente de jamón y queso";
                panini.costo = new BigDecimal("30.00");
                panini.precioVenta = new BigDecimal("55.00");
                em.persist(panini);

                // Inventario por tienda
                Inventario inventarioCentro = new Inventario();
                inventarioCentro.tienda = tiendaCentro;
                inventarioCentro.producto = cafeAmericano;
                inventarioCentro.fechaIngreso = LocalDate.now().minusDays(10);
                inventarioCentro.fechaConsumo = LocalDate.now().plusWeeks(2);
                inventarioCentro.stock = 50L;
                em.persist(inventarioCentro);

                Inventario inventarioSur = new Inventario();
                inventarioSur.tienda = tiendaSur;
                inventarioSur.producto = capuchino;
                inventarioSur.fechaIngreso = LocalDate.now().minusDays(5);
                inventarioSur.fechaConsumo = LocalDate.now().plusWeeks(1);
                inventarioSur.stock = 35L;
                em.persist(inventarioSur);

                Inventario inventarioNorte = new Inventario();
                inventarioNorte.tienda = tiendaNorte;
                inventarioNorte.producto = panini;
                inventarioNorte.fechaIngreso = LocalDate.now().minusDays(3);
                inventarioNorte.fechaConsumo = LocalDate.now().plusWeeks(3);
                inventarioNorte.stock = 20L;
                em.persist(inventarioNorte);

                // Ventas
                Venta ventaCentro = new Venta();
                ventaCentro.tienda = tiendaCentro;
                ventaCentro.producto = cafeAmericano;
                ventaCentro.fecha = LocalDate.now().minusDays(2);
                ventaCentro.total = cafeAmericano.precioVenta.multiply(BigDecimal.valueOf(2));
                em.persist(ventaCentro);

                Venta ventaSur = new Venta();
                ventaSur.tienda = tiendaSur;
                ventaSur.producto = capuchino;
                ventaSur.fecha = LocalDate.now().minusDays(1);
                ventaSur.total = capuchino.precioVenta.multiply(BigDecimal.valueOf(3));
                em.persist(ventaSur);

                Venta ventaNorte = new Venta();
                ventaNorte.tienda = tiendaNorte;
                ventaNorte.producto = panini;
                ventaNorte.fecha = LocalDate.now();
                ventaNorte.total = panini.precioVenta;
                em.persist(ventaNorte);

                // Productos vendidos
                ProductoVendido ventaCafeAmericano = new ProductoVendido();
                ventaCafeAmericano.venta = ventaCentro;
                ventaCafeAmericano.producto = cafeAmericano;
                ventaCafeAmericano.cantidad = 2;
                ventaCafeAmericano.precio = cafeAmericano.precioVenta;
                em.persist(ventaCafeAmericano);

                ProductoVendido ventaCapuchino = new ProductoVendido();
                ventaCapuchino.venta = ventaSur;
                ventaCapuchino.producto = capuchino;
                ventaCapuchino.cantidad = 3;
                ventaCapuchino.precio = capuchino.precioVenta;
                em.persist(ventaCapuchino);

                ProductoVendido ventaPanini = new ProductoVendido();
                ventaPanini.venta = ventaNorte;
                ventaPanini.producto = panini;
                ventaPanini.cantidad = 1;
                ventaPanini.precio = panini.precioVenta;
                em.persist(ventaPanini);

                // Recetas
                Receta recetaCafeAmericano = new Receta();
                recetaCafeAmericano.producto = cafeAmericano;
                recetaCafeAmericano.nombre = "Café americano clásico";
                recetaCafeAmericano.tamano = "mediano";
                recetaCafeAmericano.costoPreparacion = new BigDecimal("8.00");
                em.persist(recetaCafeAmericano);

                Receta recetaCapuchino = new Receta();
                recetaCapuchino.producto = capuchino;
                recetaCapuchino.nombre = "Capuchino artesanal";
                recetaCapuchino.tamano = "grande";
                recetaCapuchino.costoPreparacion = new BigDecimal("10.00");
                em.persist(recetaCapuchino);

                Receta recetaPanini = new Receta();
                recetaPanini.producto = panini;
                recetaPanini.nombre = "Panini tradicional";
                recetaPanini.tamano = "único";
                recetaPanini.costoPreparacion = new BigDecimal("18.00");
                em.persist(recetaPanini);

                // Pasos de preparación
                Paso pasoCafe = new Paso();
                pasoCafe.receta = recetaCafeAmericano;
                pasoCafe.numeroPaso = 1L;
                PasoDetalle detalleCafe1 = new PasoDetalle();
                detalleCafe1.paso = pasoCafe;
                detalleCafe1.pasoDetalle = "Moler café fresco en molienda media y colocar en el portafiltro.";
                PasoDetalle detalleCafe2 = new PasoDetalle();
                detalleCafe2.paso = pasoCafe;
                detalleCafe2.pasoDetalle = "Extraer la bebida con agua caliente y servir inmediatamente.";
                pasoCafe.detalles.add(detalleCafe1);
                pasoCafe.detalles.add(detalleCafe2);
                em.persist(pasoCafe);

                Paso pasoCapuchino = new Paso();
                pasoCapuchino.receta = recetaCapuchino;
                pasoCapuchino.numeroPaso = 1L;
                PasoDetalle detalleCapuchino1 = new PasoDetalle();
                detalleCapuchino1.paso = pasoCapuchino;
                detalleCapuchino1.pasoDetalle = "Extraer un shot doble de espresso.";
                PasoDetalle detalleCapuchino2 = new PasoDetalle();
                detalleCapuchino2.paso = pasoCapuchino;
                detalleCapuchino2.pasoDetalle = "Espumar la leche hasta obtener microespuma y combinar con el espresso.";
                pasoCapuchino.detalles.add(detalleCapuchino1);
                pasoCapuchino.detalles.add(detalleCapuchino2);
                em.persist(pasoCapuchino);

                Paso pasoPanini = new Paso();
                pasoPanini.receta = recetaPanini;
                pasoPanini.numeroPaso = 1L;
                PasoDetalle detallePanini1 = new PasoDetalle();
                detallePanini1.paso = pasoPanini;
                detallePanini1.pasoDetalle = "Armar el pan con jamón, queso y complementos.";
                PasoDetalle detallePanini2 = new PasoDetalle();
                detallePanini2.paso = pasoPanini;
                detallePanini2.pasoDetalle = "Tostar en prensa caliente hasta dorar y derretir el queso.";
                pasoPanini.detalles.add(detallePanini1);
                pasoPanini.detalles.add(detallePanini2);
                em.persist(pasoPanini);

                // Ingredientes
                Ingrediente cafeMolido = new Ingrediente();
                cafeMolido.nombre = "Café molido";
                cafeMolido.descripcion = "Granos arábica recién molidos.";
                em.persist(cafeMolido);

                Ingrediente leche = new Ingrediente();
                leche.nombre = "Leche entera";
                leche.descripcion = "Leche pasteurizada para bebidas calientes.";
                em.persist(leche);

                Ingrediente jamon = new Ingrediente();
                jamon.nombre = "Jamón de pavo";
                jamon.descripcion = "Rebanadas delgadas para sándwiches.";
                em.persist(jamon);

                // Proporciones de ingredientes
                ProporcionIngrediente proporcionCafe = new ProporcionIngrediente();
                proporcionCafe.receta = recetaCafeAmericano;
                proporcionCafe.ingrediente = cafeMolido;
                proporcionCafe.proporcion = 12.0;
                em.persist(proporcionCafe);

                ProporcionIngrediente proporcionCapuchino = new ProporcionIngrediente();
                proporcionCapuchino.receta = recetaCapuchino;
                proporcionCapuchino.ingrediente = leche;
                proporcionCapuchino.proporcion = 150.0;
                em.persist(proporcionCapuchino);

                ProporcionIngrediente proporcionPanini = new ProporcionIngrediente();
                proporcionPanini.receta = recetaPanini;
                proporcionPanini.ingrediente = jamon;
                proporcionPanini.proporcion = 2.0;
                em.persist(proporcionPanini);

                // Inventario de ingredientes
                InventarioIngredientes inventarioCafe = new InventarioIngredientes();
                inventarioCafe.tienda = tiendaCentro;
                inventarioCafe.ingrediente = cafeMolido;
                inventarioCafe.fechaCompra = LocalDate.now().minusDays(7);
                inventarioCafe.fechaCaducidad = LocalDate.now().plusMonths(3);
                inventarioCafe.costoCompra = new BigDecimal("120.00");
                inventarioCafe.precioVentaPorcion = new BigDecimal("5.00");
                em.persist(inventarioCafe);

                InventarioIngredientes inventarioLeche = new InventarioIngredientes();
                inventarioLeche.tienda = tiendaSur;
                inventarioLeche.ingrediente = leche;
                inventarioLeche.fechaCompra = LocalDate.now().minusDays(3);
                inventarioLeche.fechaCaducidad = LocalDate.now().plusWeeks(2);
                inventarioLeche.costoCompra = new BigDecimal("80.00");
                inventarioLeche.precioVentaPorcion = new BigDecimal("6.00");
                em.persist(inventarioLeche);

                InventarioIngredientes inventarioJamon = new InventarioIngredientes();
                inventarioJamon.tienda = tiendaNorte;
                inventarioJamon.ingrediente = jamon;
                inventarioJamon.fechaCompra = LocalDate.now().minusDays(5);
                inventarioJamon.fechaCaducidad = LocalDate.now().plusWeeks(4);
                inventarioJamon.costoCompra = new BigDecimal("150.00");
                inventarioJamon.precioVentaPorcion = new BigDecimal("9.50");
                em.persist(inventarioJamon);
            }

            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
    private DbBootstrap() {}
}

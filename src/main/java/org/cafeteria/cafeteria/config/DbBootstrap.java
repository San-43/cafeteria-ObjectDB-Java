package org.cafeteria.cafeteria.config;
import org.cafeteria.cafeteria.model.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class DbBootstrap {
    public static void init() {
        var em = JPAUtil.em();
        try {
            em.getTransaction().begin();

            // Crear tienda por defecto si no hay ninguna
            long tiendas = em.createQuery("select count(t) from Tienda t", Long.class).getSingleResult();
            if (tiendas == 1) {
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
                cafeAmericano.descripcion = "Café americano";
                cafeAmericano.costo = new BigDecimal("20.00");
                cafeAmericano.precioVenta = new BigDecimal("35.00");
                em.persist(cafeAmericano);

                Producto capuchino = new Producto();
                capuchino.descripcion = "Capuchino";
                capuchino.costo = new BigDecimal("22.00");
                capuchino.precioVenta = new BigDecimal("38.00");
                em.persist(capuchino);

                Producto panini = new Producto();
                panini.descripcion = "Panini de jamón";
                panini.costo = new BigDecimal("30.00");
                panini.precioVenta = new BigDecimal("55.00");
                em.persist(panini);

                // Inventario por tienda
                Inventario inventarioCentro = new Inventario();
                inventarioCentro.tienda = tiendaCentro;
                inventarioCentro.producto = cafeAmericano;
                inventarioCentro.stock = 50;
                em.persist(inventarioCentro);

                Inventario inventarioSur = new Inventario();
                inventarioSur.tienda = tiendaSur;
                inventarioSur.producto = capuchino;
                inventarioSur.stock = 35;
                em.persist(inventarioSur);

                Inventario inventarioNorte = new Inventario();
                inventarioNorte.tienda = tiendaNorte;
                inventarioNorte.producto = panini;
                inventarioNorte.stock = 20;
                em.persist(inventarioNorte);

                // Ventas
                Venta ventaCentro = new Venta();
                ventaCentro.tienda = tiendaCentro;
                ventaCentro.fecha = LocalDateTime.now().minusDays(2);
                ventaCentro.total = BigDecimal.ZERO;
                em.persist(ventaCentro);

                Venta ventaSur = new Venta();
                ventaSur.tienda = tiendaSur;
                ventaSur.fecha = LocalDateTime.now().minusDays(1);
                ventaSur.total = BigDecimal.ZERO;
                em.persist(ventaSur);

                Venta ventaNorte = new Venta();
                ventaNorte.tienda = tiendaNorte;
                ventaNorte.fecha = LocalDateTime.now();
                ventaNorte.total = BigDecimal.ZERO;
                em.persist(ventaNorte);

                // Productos vendidos
                ProductoVendido ventaCafeAmericano = new ProductoVendido();
                ventaCafeAmericano.venta = ventaCentro;
                ventaCafeAmericano.producto = cafeAmericano;
                ventaCafeAmericano.cantidad = 2;
                ventaCafeAmericano.precio = cafeAmericano.precioVenta;
                em.persist(ventaCafeAmericano);
                ventaCentro.total = ventaCafeAmericano.precio.multiply(BigDecimal.valueOf(ventaCafeAmericano.cantidad));

                ProductoVendido ventaCapuchino = new ProductoVendido();
                ventaCapuchino.venta = ventaSur;
                ventaCapuchino.producto = capuchino;
                ventaCapuchino.cantidad = 3;
                ventaCapuchino.precio = capuchino.precioVenta;
                em.persist(ventaCapuchino);
                ventaSur.total = ventaCapuchino.precio.multiply(BigDecimal.valueOf(ventaCapuchino.cantidad));

                ProductoVendido ventaPanini = new ProductoVendido();
                ventaPanini.venta = ventaNorte;
                ventaPanini.producto = panini;
                ventaPanini.cantidad = 1;
                ventaPanini.precio = panini.precioVenta;
                em.persist(ventaPanini);
                ventaNorte.total = ventaPanini.precio.multiply(BigDecimal.valueOf(ventaPanini.cantidad));

                // Recetas
                Receta recetaCafeAmericano = new Receta();
                recetaCafeAmericano.producto = cafeAmericano;
                recetaCafeAmericano.tamano = "mediano";
                recetaCafeAmericano.costoPreparacion = new BigDecimal("8.00");
                em.persist(recetaCafeAmericano);

                Receta recetaCapuchino = new Receta();
                recetaCapuchino.producto = capuchino;
                recetaCapuchino.tamano = "grande";
                recetaCapuchino.costoPreparacion = new BigDecimal("10.00");
                em.persist(recetaCapuchino);

                Receta recetaPanini = new Receta();
                recetaPanini.producto = panini;
                recetaPanini.tamano = "único";
                recetaPanini.costoPreparacion = new BigDecimal("18.00");
                em.persist(recetaPanini);

                // Pasos de preparación
                Paso pasoCafe = new Paso();
                pasoCafe.receta = recetaCafeAmericano;
                pasoCafe.nombre = "Preparar café americano";
                pasoCafe.descripcion = "Proceso básico para extraer un café americano equilibrado.";
                PasoDetalle detalleCafe1 = new PasoDetalle();
                detalleCafe1.paso = pasoCafe;
                detalleCafe1.detalle = "Moler café fresco en molienda media y colocar en el portafiltro.";
                PasoDetalle detalleCafe2 = new PasoDetalle();
                detalleCafe2.paso = pasoCafe;
                detalleCafe2.detalle = "Extraer la bebida con agua caliente y servir inmediatamente.";
                pasoCafe.detalles.add(detalleCafe1);
                pasoCafe.detalles.add(detalleCafe2);
                em.persist(pasoCafe);

                Paso pasoCapuchino = new Paso();
                pasoCapuchino.receta = recetaCapuchino;
                pasoCapuchino.nombre = "Preparar capuchino";
                pasoCapuchino.descripcion = "Secuencia para elaborar un capuchino con espuma sedosa.";
                PasoDetalle detalleCapuchino1 = new PasoDetalle();
                detalleCapuchino1.paso = pasoCapuchino;
                detalleCapuchino1.detalle = "Extraer un shot doble de espresso.";
                PasoDetalle detalleCapuchino2 = new PasoDetalle();
                detalleCapuchino2.paso = pasoCapuchino;
                detalleCapuchino2.detalle = "Espumar la leche hasta obtener microespuma y combinar con el espresso.";
                pasoCapuchino.detalles.add(detalleCapuchino1);
                pasoCapuchino.detalles.add(detalleCapuchino2);
                em.persist(pasoCapuchino);

                Paso pasoPanini = new Paso();
                pasoPanini.receta = recetaPanini;
                pasoPanini.nombre = "Preparar panini";
                pasoPanini.descripcion = "Guía para armar y dorar un panini crujiente.";
                PasoDetalle detallePanini1 = new PasoDetalle();
                detallePanini1.paso = pasoPanini;
                detallePanini1.detalle = "Armar el pan con jamón, queso y complementos.";
                PasoDetalle detallePanini2 = new PasoDetalle();
                detallePanini2.paso = pasoPanini;
                detallePanini2.detalle = "Tostar en prensa caliente hasta dorar y derretir el queso.";
                pasoPanini.detalles.add(detallePanini1);
                pasoPanini.detalles.add(detallePanini2);
                em.persist(pasoPanini);

                // Ingredientes
                Ingrediente cafeMolido = new Ingrediente();
                cafeMolido.nombre = "Café molido";
                cafeMolido.descripcion = "Granos arábica recién molidos.";
                cafeMolido.preparacion = "Molienda media";
                em.persist(cafeMolido);

                Ingrediente leche = new Ingrediente();
                leche.nombre = "Leche entera";
                leche.descripcion = "Leche pasteurizada para bebidas calientes.";
                leche.preparacion = "Espumar antes de servir.";
                em.persist(leche);

                Ingrediente jamon = new Ingrediente();
                jamon.nombre = "Jamón de pavo";
                jamon.descripcion = "Rebanadas delgadas para sándwiches.";
                jamon.preparacion = "Mantener refrigerado";
                em.persist(jamon);

                // Proporciones de ingredientes
                ProporcionIngrediente proporcionCafe = new ProporcionIngrediente();
                proporcionCafe.receta = recetaCafeAmericano;
                proporcionCafe.ingrediente = cafeMolido;
                proporcionCafe.proporcion = "12 g";
                em.persist(proporcionCafe);

                ProporcionIngrediente proporcionCapuchino = new ProporcionIngrediente();
                proporcionCapuchino.receta = recetaCapuchino;
                proporcionCapuchino.ingrediente = leche;
                proporcionCapuchino.proporcion = "150 ml";
                em.persist(proporcionCapuchino);

                ProporcionIngrediente proporcionPanini = new ProporcionIngrediente();
                proporcionPanini.receta = recetaPanini;
                proporcionPanini.ingrediente = jamon;
                proporcionPanini.proporcion = "2 rebanadas";
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

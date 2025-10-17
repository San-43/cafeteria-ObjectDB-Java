# Cafetería — Administrador (ObjectDB + JavaFX)

Aplicación de escritorio creada con JavaFX para administrar la operación de una cadena de cafeterías. El proyecto usa ObjectDB como motor embebido de persistencia con JPA y ofrece formularios para capturar y consultar información de tiendas, productos, ingredientes, recetas, inventario y ventas.

## Características principales

- Interfaz JavaFX con un panel principal (`MainView.fxml`) que permite navegar entre diferentes formularios.
- Persistencia con ObjectDB mediante JPA; el archivo de base de datos se guarda localmente en `data/cafeteria.odb` y se inicializa automáticamente la primera vez que se ejecuta la app.
- Datos de ejemplo generados por `DbBootstrap` para comenzar con tiendas, productos, recetas, inventario y ventas precargadas.
- Formularios especializados para:
  - Tiendas, productos e inventario por sucursal.
  - Ingredientes, proporciones y pasos de preparación asociados a las recetas.
  - Registro de ventas y de productos vendidos por ticket.
- Utiliza controles adicionales (ControlsFX, ValidatorFX, TilesFX, Ikonli, BootstrapFX y FXGL) listos para extender la interfaz con validaciones y componentes enriquecidos.

## Requisitos

- Java 21 o superior (la compilación usa `--release 21`).
- Maven 3.9+ (el repositorio incluye el wrapper `mvnw` / `mvnw.cmd`).
- No es necesario instalar ObjectDB por separado; el `pom.xml` resuelve la dependencia desde el repositorio oficial.

## Cómo ejecutar la aplicación

```bash
# Limpiar, compilar y arrancar la interfaz JavaFX
./mvnw clean javafx:run
```

El plugin `javafx-maven-plugin` arranca la clase `org.cafeteria.cafeteria.MainApp`. Al iniciar, se crea/actualiza la base ObjectDB en `data/cafeteria.odb`; si el archivo no existe, `DbBootstrap` inserta los registros de ejemplo.

### Empaquetado jlink

El plugin de JavaFX está configurado para generar una imagen modular con `./mvnw javafx:jlink`. El resultado se almacena en `target/app`. Desde ahí puedes iniciar la aplicación con `target/app/bin/app`.

## Estructura del proyecto

```
src/main/java/org/cafeteria/cafeteria/
├── MainApp.java                # Punto de entrada JavaFX
├── config/                     # Configuración de JPA y carga de datos iniciales
├── controller/                 # Controladores FXML para cada formulario
└── model/                      # Entidades JPA (Tienda, Producto, Receta, etc.)

src/main/resources/
├── META-INF/persistence.xml    # Configuración de unidad de persistencia (opcional)
└── fxml/                       # Vistas JavaFX (formularios)
```

Las entidades y controladores usan paquetes separados para mantener la lógica de UI y de dominio aisladas. El formulario principal carga cada vista dentro de un `StackPane`, lo que facilita agregar nuevas secciones.

## Gestión de datos

- El gestor `JPAUtil` crea la `EntityManagerFactory`, intentando primero la unidad `CafeteriaPU` definida en `META-INF/persistence.xml` y, como alternativa, configura ObjectDB directamente.
- La clase `DbBootstrap` revisa si existen datos y, cuando la base está vacía, persiste un conjunto completo de registros (tiendas, inventario, ventas, recetas, ingredientes y proporciones) para facilitar la demostración.

El archivo `data/cafeteria.odb` se almacena junto al proyecto; puedes respaldarlo o eliminarlo para regenerar datos de ejemplo.

## Pruebas

El proyecto incluye dependencias de JUnit 5, aunque actualmente no se encuentran suites de pruebas automatizadas. Puedes ejecutar el objetivo estándar de Maven:

```bash
./mvnw test
```

## Licencia

Agrega aquí la licencia correspondiente a tu proyecto (MIT, Apache 2.0, etc.) en caso de que aún no esté definida.

# Manual Técnico - SOFT.GS

## 1. Alcance técnico

SOFT.GS es una aplicación de escritorio Java para la gestión de stock de un mayorista de artículos de computación.
La solución aplica POO y arquitectura en capas: vista, servicio, repositorio, modelo persistente y base de datos.

Este manual describe el diseño del TPI y la estructura del complemento de código. 
La ejecución real de extremo a extremo debe verificarse en el entorno del grupo antes de declarar 
la versión libre de errores.

## 2. Tecnologías de referencia

- JDK 21.
- Apache NetBeans y Maven.
- Java Swing.
- Jakarta Persistence 3.2.0.
- Hibernate ORM 7.4.15.Final (referencia incluida en el POM de complemento).
- MySQL Connector/J 9.7.0 (referencia incluida en el POM de complemento).
- MySQL Server.
- JUnit Jupiter 5.11.4.

## 3. Arquitectura en capas

```text
Swing (view)
    |
    v
Servicios (service)       <- reglas de negocio y límites transaccionales
    |
    v
Repositorios (repository) <- consultas JPQL y operaciones EntityManager
    |
    v
Hibernate / Jakarta Persistence
    |
    v
MySQL (gestion_stock)
```

- **view:** captura datos, muestra tablas, alertas y errores. No debe ejecutar SQL.
- **service:** valida operaciones de negocio y coordina transacciones.
- **repository:** encapsula persistencia/consultas y no concentra las reglas de compra/venta.
- **model:** entidades y comportamiento propio del dominio.
- **notification:** contratos y publicación de alertas Observer.
- **util:** fábrica de EntityManager y ejecución de unidades de trabajo/transacciones.

## 4. Estructura de paquetes

```text
com.gestionstock
├── SoftwareGestionStock.java
├── model
│   ├── Rubro.java
│   ├── Proveedor.java
│   ├── Cliente.java
│   ├── Articulo.java
│   ├── Compra.java
│   ├── DetalleCompra.java
│   ├── Factura.java
│   └── DetalleFactura.java
├── enums
│   ├── EstadoCompra.java
│   └── EstadoFactura.java
├── repository
│   ├── BaseRepository.java
│   ├── RubroRepository.java
│   ├── ProveedorRepository.java
│   ├── ClienteRepository.java
│   ├── ArticuloRepository.java
│   ├── CompraRepository.java
│   └── FacturaRepository.java
├── service
│   ├── RubroService.java
│   ├── ProveedorService.java
│   ├── ClienteService.java
│   ├── ArticuloService.java
│   ├── CompraService.java
│   ├── FacturaService.java
│   └── StockService.java
├── notification
│   ├── StockObserver.java
│   ├── StockNotifier.java
│   └── StockAlertService.java
├── util
│   ├── JpaUtil.java
│   ├── TransactionExecutor.java
│   └── Validaciones.java
└── view
    └── MenuPrincipal.java
```

## 5. Modelo de dominio y relaciones

| Entidad 		| Responsabilidad 			| Relaciones 															|
|   ---			|   ---					|   ---																	|
| Rubro 		| Categoría de artículos 		| Un rubro clasifica muchos artículos 									|
| Proveedor 		| Datos del proveedor 			| Un proveedor suministra artículos y registra compras 					|
| Cliente 		| Datos del comprador 			| Un cliente puede tener muchas facturas 								|
| Articulo 		| Producto, precio y stock 		| Pertenece a un rubro y proveedor; aparece en detalles de compra/venta |
| Compra 		| Cabecera de compra 			| Pertenece a un proveedor y contiene detalles 							|
| DetalleCompra 	| Línea de compra y precio histórico 	| Pertenece a una compra y referencia un artículo 						|
| Factura 		| Cabecera de venta 			| Pertenece a un cliente y contiene detalles 							|
| DetalleFactura 	| Línea de venta y precio histórico 	| Pertenece a una factura y referencia un artículo 						|

Compra–Artículo y Factura–Artículo son relaciones conceptualmente N:M resueltas con entidades de detalle.

## 6. Mapeo principal JPA / MySQL

| Clase             | Tabla  		| PK 			| FKs relevantes            |
|  ---              |  ---	 	| --- 			|  ---                      |
| Rubro             | rubros 		| id_rubro 		| -                         |
| Proveedor         | proveedores 	| id_proveedor 		| -                         |
| Cliente           | clientes 		| id_cliente 		| -                         |
| Articulo          | articulos 	| id_articulo 		| id_rubro, id_proveedor    |
| Compra            | compras 		| id_compra 		| id_proveedor              |
| DetalleCompra     | detalle_compra 	| id_detalle_compra     | id_compra, id_articulo    |
| Factura           | facturas 		| id_factura 		| id_cliente                |
| DetalleFactura    | detalle_factura 	| id_detalle_factura 	| id_factura, id_articulo   |

Las entidades utilizan `@Entity`, `@Table`, `@Id`, `@GeneratedValue(strategy = IDENTITY)` y `@Column`. 
Las relaciones se expresan con `@ManyToOne`, `@OneToMany(mappedBy=...)` y `@JoinColumn`. 
Los estados se persisten mediante `@Enumerated(EnumType.STRING)`. 
Para importes se utiliza `BigDecimal`; para fechas, `LocalDateTime`.

El lado propietario de las relaciones detalle-cabecera es el detalle (`@ManyToOne`). 
La colección de la cabecera usa `mappedBy`. 
Las cabeceras propagan persistencia/merge a los detalles y emplean `orphanRemoval` para permitir 
quitar una línea sólo en borrador. 
No se configura `CascadeType.REMOVE` indiscriminadamente, porque las operaciones históricas deben conservarse.

## 7. Esquema de base de datos

El script `gestion_stock.sql` crea:

1. `rubros`
2. `proveedores`
3. `clientes`
4. `articulos`
5. `compras`
6. `detalle_compra`
7. `facturas`
8. `detalle_factura`
9. Vista `vw_articulos_stock_bajo`

El DDL define PK, FK, índices de búsqueda, restricciones `UNIQUE`, controles `CHECK` de 
cantidades/importes/estados y motor InnoDB. 
La vista devuelve artículos activos para los que `stock_actual <= stock_minimo`, junto con las unidades faltantes.

**Contrato de esquema:** los nombres de columnas en el DDL deben coincidir con `@Column` 
y `@JoinColumn`. `persistence.xml` utiliza `hibernate.hbm2ddl.auto=validate`; 
Hibernate comprueba el esquema pero no lo crea ni actualiza.

## 8. Configuración de persistencia

Archivo: `src/main/resources/META-INF/persistence.xml`  
Unidad: `gestion-stock-pu`  
Driver: `com.mysql.cj.jdbc.Driver`  
URL de referencia: `jdbc:mysql://localhost:3306/gestion_stock`  
Credenciales: variables de entorno `MYSQL_USER` y `MYSQL_PASSWORD` leídas por `JpaUtil`.

`JpaUtil` administra una única instancia compartida de `EntityManagerFactory`. 
Cada unidad de trabajo crea su propio `EntityManager`; no se comparte un `EntityManager` entre hilos.

## 9. Repositorios

`BaseRepository<T>` concentra operaciones genéricas: guardar, actualizar, buscar por ID, listar y 
una eliminación física controlada. Las clases concretas agregan consultas de dominio:

- `RubroRepository`: búsqueda por código.
- `ProveedorRepository`: búsqueda por código y CUIT/CUIL.
- `ClienteRepository`: búsqueda por código y DNI.
- `ArticuloRepository`: búsqueda por código, stock bajo y recuperación con bloqueo pesimista.
- `CompraRepository`: recuperación con bloqueo y compras entre fechas.
- `FacturaRepository`: recuperación con bloqueo, por número, por cliente, por fechas y carga para impresión.

La baja normal de datos maestros es lógica (`activo=false`), no debe llamarse al método de 
eliminación física cuando existe historial.

## 10. Servicios y reglas de negocio

| Servicio          | Operaciones principales                                                       |
|    ---            |          ---                                                                  |
| RubroService      | Crear, modificar, desactivar, listar                                          |
| ProveedorService  | Crear, modificar, desactivar, listar                                          |
| ClienteService    | Crear, modificar, desactivar, listar                                          |
| ArticuloService   | Crear, modificar datos/precio/mínimo, desactivar, listar y stock bajo         |
| CompraService     | Crear borrador, agregar/quitar detalles, confirmar/anular, filtrar por fecha  |
| FacturaService    | Crear borrador, agregar/quitar detalles, emitir/anular, consultar e imprimir  |
| StockService      | Consultar artículos con stock bajo                                            |

### 10.1 Compra

1. Crear cabecera en `BORRADOR`.
2. Agregar detalles con cantidad y precio de compra.
3. Validar proveedor, artículos y al menos una línea.
4. Confirmar dentro de una transacción: bloquear los artículos, incrementar todo el stock, 
   cambiar el estado a `CONFIRMADA` y hacer commit.
5. Ante excepción, rollback de toda la operación.

### 10.2 Factura

1. Crear cabecera en `BORRADOR`.
2. Agregar líneas: el precio se copia del precio unitario vigente del artículo.
3. Validar todos los artículos y stock disponible antes de descontar.
4. Bloquear los artículos y descontar todo dentro de una transacción.
5. Cambiar el estado a `EMITIDA` y confirmar.
6. Si cualquier línea falla, rollback completo; no debe haber descuentos parciales.

### 10.3 Anulaciones

- Anular factura emitida repone las cantidades de sus detalles.
- La operación se protege contra una segunda anulación mediante la validación del estado.
- La implementación actual del complemento también ofrece anulación de compra confirmada, 
  revirtiendo el ingreso si hay stock suficiente. 
  El documento base presenta criterios distintos sobre este punto; debe acordarse con el docente.

## 11. Transacciones y concurrencia

`TransactionExecutor` crea un `EntityManager`, inicia la transacción, ejecuta la operación, 
hace `commit` y cierra el contexto. 
Ante una excepción de ejecución, realiza `rollback` y vuelve a propagar el error.

Los repositorios de compras, facturas y artículos aplican `LockModeType.PESSIMISTIC_WRITE` 
para bloquear registros críticos durante las operaciones de stock. 
Los bloqueos se utilizan dentro de la transacción.

## 12. Patrón Observer para stock mínimo

- `StockObserver`: contrato de notificación.
- `StockNotifier`: mantiene los observadores registrados y publica eventos.
- `StockAlertService`: observador de consola/log.
- `MenuPrincipal`: registra un observador que muestra un `JOptionPane`.

El evento se genera cuando el stock anterior era mayor que el mínimo y el nuevo es igual o inferior. 
Se publica después del commit, para no alertar por una operación revertida. 
La interfaz recibe el evento en el hilo de Swing mediante `SwingUtilities.invokeLater`.

## 13. Interfaz gráfica

`SoftwareGestionStock` inicia Swing en el Event Dispatch Thread y muestra `MenuPrincipal`. 
La ventana utiliza pestañas:

- Rubros
- Proveedores
- Clientes
- Artículos
- Compras
- Facturación
- Stock bajo
- Reportes

La GUI invoca servicios; no ejecuta SQL directamente. En la versión actual, algunos módulos reciben 
IDs numéricos manualmente. 
Los filtros de reportes usan `yyyy-MM-dd`. La impresión de facturas usa una vista de texto con 
`JTextArea.print()`; no se implementa PDF formal.

## 14. Pruebas

Pruebas unitarias disponibles:

- `ArticuloTest`: incremento, descuento inválido y stock mínimo.
- `DetalleFacturaTest`: subtotal y precio histórico.
- `CompraTest`: total y validación de borrador vacío.
- `FacturaTest`: total y retiro de línea en borrador.

Ejecutar `mvn test`. Las pruebas de integración a completar deben usar una base independiente 
y cubrir alta/relaciones, persistencia, compra, venta, stock insuficiente, rollback, anulación, 
consulta por fechas y alerta Observer.

## 15. Decisiones de diseño y fuentes

Este manual sigue la organización del documento TPI: análisis/diseño, ocho entidades, mapeo JPA, 
Maven/NetBeans, Hibernate/MySQL, compras/ventas/control de stock, Swing y pruebas. 
Las ampliaciones del complemento se identifican en las secciones correspondientes 
y no se presentan como requisitos obligatorios del documento base.
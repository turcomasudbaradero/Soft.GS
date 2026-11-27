# SOFTWARE DE GESTIÓN DE STOCK

> **Creado por GRUPO 1:** Castelli; Masud; Ortiz; Piñeyro - Para POO - UNSADA 2026.

---

## 1. Descripción del proyecto

El **Software de Gestión de Stock** es una aplicación de escritorio desarrollada en Java, aplicando los principios de la Programación Orientada a Objetos (POO).

El sistema está destinado a la administración de un mayorista de artículos de computación. Permite gestionar artículos, rubros, proveedores, clientes, compras y facturas de venta, manteniendo actualizado el inventario de manera automática.

La aplicación cuenta con una interfaz gráfica desarrollada con Java Swing y utiliza Hibernate como framework de persistencia para almacenar y recuperar la información desde una base de datos MySQL. Además, implementa el patrón de diseño Observer para notificar cuando un artículo alcanza o se encuentra por debajo de su stock mínimo.

---

## 2. Objetivos

* Aplicar los conceptos fundamentales de la Programación Orientada a Objetos.
* Desarrollar un sistema organizado en clases y paquetes.
* Implementar operaciones de alta, baja, modificación y consulta (ABM).
* Utilizar Hibernate para la persistencia de datos.
* Implementar el patrón Observer para las notificaciones de stock.
* Desarrollar una interfaz gráfica de escritorio.
* Mantener la integridad de los datos durante las operaciones de compra y venta.
* Facilitar el control y seguimiento del inventario.

---

## 3. Tecnologías utilizadas

| Tecnología | Utilización |
| :--- | :--- |
| **Java 17** | Lenguaje de programación |
| **Apache Maven** | Gestión de dependencias y construcción |
| **NetBeans** | Entorno de desarrollo |
| **Java Swing** | Desarrollo de la interfaz gráfica |
| **Hibernate 6** | Framework de persistencia y mapeo objeto-relacional |
| **Jakarta Persistence** | Anotaciones para el mapeo de entidades |
| **MySQL Server** | Sistema de gestión de base de datos |
| **MySQL Workbench** | Administración y gestión visual de la base de datos |
| **JUnit 5** | Pruebas automatizadas |

---

## 4. Funcionalidades del sistema

### 4.1. Gestión de artículos
Permite:
* Registrar artículos.
* Modificar sus datos.
* Eliminar artículos cuando no tengan dependencias que lo impidan.
* Consultar y listar artículos.
* Asignar un rubro y un proveedor.
* Establecer el stock actual y el stock mínimo.
* Definir el precio unitario.
* Consultar los artículos cuyo stock se encuentra por debajo o igual al mínimo.

### 4.2. Gestión de rubros
Permite registrar, modificar, eliminar y consultar los rubros utilizados para clasificar los artículos. Cada rubro posee un código único, nombre y descripción.

### 4.3. Gestión de proveedores
Permite administrar los datos de los proveedores, incluyendo código, CUIL/CUIT, razón social, dirección, teléfono y correo electrónico. También permite asociar proveedores a los artículos y registrar compras realizadas.

### 4.4. Gestión de clientes
Permite registrar y administrar los datos de los clientes, incluyendo código, apellido, nombre, DNI, dirección, teléfono y correo electrónico. Los clientes pueden asociarse a las facturas de venta.

### 4.5. Gestión de compras
Permite registrar compras realizadas a proveedores.

Al confirmar una compra:
* Se registra el proveedor.
* Se guardan los artículos y las cantidades adquiridas.
* Se calcula el importe correspondiente.
* Se incrementa automáticamente el stock de los artículos involucrados.
* Se conserva el registro de la operación para posteriores consultas.

También permite consultar compras y filtrar los resultados por fechas.

### 4.6. Gestión de facturas
Permite registrar y consultar facturas de venta. Durante la creación de una factura se pueden agregar o quitar artículos y establecer las cantidades correspondientes.

Al confirmar la operación:
* Se registra el cliente.
* Se guardan los artículos incluidos.
* Se conserva el precio unitario aplicado a cada detalle.
* Se calculan los subtotales y el total.
* Se descuenta automáticamente el stock.
* Se almacena la factura para su consulta e impresión posterior.

El sistema debe impedir la confirmación de una factura cuando no existe stock suficiente.

### 4.7. Control de stock y notificaciones
El sistema controla el stock de los artículos durante las operaciones que modifican el inventario. Cuando un artículo alcanza o queda por debajo del stock mínimo, se genera una notificación mediante el patrón Observer. Esto permite separar la lógica de control del inventario del mecanismo encargado de mostrar las alertas en pantalla.

---

## 5. Arquitectura del proyecto

El proyecto está organizado en paquetes para separar las responsabilidades y facilitar el mantenimiento del código.

La estructura general es:

```text
Software de Gestion de Stock/
├── pom.xml
├── README.md
└── src/
    └── main/
        ├── java/
        │   └── com/gestionstock/
        │       ├── SoftwareGestionStock/
        │       ├── util/
        │       ├── model/
        │       ├── enums/
        │       ├── notification/
        │       ├── repository/
        │       ├── service/
        │       ├── view/
        │       └── exception/
        └── resources/
            ├── META-INF/
            │   └── persistence.xml
            └── imágenes/
                └── logo.png
```

### 5.1. Paquete `SoftwareGestionStock`
Contiene la clase principal del programa.
* **Clase `Main`:** Es el punto de entrada de la aplicación. Inicializa los componentes necesarios, prepara la conexión con Hibernate, registra los observadores correspondientes y abre la ventana principal del sistema. También debe encargarse del cierre ordenado de los recursos cuando se termina la aplicación.

### 5.2. Paquete `util`
Contiene las clases responsables de la configuración y conexión con la base de datos.
* **Clase `JpaUtil`:** Centraliza la creación y administración de `SessionFactory`, el componente de Hibernate que permite abrir sesiones para trabajar con la base de datos. También proporciona el método necesario para cerrar correctamente la fábrica de sesiones cuando finaliza la aplicación.
* **Clase `TransactionExecutor`:** Centraliza la ejecución de las operaciones que modifican o consultan la base de datos mediante JPA. Se encarga de administrar las transacciones, iniciándolas antes de ejecutar una operación y confirmándolas cuando finaliza correctamente. Si ocurre algún error, realiza un rollback para deshacer los cambios y evitar que la base de datos quede en un estado inconsistente. Finalmente, cierra el `EntityManager` utilizado.
* **Clase `Validaciones`:** Centraliza las comprobaciones de los datos que ingresan al sistema antes de utilizarlos o almacenarlos. Verifica, por ejemplo, que los campos de texto obligatorios no estén vacíos, que las cantidades sean positivas o no negativas y que los importes no sean nulos ni negativos. Cuando un dato no cumple las condiciones establecidas, genera una excepción `DatosInvalidosException` con un mensaje que explica el problema.

### 5.3. Paquete `model`
Contiene las clases que representan las entidades principales del sistema. Estas clases contienen los atributos, constructores, métodos de acceso y comportamientos propios de cada objeto. Además, utilizan anotaciones de Jakarta Persistence para establecer su correspondencia con las tablas de la base de datos en MySQL.
* **Clase `Articulo`:** Representa un artículo disponible para la venta. Contiene código, descripción, rubro, proveedor, stock actual, stock mínimo y precio unitario. Incluye operaciones para aumentar y disminuir el stock, validando que no se produzcan cantidades negativas ni ventas superiores a las existencias.
* **Clase `Rubro`:** Representa una categoría de artículos. Contiene código, nombre y descripción.
* **Clase `Proveedor`:** Representa a los proveedores de mercadería. Almacena sus datos de identificación y contacto.
* **Clase `Cliente`:** Representa a los clientes del mayorista y almacena sus datos personales y de contacto.
* **Clase `Factura`:** Representa una venta realizada a un cliente. Contiene número, fecha, cliente, detalles y total.
* **Clase `DetalleFactura`:** Representa cada artículo incluido en una factura. Guarda la cantidad, el precio unitario aplicado y el subtotal correspondiente.
* **Clase `Compra`:** Representa una operación de adquisición de mercadería a un proveedor. Conserva los datos de la operación, su fecha y los detalles correspondientes.
* **Clase `DetalleCompra`:** Representa cada artículo incluido en una compra, con su cantidad y precio de adquisición.

### 5.4. Paquete `notification`
Contiene las clases que implementan el patrón de diseño Observer para controlar las notificaciones de stock mínimo.
* **Interfaz `StockObserver`:** Define el método que deben implementar los objetos interesados en recibir notificaciones cuando cambia la situación de stock de un artículo.
* **Clase `StockNotifier`:** Administra la lista de observadores. Permite agregarlos, quitarlos y notificarles cuando ocurre un evento.
* **Clase `StockAlertService`:** Implementa la interfaz `StockObserver`. Recibe la notificación y muestra una alerta con los datos del artículo que alcanzó o quedó por debajo del stock mínimo.

### 5.5. Paquete `repository`
Contiene las clases encargadas de acceder a la base de datos mediante Hibernate. Los repositorios realizan operaciones de persistencia, consultas, búsquedas, modificaciones y eliminaciones. De esta manera, el acceso a la base de datos queda separado de las reglas de negocio.

Según la entidad administrada, los repositorios realizan operaciones como:
* Guardar registros.
* Modificar registros.
* Eliminar registros.
* Buscar por identificador.
* Consultar todos los registros.
* Buscar por código.
* Consultar información mediante filtros.

Entre los repositorios del sistema se encuentran los correspondientes a artículos, rubros, proveedores, clientes, facturas y compras.

### 5.6. Paquete `service`
Contiene las clases que implementan las reglas de negocio. Los servicios coordinan las operaciones entre los repositorios y las entidades. Se encargan de validar los datos y controlar operaciones como compras, ventas y actualizaciones de stock.
* **Servicio de artículos (`ArticuloService`):** Administra los artículos, valida sus datos y controla las modificaciones de stock. También participa en la generación de notificaciones mediante Observer.
* **Servicio de rubros (`RubroService`):** Gestiona las operaciones relacionadas con los rubros.
* **Servicio de proveedores (`ProveedorService`):** Administra los proveedores y permite consultar sus datos.
* **Servicio de clientes (`ClienteService`):** Administra los clientes y permite consultar sus datos.
* **Servicio de compras (`CompraService`):** Coordina el registro de compras, la actualización del stock y la persistencia de los detalles de cada operación.
* **Servicio de facturas (`FacturaService`):** Coordina las ventas, calcula los importes, valida las existencias y descuenta el stock al confirmar una factura.

Las operaciones que afectan varias entidades deben ejecutarse dentro de una única transacción para evitar registros incompletos o inconsistencias.


### 5.7. Paquete `enums`
El paquete `enums` contiene las enumeraciones del sistema, utilizadas para representar los posibles estados de emisión de las operaciones comerciales.

### Clases que lo integran

| Clase | Descripción |
|---|---|
| `EmisionCompra.java` | Define los posibles estados de una compra, permitiendo identificar en qué situación se encuentra dentro del sistema. |
| `EmisionFactura.java` | Define los posibles estados de una factura, permitiendo distinguir las diferentes situaciones de su proceso de emisión. |

### ¿Para qué se utilizan las enumeraciones?
Las enumeraciones (`enum`) permiten trabajar con un conjunto limitado de valores predefinidos.
En SOFT.GS se utilizan para representar los estados de las operaciones comerciales, evitando el uso de textos libres y reduciendo posibles errores en la lógica del programa.

### Estructura del paquete
```text
enums/
├── EmisionCompra.java
└── EmisionFactura.java
```

### Ventajas de su implementación
- **Organización:** centralizan los estados posibles de las operaciones.
- **Seguridad:** evitan asignar valores que no estén definidos.
- **Legibilidad:** facilitan la comprensión del código.
- **Mantenimiento:** permiten modificar los estados desde un lugar específico.
- **Consistencia:** ayudan a mantener criterios uniformes en compras y facturación.

### 5.8. Paquete `view`
Contiene las clases responsables de la interfaz gráfica desarrollada con Java Swing. Las vistas fueron creadas como clases Java comunes, escribiendo el código de construcción y organización de los componentes gráficos directamente en cada clase, sin utilizar el diseñador visual de formularios de NetBeans.

Los componentes gráficos se crean y organizan directamente mediante código Java, utilizando clases de Swing como `JFrame`, `JPanel`, `JButton`, `JLabel`, `JTextField`, `JTable`, `JComboBox` y `JOptionPane`. Esto permite controlar explícitamente la creación, disposición y comportamiento de los elementos de la interfaz.

Las vistas interactúan con los servicios para ejecutar las operaciones del sistema, evitando acceder directamente a Hibernate.
* **Clase `MenuPrincipal`:** Presenta el menú principal y permite acceder a las distintas funcionalidades del sistema.
* **Vistas de artículos, rubros, proveedores y clientes:** Permiten ingresar, modificar, eliminar y consultar los datos correspondientes a cada entidad.
* **Vista de compras:** Permite seleccionar un proveedor, agregar artículos y cantidades, y confirmar una compra.
* **Vista de facturas:** Permite seleccionar un cliente, agregar o quitar artículos, indicar cantidades, calcular importes y confirmar una venta.
* **Vistas de consultas:** Presentan listados y resultados de búsquedas, incluyendo artículos con stock bajo y compras filtradas por fechas.

La interfaz se comunica con los servicios, no directamente con Hibernate. De esta manera se mantiene separada la presentación de las reglas de negocio y del acceso a datos.

### 5.9. Paquete `exception`
Contiene las excepciones personalizadas del sistema. Estas permiten representar errores relacionados con las reglas de negocio y las operaciones realizadas por los usuarios.

Estas excepciones facilitan el manejo controlado de errores y permiten mostrar mensajes comprensibles al usuario. Por ejemplo, se pueden utilizar para informar situaciones como stock insuficiente, datos inválidos, operaciones no permitidas por relaciones existentes, o registros inexistentes.
* **Clase `DatosInvalidosException`:** Se utiliza cuando los datos ingresados o utilizados por el sistema no cumplen con las condiciones necesarias. Por ejemplo, cuando se intenta registrar un artículo sin descripción, ingresar una cantidad negativa o cargar un importe inválido.
* **Clase `EntidadNoEncontradaException`:** Se utiliza cuando el sistema intenta buscar un registro que no existe en la base de datos. Por ejemplo, cuando se consulta un artículo, cliente, proveedor o factura utilizando un identificador que no se encuentra registrado.
* **Clase `StockInsuficienteException`:** Se utiliza cuando se intenta realizar una operación que requiere una cantidad de artículos superior al stock disponible. Por ejemplo, al emitir una factura cuya cantidad de productos supera las existencias actuales.
* **Clase `OperacionNoPermitidaException`:** Se utiliza cuando se intenta realizar una acción que no está permitida por las reglas del sistema. Por ejemplo, confirmar una compra sin detalles o intentar modificar una factura que ya fue emitida.

---

## 6. Persistencia con Hibernate y MySQL

El sistema utiliza Hibernate como framework de persistencia y MySQL Server como motor de base de datos. MySQL Workbench se utiliza como herramienta gráfica para administrar la base de datos, ejecutar consultas SQL y visualizar las tablas y sus relaciones.

Hibernate realiza el mapeo objeto-relacional (ORM), relacionando las clases Java con las tablas de MySQL mediante anotaciones de Jakarta Persistence.

Las entidades utilizan anotaciones como:
* `@Entity`: indica que una clase se almacena como entidad.
* `@Table`: establece el nombre de la tabla.
* `@Id`: identifica la clave primaria.
* `@GeneratedValue`: define la generación automática de identificadores.
* `@Column`: configura las columnas.
* `@ManyToOne`: representa una relación de muchos a uno.
* `@OneToMany`: representa una relación de uno a muchos.
* `@JoinColumn`: establece la columna utilizada para una relación.

La conexión se configura en `src/main/resources/META-INF/persistence.xml`, donde se establecen el controlador JDBC, la URL de conexión, las credenciales y el dialecto correspondiente a MySQL.

La clase `JpaUtil`, ubicada en el paquete `util`, centraliza la creación de `SessionFactory` y permite administrar las sesiones utilizadas por los repositorios.

---

## 7. Patrón Observer

El patrón Observer permite que un objeto notifique a otros objetos cuando ocurre un determinado evento, sin necesidad de conocer en detalle cómo reaccionará cada observador. En este proyecto se utiliza para las alertas de stock mínimo.

El funcionamiento general es:
1. Se modifica el stock de un artículo.
2. El servicio comprueba si el stock actual es menor o igual al mínimo.
3. Si se cumple la condición, se notifica a los observadores registrados.
4. `StockAlertService` recibe el artículo.
5. Se muestra una alerta en la interfaz gráfica.

Este patrón permite incorporar nuevos tipos de notificaciones sin modificar la lógica principal de actualización del stock.

---

## 8. Instalación y ejecución desde NetBeans

### Requisitos previos
* JDK 17 o compatible.
* Apache NetBeans.
* MySQL Server instalado y en funcionamiento.
* MySQL Workbench para administrar visualmente la base de datos.
* Conexión a Internet para descargar las dependencias Maven la primera vez.

### Configuración de la base de datos
1. Iniciar MySQL Server.
2. Abrir MySQL Workbench y conectarse al servidor.
3. Crear la base de datos que utilizará el proyecto, con el nombre configurado en `persistence.xml`.
4. Verificar las credenciales de conexión.
5. Confirmar que Hibernate tenga acceso a la base de datos.

### Apertura del proyecto
1. Iniciar NetBeans.
2. Seleccionar **File → Open Project**.
3. Seleccionar la carpeta raíz del proyecto, donde se encuentra `pom.xml`.
4. Esperar a que Maven descargue las dependencias.
5. Verificar que el proyecto utilice Java 17.

### Ejecución
1. Ubicar la clase `Main` dentro del paquete `SoftwareGestionStock`.
2. Hacer clic derecho sobre la clase.
3. Seleccionar **Run File**.
4. Comprobar que Hibernate establezca la conexión con MySQL y que se abra la ventana principal.

> **Nota:** Si la conexión falla, verificar que MySQL Server esté iniciado, que el nombre de la base de datos sea correcto y que las credenciales coincidan con las configuradas en `persistence.xml`.

---

## 9. Guía de pruebas de integración

### 9.1. Objetivo
Las pruebas de integración permiten comprobar que las distintas partes del sistema funcionan correctamente cuando se utilizan en conjunto.

A diferencia de las pruebas unitarias, que verifican un método o una clase de manera aislada, las pruebas de integración comprueban la interacción entre:
* Entidades.
* Servicios.
* Repositorios.
* Hibernate.
* Base de datos.
* Patrón Observer.

El objetivo principal es verificar que las operaciones realizadas desde los servicios se reflejen correctamente en la base de datos y que se respeten las reglas de negocio.

### 9.2. Preparación del entorno de pruebas
Se recomienda utilizar una base MySQL independiente para las pruebas, evitando modificar los datos de la base utilizada por la aplicación. La configuración de pruebas debe utilizar una URL diferente. Esto permite crear una base temporal en memoria.

Cada prueba debe comenzar con datos controlados y terminar dejando el entorno limpio. Para las pruebas automatizadas se utiliza **JUnit 5**.

### 9.3. Casos de prueba de integración

#### Prueba 1. Alta y consulta de un rubro
* **Objetivo:** verificar que se pueda guardar y recuperar un rubro.
* **Procedimiento:**
  1. Crear un rubro con código, nombre y descripción.
  2. Guardarlo utilizando `RubroService`.
  3. Buscarlo mediante su identificador o código.
  4. Comprobar que los datos recuperados coincidan con los ingresados.
* **Resultado esperado:** el rubro se almacena correctamente y puede consultarse desde la base de datos.

#### Prueba 2. Alta de un proveedor
* **Objetivo:** comprobar el registro y recuperación de proveedores.
* **Procedimiento:**
  1. Crear un proveedor con todos sus datos.
  2. Guardarlo.
  3. Consultarlo mediante su código.
  4. Verificar razón social, CUIL/CUIT y datos de contacto.
* **Resultado esperado:** el proveedor queda registrado y sus datos se recuperan correctamente.

#### Prueba 3. Alta de un artículo con relaciones
* **Objetivo:** comprobar que Hibernate almacene correctamente las relaciones entre entidades.
* **Procedimiento:**
  1. Crear un rubro.
  2. Crear un proveedor.
  3. Crear un artículo y asociarle ambos objetos.
  4. Guardar el artículo.
  5. Recuperarlo desde el repositorio.
  6. Verificar sus datos y sus asociaciones.
* **Resultado esperado:** el artículo queda asociado al rubro y al proveedor correctos.

#### Prueba 4. Modificación de stock
* **Objetivo:** verificar el aumento y la disminución del inventario.
* **Procedimiento:**
  1. Crear un artículo con stock inicial de 20 unidades.
  2. Aumentar el stock en 10 unidades.
  3. Guardar los cambios.
  4. Consultar el artículo.
  5. Comprobar que el stock sea 30.
  6. Disminuir el stock en 5 unidades.
  7. Guardar nuevamente.
  8. Comprobar que el stock sea 25.
* **Resultado esperado:** las modificaciones se conservan correctamente en la base de datos.

#### Prueba 5. Control de stock insuficiente
* **Objetivo:** comprobar que no se permita una salida de mercadería superior al stock disponible.
* **Procedimiento:**
  1. Crear un artículo con 5 unidades disponibles.
  2. Intentar disminuir el stock en 6 unidades.
  3. Verificar que se genere la excepción correspondiente.
  4. Consultar nuevamente el artículo.
* **Resultado esperado:** la operación se rechaza y el stock permanece en 5 unidades.

#### Prueba 6. Registro de una compra
* **Objetivo:** comprobar que una compra incremente el stock.
* **Procedimiento:**
  1. Crear un proveedor.
  2. Crear uno o más artículos.
  3. Registrar el stock inicial de cada artículo.
  4. Crear una compra asociada al proveedor.
  5. Agregar artículos y cantidades.
  6. Confirmar la compra.
  7. Consultar la compra y los artículos.
* **Resultado esperado:** la compra queda almacenada y el stock aumenta según las cantidades adquiridas.

#### Prueba 7. Consulta de compras por fecha
* **Objetivo:** verificar el filtro de compras por período.
* **Procedimiento:**
  1. Registrar varias compras con diferentes fechas.
  2. Consultar las compras entre dos fechas determinadas.
  3. Comprobar los resultados obtenidos.
* **Resultado esperado:** se muestran únicamente las compras comprendidas en el período seleccionado, respetando los límites de fecha definidos por el filtro.

#### Prueba 8. Registro de una factura de venta
* **Objetivo:** verificar que una venta descuente correctamente el stock y almacene sus detalles.
* **Procedimiento:**
  1. Crear un cliente.
  2. Crear un artículo con 20 unidades disponibles.
  3. Crear una factura para el cliente.
  4. Agregar 3 unidades del artículo.
  5. Confirmar la factura.
  6. Consultar la factura y el artículo.
* **Resultado esperado:**
  * La factura queda registrada.
  * El detalle conserva la cantidad y el precio unitario aplicado.
  * El subtotal y el total son correctos.
  * El stock del artículo pasa de 20 a 17 unidades.

#### Prueba 9. Factura con stock insuficiente
* **Objetivo:** verificar que una venta no pueda dejar el inventario en valores negativos.
* **Procedimiento:**
  1. Crear un artículo con 3 unidades.
  2. Crear una factura.
  3. Intentar vender 4 unidades.
  4. Confirmar la operación.
* **Resultado esperado:** la factura no se confirma y el stock permanece en 3 unidades. También se debe comprobar que no quede una factura parcialmente guardada.

#### Prueba 10. Notificación mediante Observer
* **Objetivo:** verificar el funcionamiento del patrón Observer.
* **Procedimiento:**
  1. Crear un artículo con stock mínimo de 5 unidades.
  2. Registrar un observador de stock.
  3. Reducir el stock de 8 a 5 unidades.
  4. Comprobar que el observador reciba la notificación.
  5. Repetir la prueba reduciendo el stock de 5 a 4 unidades.
* **Resultado esperado:** el observador recibe la notificación cuando el stock alcanza o queda por debajo del mínimo.
  *(Para automatizar esta prueba no es necesario abrir un cuadro de diálogo. Se puede registrar un observador de prueba que incremente un contador cada vez que recibe una notificación).*

#### Prueba 11. Persistencia entre ejecuciones
* **Objetivo:** verificar que los datos permanezcan almacenados después de cerrar el sistema.
* **Procedimiento:**
  1. Registrar un cliente, un proveedor y un artículo.
  2. Cerrar correctamente la aplicación.
  3. Volver a iniciar el programa.
  4. Consultar los registros creados.
* **Resultado esperado:** los datos continúan disponibles porque se almacenan en la base MySQL en modo archivo.

#### Prueba 12. Eliminación y relaciones
* **Objetivo:** verificar que no se eliminen registros que todavía tienen dependencias.
* **Procedimiento:**
  1. Crear un rubro.
  2. Asociar un artículo al rubro.
  3. Intentar eliminar el rubro.
  4. Comprobar el comportamiento del sistema.
  5. Eliminar o reasignar el artículo, si la operación está permitida.
  6. Volver a intentar la eliminación.
* **Resultado esperado:** se mantiene la integridad referencial y no se generan relaciones inválidas.

### 9.4. Ejecución de las pruebas desde NetBeans
1. Abrir el proyecto.
2. Verificar que JUnit 5 esté incluido en las dependencias de Maven.
3. Ubicar las clases de prueba dentro de `src/test/java`.
4. Ejecutar las pruebas individualmente haciendo clic derecho sobre cada clase y seleccionando **Test File**.
5. Para ejecutar todas las pruebas, hacer clic derecho sobre el proyecto y seleccionar **Test**.

También se pueden ejecutar desde una terminal ubicada en la carpeta del proyecto:

```bash
mvn clean test
```

Maven compila el proyecto y ejecuta las pruebas automatizadas.

### 9.5. Registro de resultados
Para documentar las pruebas realizadas, se recomienda utilizar una tabla como la siguiente:

| N.º | Prueba | Resultado esperado | Resultado obtenido |
| :---: | :--- | :--- | :--- |
| **1** | Alta y consulta de rubro | Registro correcto | Se logró registrar 9 rubros de manera exitosa. |
| **2** | Alta de proveedor | Registro correcto | Se registraron 3 proveedores correctamente. |
| **3** | Alta de artículo | Relaciones correctas | Los artículos ingresados se asociaron a Rubros y Proveedores correctamente. |
| **4** | Modificación de stock | Stock actualizado | Se incrementó el Stock de un artículo en 10, pasando de 20 a 30, y las modificaciones quedaron guardadas correctamente. |
| **5** | Stock insuficiente | Operación rechazada | Teniendo un artículo con stock 3 y al intentar quitar 5 unidades la operación se rechaza y el stock permanece igual. |
| **6** | Registro de compra | Stock incrementado | La compra queda registrada, almacenada y aumenta el stock del artículo. |
| **7** | Filtro por fechas | Resultados correctos | Al consultar las compras realizadas entre ciertas fechas se observan las comprendidas entre lo indicado en el filtro. |
| **8** | Registro de factura | Stock descontado | Se generó una venta con su correspondiente factura, y disminuyó el stock del artículo elegido. |
| **9** | Venta sin stock | Operación rechazada | Se quiso emitir una venta de un artículo cuyo stock es de 3 unidades y la venta requería 5. La factura no se confirma y el stock queda en 3 unidades. |
| **10** | Observer | Notificación recibida | El Observer nos devuelve una notificación de error cuando queremos disminuir el stock de un artículo por debajo del mínimo. Por ejemplo, reducir el stock de 8 a 5 unidades. |
| **11** | Persistencia | Datos conservados | Los datos quedan disponibles, ya que se almacena en la base de datos MySQL. |
| **12** | Integridad referencial | Relaciones protegidas | Se dio de baja un Rubro, y no produjo problemas de integridad referencial y no se generaron relaciones inválidas. |

---

## 10. Integridad y consistencia de los datos

Las operaciones de compra y venta modifican más de un registro de la base de datos. Por este motivo, deben manejarse mediante transacciones.

Por ejemplo, al confirmar una factura deben guardarse la factura, sus detalles y las modificaciones de stock como una única operación lógica. Si ocurre un error durante el proceso, la transacción debe revertirse para evitar que quede una factura sin detalles o un descuento de stock sin su correspondiente venta.

También se deben aplicar validaciones para evitar:
* Códigos duplicados.
* Datos obligatorios vacíos.
* Cantidades negativas o iguales a cero.
* Precios negativos.
* Ventas sin stock suficiente.
* Relaciones con entidades inexistentes.
* Eliminaciones que comprometan la integridad de los datos.

---

## 11. Consideraciones sobre la impresión de facturas

Las facturas se almacenan para permitir su consulta e impresión posterior. El sistema debe conservar en cada detalle el precio unitario utilizado al momento de la venta. De esta forma, una modificación futura del precio del artículo no altera el importe de una factura anterior.

La impresión puede realizarse utilizando las herramientas de impresión de Java, o mediante una representación preparada para imprimir desde la interfaz.

---

## 12. Entrega del proyecto

La entrega incluye:
* Código fuente completo.
* Archivo `pom.xml`.
* Archivo de configuración de Hibernate.
* Entidades y relaciones.
* Repositorios y servicios.
* Implementación del patrón Observer.
* Interfaz gráfica.
* Pruebas de integración.
* Diagrama de clases.
* Este archivo `README`.

---

## 13. Conclusión

El **Sistema de Gestión de Stock** integra los conceptos de Programación Orientada a Objetos con herramientas de persistencia y diseño de software.

La separación en paquetes permite organizar las responsabilidades de cada parte del sistema. Hibernate facilita la persistencia de las entidades, mientras que los repositorios y servicios separan el acceso a datos de las reglas de negocio. El patrón Observer permite implementar las notificaciones de stock mínimo sin acoplar la lógica del inventario con la interfaz gráfica.

Finalmente, la interfaz desarrollada con Java Swing permite utilizar las funcionalidades del sistema mediante una aplicación de escritorio, facilitando la administración de artículos, clientes, proveedores, compras y ventas.

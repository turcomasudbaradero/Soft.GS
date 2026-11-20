# Manual de Usuario - SOFT.GS

## 1. Propósito

Este manual explica cómo utilizar SOFT.GS, aplicación de escritorio para administrar 
un mayorista de artículos de computación. 
El operador puede administrar datos maestros, registrar compras, emitir facturas, 
consultar stock bajo y generar consultas por período.

## 2. Requisitos antes de iniciar

- MySQL Server en ejecución.
- Base `gestion_stock` creada y con tablas compatibles con el mapeo JPA.
- Variables `MYSQL_USER` y `MYSQL_PASSWORD` configuradas para el usuario local de Windows.
- Proyecto compilado en Apache NetBeans con JDK 21 y dependencias Maven descargadas.

Abrir el proyecto y ejecutar `com.gestionstock.SoftwareGestionStock`. La ventana principal se titula 
**SOFT.GS - Sistema de Gestión de Stock** y presenta las pestañas Rubros, Proveedores, Clientes, 
Artículos, Compras, Facturación, Stock bajo y Reportes.

## 3. Recomendación de carga inicial

Cargar la información en este orden:

1. Rubros.
2. Proveedores.
3. Clientes.
4. Artículos.
5. Compras y facturas.

La interfaz utiliza identificadores numéricos. Para conocerlos, consultar la columna ID de las tablas.

## 4. Módulo Rubros

1. Abrir la pestaña **Rubros**.
2. Completar Código, Nombre y Descripción.
3. Presionar **Guardar rubro**.
4. La tabla muestra ID, código, nombre, descripción y estado activo.
5. Para modificar, seleccionar una fila, cambiar nombre/descripción y presionar **Modificar seleccionado**.
6. Para una baja lógica, seleccionar la fila y presionar **Dar de baja**.
7. Usar **Actualizar lista** para refrescar la tabla.

El código se conserva como identificador. Un rubro inactivo no debe asignarse a artículos nuevos.

## 5. Módulo Proveedores

1. Abrir **Proveedores**.
2. Ingresar Código, CUIT/CUIL, Razón social, Dirección, Teléfono y Email.
3. Presionar **Guardar proveedor**.
4. Para modificar, seleccionar el registro y cambiar razón social/contacto.
5. Para desactivar, seleccionar y presionar **Dar de baja**.

El código y el CUIT/CUIL son identificadores únicos. No reutilizar números tributarios ya registrados.

## 6. Módulo Clientes

1. Abrir **Clientes**.
2. Completar Código, Apellido, Nombre, DNI y datos de contacto.
3. Presionar **Guardar cliente**.
4. Para actualizar datos, seleccionar el cliente y presionar **Modificar seleccionado**.
5. Para una baja lógica, seleccionar el cliente y presionar **Dar de baja**.

El DNI debe ser único. No se elimina físicamente un cliente que pueda estar asociado a facturas históricas.

## 7. Módulo Artículos

1. Antes de crear un artículo, registrar un rubro y un proveedor.
2. Abrir **Rubros** y **Proveedores** para conocer sus ID.
3. Abrir **Artículos**.
4. Completar Código, Descripción, ID rubro, ID proveedor, Stock inicial, Stock mínimo y Precio unitario.
5. Presionar **Guardar artículo**.
6. Para modificar, seleccionar una fila, corregir los datos y presionar **Modificar seleccionado**.
7. Para desactivar, seleccionar y presionar **Dar de baja**.

No se debe usar la modificación del artículo como sustituto del proceso normal de ingreso de mercadería. 
Los ingresos se registran mediante compras confirmadas. El precio debe ser mayor o igual a cero 
y el stock actual/mínimo no pueden ser negativos.

## 8. Registrar una compra

Una compra representa el ingreso de mercadería desde un proveedor.

1. Abrir **Compras**.
2. Ingresar Número de comprobante e ID proveedor.
3. Presionar **Crear borrador**. El sistema informa el ID de compra generado; anotarlo porque 
   se utiliza en los pasos siguientes.
4. Para cada artículo, ingresar ID compra, ID artículo, Cantidad y Precio compra unitario.
5. Presionar **Agregar artículo**. Repetir para cada línea.
6. Para quitar una línea antes de confirmar, indicar ID compra e ID artículo y presionar **Quitar artículo**.
7. Presionar **Confirmar compra**.
8. Consultar Artículos y comprobar que el stock aumentó.

Una compra en borrador no modifica existencias. El aumento se realiza al confirmar. 
No se permite confirmar una compra vacía ni repetir su confirmación.

**Anulación:** el complemento permite anular compras. Si estaban confirmadas, intenta revertir el ingreso; 
si las unidades ya fueron consumidas y no existe stock suficiente para la reversión, 
la operación debe rechazarse íntegramente. Esta política es una ampliación y debe confirmarse con 
el criterio del docente.

## 9. Registrar una factura de venta

1. Abrir **Facturación**.
2. Ingresar Número factura e ID cliente.
3. Presionar **Crear borrador** y anotar el ID de factura generado.
4. Ingresar ID factura, ID artículo y Cantidad.
5. Presionar **Agregar artículo**. El precio se toma del precio unitario vigente del artículo al agregar la línea.
6. Repetir para todos los productos.
7. Para quitar una línea antes de emitir, indicar ID factura e ID artículo y presionar **Quitar artículo**.
8. Presionar **Emitir factura**.
9. Consultar Artículos para comprobar el descuento de stock.

La factura debe contener al menos una línea. Si cualquiera de los artículos no tiene stock suficiente, 
no se debe emitir parcialmente: la transacción completa se revierte. 
Una factura emitida no se modifica como borrador.

## 10. Alerta de stock mínimo

La regla de consulta es `stock_actual <= stock_minimo`. La pestaña **Stock bajo** muestra ID, código, 
descripción, stock actual, stock mínimo y unidades faltantes.

La notificación Observer se presenta cuando el stock pasa de estar por encima del mínimo a estar igual o por debajo. 
No se repite en cada operación mientras el artículo sigue debajo del mínimo. 
Si el stock vuelve a superar el umbral y posteriormente lo cruza de nuevo, puede generarse otra alerta. 
La notificación se publica después del commit.

## 11. Preparar e imprimir una factura

1. Abrir **Facturación**.
2. Ingresar el ID de una factura existente en el campo correspondiente.
3. Presionar **Preparar impresión**.
4. Revisar la vista previa: número, fecha, cliente, artículos, cantidades, precios, subtotales, total y estado.
5. Confirmar **Enviar a la impresora** cuando corresponda.

La versión actual utiliza impresión de texto con `JTextArea.print()`. No genera un PDF formal. 
Para imprimir posteriormente, la factura debe continuar almacenada en MySQL.

## 12. Reportes por fecha

1. Abrir **Reportes**.
2. Ingresar fecha Desde y Hasta en formato `yyyy-MM-dd` (por ejemplo, `2026-11-01`).
3. Presionar **Consultar compras** para visualizar compras del período.
4. Presionar **Consultar facturas** para visualizar facturas del período.
5. Verificar que la fecha Desde no sea posterior a Hasta.

## 13. Mensajes y problemas habituales

| Mensaje / situación 				| Qué revisar 															|
|           ---					|                ---													|
| No se puede conectar a MySQL 			| Servidor activo, URL, usuario, contraseña y permisos 					|
| No existe el rubro/proveedor/cliente/artículo | Consultar el ID y verificar que exista 								|
| Código/DNI/CUIT duplicado 			| Utilizar un identificador no registrado 								|
| Stock insuficiente 				| Reducir la cantidad o registrar una compra 							|
| No se puede confirmar/emitar 			| Verificar estado del documento y que tenga detalles 					|
| Error de fechas 				| Usar `yyyy-MM-dd` y un rango válido 									|
| Error de esquema Hibernate 			| Comparar columnas y tipos con el DDL; `validate` no modifica tablas 	|

## 14. Buenas prácticas

- Realizar copias de seguridad de la base.
- No compartir credenciales de MySQL.
- No editar las tablas manualmente durante el uso normal.
- Registrar compras y ventas mediante los módulos correspondientes.
- Ejecutar pruebas con una base separada.
- No dar de baja artículos, clientes o proveedores si existe un motivo para conservarlos activos 
  en operaciones futuras; la baja lógica conserva el historial, pero bloquea nuevas operaciones 
  donde se valida actividad.
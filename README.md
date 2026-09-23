# TP JPQL — Consultas Avanzadas (Facturación)

![Java](https://img.shields.io/badge/Java-17+-orange)
![Hibernate](https://img.shields.io/badge/Hibernate-5.6-59666C)
![PostgreSQL](https://img.shields.io/badge/DB-PostgreSQL-336791)
![Maven](https://img.shields.io/badge/Build-Maven-C71A36)

Trabajo práctico de **JPQL** (Java Persistence Query Language) sobre el modelo de
facturación del [TP de JPA](https://github.com/giovanniclmb/jpa-facturacion).
Resuelve **20 consultas en 5 niveles**: filtros, agregaciones, JOINs, agrupamiento y
subconsultas correlacionadas, escritas sobre **entidades y atributos Java** y
ejecutadas con `em.createQuery(jpql, Clase.class)` y parámetros con nombre.

## Stack

| Tecnología | Rol |
|---|---|
| Java 17+ | Lenguaje |
| JPA 2.2 (namespace `javax`) | Especificación de persistencia y JPQL |
| Hibernate 5.6 | Implementación de JPA (traduce JPQL a SQL) |
| PostgreSQL | Base de datos relacional |
| Maven | Gestión de dependencias y build |

## Estructura

| Clase | Qué hace |
|---|---|
| `CargaDatos` | Carga un dataset de prueba (8 facturas) diseñado para que cada consulta tenga algo que demostrar. Es idempotente: si ya hay datos, no vuelve a insertar. |
| `ConsultasJPQL` | Las 20 consultas, un método por consulta. |
| `MainJPQL` | Punto de entrada: carga los datos y ejecuta las 20 consultas. |
| `model/` | Las 14 entidades mapeadas en el TP de JPA. |

## Consultas

| Nivel | # | Consulta | Concepto JPQL |
|---|---|---|---|
| 1 | 1 | Todas las facturas | `SELECT` de entidades |
| 1 | 2 | Número, fecha y total de cada factura | Proyección con `Object[]` |
| 1 | 3 | Artículos de un rubro | `WHERE` por igualdad + parámetro con nombre |
| 1 | 4 | Facturas en un rango de fechas | `BETWEEN` con `TemporalType` |
| 2 | 5 | Facturas emitidas, > $10.000 y no anuladas | `AND` + `IS NULL` |
| 2 | 6 | Clientes por texto parcial o prefijo de CUIT | `LIKE` + `LOWER` + `OR` |
| 2 | 7 | Estados distintos, ordenados | `DISTINCT` + `ORDER BY` |
| 2 | 8 | Cantidad, suma y promedio de facturas | `COUNT` / `SUM` / `AVG` con `getSingleResult()` |
| 2 | 9 | Puntos de venta de una lista de números | `IN` con lista por parámetro |
| 3 | 10 | Facturas cargadas por un usuario | Navegación implícita (`f.usuarioCarga.usuario`) |
| 3 | 11 | Detalles de un punto de venta | `INNER JOIN` explícito |
| 3 | 12 | Artículos con su marca (incluye sin marca) | `LEFT JOIN` |
| 3 | 13 | Facturas con artículos de una marca | JOINs multinivel + `DISTINCT` |
| 3 | 14 | Facturas sobre el promedio | Subconsulta en `WHERE` |
| 4 | 15 | Facturas y total por punto de venta | `GROUP BY` |
| 4 | 16 | Usuarios con más de 5 facturas | `GROUP BY` + `HAVING` |
| 4 | 17 | Unidades y subtotal por marca | Agrupamiento sobre entidades relacionadas |
| 5 | 18 | Marcas con al menos un artículo facturado | Subconsulta correlacionada con `EXISTS` |
| 5 | 19 | Artículos nunca facturados | Subconsulta correlacionada con `NOT EXISTS` |
| 5 | 20 | Facturas clasificadas por importe | `CASE WHEN` |

## Cómo ejecutarlo

**Requisitos:** JDK 17+, Maven y PostgreSQL instalados y corriendo.

**1. Clonar y crear la base:**

```bash
git clone https://github.com/giovanniclmb/jpql-facturacion.git
cd jpql-facturacion
createdb facturacion
```

**2. Ajustar credenciales** en `src/main/resources/META-INF/persistence.xml`
(`javax.persistence.jdbc.user` y `.password`) según tu PostgreSQL local.

**3. Compilar y ejecutar:**

```bash
mvn compile exec:java -Dexec.mainClass="com.tp.facturacion.MainJPQL"
```

La primera corrida crea las tablas y carga los datos de prueba. Las siguientes
detectan que ya hay datos y ejecutan directamente las consultas.

> Si usás JDK 25 o superior y aparece un error de Byte Buddy, agregá
> `-Dnet.bytebuddy.experimental=true` al comando.

## Autor

Giovanni — [github.com/giovanniclmb](https://github.com/giovanniclmb)

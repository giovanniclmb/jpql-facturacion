# TP JPA — Facturación (ORM con Hibernate + PostgreSQL)

![Java](https://img.shields.io/badge/Java-17+-orange)
![Hibernate](https://img.shields.io/badge/Hibernate-5.6-59666C)
![PostgreSQL](https://img.shields.io/badge/DB-PostgreSQL-336791)
![Maven](https://img.shields.io/badge/Build-Maven-C71A36)

Trabajo práctico de mapeo objeto-relacional (**ORM**) con **JPA/Hibernate** sobre un
modelo de dominio de **facturación electrónica**. Implementa el mapeo completo de las
entidades, la configuración de la unidad de persistencia hacia PostgreSQL, y la
persistencia en cascada desde un método `main`.

## Stack

| Tecnología | Rol |
|---|---|
| Java 17+ | Lenguaje |
| JPA (Jakarta Persistence 2.2, namespace `javax`) | Especificación de persistencia |
| Hibernate 5.6 | Implementación de JPA (ORM) |
| PostgreSQL | Base de datos relacional |
| Maven | Gestión de dependencias y build |

## Modelo de dominio

Todas las entidades heredan de dos superclases mapeadas (`@MappedSuperclass`):
- **`EntityId`** → aporta el `id` (clave primaria autogenerada).
- **`AuditoriaApp`** (extiende `EntityId`) → agrega campos de auditoría: fechas
  (`@Temporal`) y usuarios de carga/baja/modificación (`@ManyToOne` a `Usuario`).

**Entidades (14):** `Usuario`, `FacturaVenta`, `FacturaVentaDetalle`, `PuntoVenta`,
`Cliente`, `Contacto`, `Domicilio`, `Articulo`, `ListaPrecio`, `ListaPrecioArticulo`,
`Marca`, `Rubro`, `TipoMoneda`, `CondicionIva`.

**Relaciones principales:**

| Relación | Tipo |
|---|---|
| FacturaVenta → FacturaVentaDetalle | `@OneToMany` (composición, `cascade = ALL`) |
| Cliente → Contacto / Domicilio | `@OneToOne` |
| FacturaVentaDetalle → ListaPrecioArticulo | `@ManyToOne` |
| ListaPrecioArticulo → ListaPrecio / Articulo | `@ManyToOne` |
| Articulo → Rubro / Marca | `@ManyToOne` |

## Cómo ejecutarlo

**Requisitos:** JDK 17+, Maven y PostgreSQL instalados.

**1. Clonar y crear la base:**
```bash
git clone https://github.com/giovanniclmb/jpa-facturacion.git
cd jpa-facturacion
createdb facturacion
```

**2. Ajustar credenciales** en `src/main/resources/META-INF/persistence.xml`
(propiedades `javax.persistence.jdbc.user` y `.password`) según tu PostgreSQL local.

**3. Compilar y ejecutar:**
```bash
mvn compile
```
Luego ejecutar `com.tp.facturacion.Main` desde el IDE. Al correr, Hibernate crea las
14 tablas (`hbm2ddl.auto=update`) y persiste una factura con sus detalles mediante un
único `em.persist` (cascada).

> La clase `com.tp.facturacion.TestConexion` permite probar solo la conexión y la
> creación del esquema, sin insertar datos.

## Conceptos JPA aplicados

Herencia encadenada con `@MappedSuperclass` · claves primarias autogeneradas
(`@GeneratedValue`) · mapeo temporal (`@Temporal`) · restricciones de obligatoriedad
(`@Column(nullable = false)`) · relaciones `@ManyToOne`, `@OneToMany` (con `mappedBy`)
y `@OneToOne` · persistencia en cascada · asociación bidireccional · unidad de
persistencia `RESOURCE_LOCAL` con transacciones manuales.

## 👤 Autor

Giovanni — [github.com/giovanniclmb](https://github.com/giovanniclmb)
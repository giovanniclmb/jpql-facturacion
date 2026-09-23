package com.tp.facturacion;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TemporalType;

import com.tp.facturacion.model.*;

public class ConsultasJPQL {

    private final EntityManager em;
    private final SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");

    public ConsultasJPQL(EntityManager em) {
        this.em = em;
    }

    // ======================= NIVEL 1 =======================

    // 1. Consulta de entidades completas
    public void consulta01() {
        titulo("1. Todas las facturas de venta");

        List<FacturaVenta> facturas = em.createQuery(
                "SELECT f FROM FacturaVenta f", FacturaVenta.class)
                .getResultList();

        for (FacturaVenta f : facturas) {
            imprimirFactura(f);
        }
    }

    // 2. Proyección de atributos específicos
    public void consulta02() {
        titulo("2. Número, fecha de emisión e importe total de cada factura");

        List<Object[]> filas = em.createQuery(
                "SELECT f.numero, f.fechaEmision, f.importeTotal FROM FacturaVenta f",
                Object[].class)
                .getResultList();

        for (Object[] fila : filas) {
            Long numero = (Long) fila[0];
            Date fechaEmision = (Date) fila[1];
            Double importeTotal = (Double) fila[2];
            System.out.println("Nº " + numero + " | " + formato.format(fechaEmision) + " | $" + importeTotal);
        }
    }

    // 3. Filtrado por igualdad (WHERE)
    public void consulta03(String denominacionRubro) {
        titulo("3. Artículos del rubro '" + denominacionRubro + "'");

        List<Articulo> articulos = em.createQuery(
                "SELECT a FROM Articulo a WHERE a.rubro.denominacion = :rubro",
                Articulo.class)
                .setParameter("rubro", denominacionRubro)
                .getResultList();

        for (Articulo a : articulos) {
            System.out.println(a.getCodigo() + " - " + a.getDenominacion());
        }
    }

    // 4. Filtrado por rango de fechas (BETWEEN)
    public void consulta04(Date desde, Date hasta) {
        titulo("4. Facturas emitidas entre " + formato.format(desde) + " y " + formato.format(hasta));

        List<FacturaVenta> facturas = em.createQuery(
                "SELECT f FROM FacturaVenta f WHERE f.fechaEmision BETWEEN :desde AND :hasta",
                FacturaVenta.class)
                .setParameter("desde", desde, TemporalType.TIMESTAMP)
                .setParameter("hasta", hasta, TemporalType.TIMESTAMP)
                .getResultList();

        for (FacturaVenta f : facturas) {
            imprimirFactura(f);
        }
    }

        // ======================= NIVEL 2 =======================

    // 5. Condicionales complejos y verificación de nulos (AND, IS NULL)
    public void consulta05(String estado, double importeMinimo) {
        titulo("5. Facturas '" + estado + "' con total > $" + importeMinimo + " y no anuladas");

        List<FacturaVenta> facturas = em.createQuery(
                "SELECT f FROM FacturaVenta f "
                + "WHERE f.estado = :estado "
                + "AND f.importeTotal > :minimo "
                + "AND f.fechaAnulacion IS NULL",
                FacturaVenta.class)
                .setParameter("estado", estado)
                .setParameter("minimo", importeMinimo)
                .getResultList();

        for (FacturaVenta f : facturas) {
            imprimirFactura(f);
        }
    }

    // 6. Búsqueda por patrón de texto (LIKE, LOWER, OR)
    public void consulta06(String textoDenominacion, String prefijoCuit) {
        titulo("6. Clientes con '" + textoDenominacion + "' en el nombre o CUIT que empiece con '" + prefijoCuit + "'");

        List<Cliente> clientes = em.createQuery(
                "SELECT c FROM Cliente c "
                + "WHERE LOWER(c.denominacion) LIKE LOWER(:patron) "
                + "OR c.cuitCuil LIKE :prefijo",
                Cliente.class)
                .setParameter("patron", "%" + textoDenominacion + "%")
                .setParameter("prefijo", prefijoCuit + "%")
                .getResultList();

        for (Cliente c : clientes) {
            System.out.println(c.getDenominacion() + " | CUIT " + c.getCuitCuil());
        }
    }

    // 7. Valores distintos y ordenamiento (DISTINCT, ORDER BY)
    public void consulta07() {
        titulo("7. Estados distintos de las facturas (orden alfabético)");

        List<String> estados = em.createQuery(
                "SELECT DISTINCT f.estado FROM FacturaVenta f ORDER BY f.estado ASC",
                String.class)
                .getResultList();

        for (String estado : estados) {
            System.out.println(estado);
        }
    }

    // 8. Funciones de agregación simples (COUNT, SUM, AVG)
    public void consulta08(String estado) {
        titulo("8. Cantidad, suma y promedio de las facturas '" + estado + "'");

        Object[] resultado = em.createQuery(
                "SELECT COUNT(f), SUM(f.importeTotal), AVG(f.importeTotal) "
                + "FROM FacturaVenta f WHERE f.estado = :estado",
                Object[].class)
                .setParameter("estado", estado)
                .getSingleResult();

        Long cantidad = (Long) resultado[0];
        Double suma = (Double) resultado[1];
        Double promedio = (Double) resultado[2];
        System.out.println("Cantidad: " + cantidad + " | Suma: $" + suma + " | Promedio: $" + promedio);
    }

    // 9. Operador de inclusión (IN)
    public void consulta09(List<Integer> numeros) {
        titulo("9. Puntos de venta con número en " + numeros);

        List<PuntoVenta> puntos = em.createQuery(
                "SELECT pv FROM PuntoVenta pv WHERE pv.numero IN (:numeros)",
                PuntoVenta.class)
                .setParameter("numeros", numeros)
                .getResultList();

        for (PuntoVenta pv : puntos) {
            System.out.println(pv.getNumero() + " - " + pv.getDescripcion());
        }
    }

        // ======================= NIVEL 3 =======================

    // 10. Navegación implícita por relaciones (path expressions)
    public void consulta10(String nombreUsuario) {
        titulo("10. Facturas cargadas por el usuario '" + nombreUsuario + "'");

        List<FacturaVenta> facturas = em.createQuery(
                "SELECT f FROM FacturaVenta f WHERE f.usuarioCarga.usuario = :usuario",
                FacturaVenta.class)
                .setParameter("usuario", nombreUsuario)
                .getResultList();

        for (FacturaVenta f : facturas) {
            imprimirFactura(f);
        }
    }

    // 11. INNER JOIN explícito
    public void consulta11(int numeroPuntoVenta) {
        titulo("11. Detalles de facturas del punto de venta Nº " + numeroPuntoVenta);

        List<FacturaVentaDetalle> detalles = em.createQuery(
                "SELECT d FROM FacturaVentaDetalle d "
                + "JOIN d.factura f "
                + "JOIN f.puntoVenta pv "
                + "WHERE pv.numero = :numero",
                FacturaVentaDetalle.class)
                .setParameter("numero", numeroPuntoVenta)
                .getResultList();

        for (FacturaVentaDetalle d : detalles) {
            System.out.println("Factura Nº " + d.getFactura().getNumero()
                    + " | " + d.getDescripcion()
                    + " | Cant: " + d.getCantidad()
                    + " | Subtotal: $" + d.getImporteSubtotal());
        }
    }

    // 12. LEFT JOIN (inclusión de nulos)
    public void consulta12() {
        titulo("12. Artículos con su marca (incluye los que no tienen marca)");

        List<Object[]> filas = em.createQuery(
                "SELECT a.denominacion, m.denominacion "
                + "FROM Articulo a LEFT JOIN a.marca m",
                Object[].class)
                .getResultList();

        for (Object[] fila : filas) {
            String articulo = (String) fila[0];
            String marca = (String) fila[1];
            System.out.println(articulo + " | Marca: " + (marca != null ? marca : "(sin marca)"));
        }
    }

    // 13. Navegación multinivel con JOINs combinados
    public void consulta13(String denominacionMarca) {
        titulo("13. Facturas con al menos un artículo de la marca '" + denominacionMarca + "'");

        List<FacturaVenta> facturas = em.createQuery(
                "SELECT DISTINCT f FROM FacturaVenta f "
                + "JOIN f.detalles d "
                + "JOIN d.listaPrecioArticulo lpa "
                + "JOIN lpa.articulo a "
                + "JOIN a.marca m "
                + "WHERE m.denominacion = :marca "
                + "ORDER BY f.numero",
                FacturaVenta.class)
                .setParameter("marca", denominacionMarca)
                .getResultList();

        for (FacturaVenta f : facturas) {
            imprimirFactura(f);
        }
    }

    // 14. Subconsulta en la cláusula WHERE
    public void consulta14() {
        titulo("14. Facturas con importe total mayor al promedio");

        List<FacturaVenta> facturas = em.createQuery(
                "SELECT f FROM FacturaVenta f "
                + "WHERE f.importeTotal > (SELECT AVG(f2.importeTotal) FROM FacturaVenta f2)",
                FacturaVenta.class)
                .getResultList();

        for (FacturaVenta f : facturas) {
            imprimirFactura(f);
        }
    }

        // ======================= NIVEL 4 =======================

    // 15. Agrupamiento básico (GROUP BY)
    public void consulta15() {
        titulo("15. Cantidad de facturas y total facturado por punto de venta");

        List<Object[]> filas = em.createQuery(
                "SELECT pv.descripcion, COUNT(f), SUM(f.importeTotal) "
                + "FROM FacturaVenta f JOIN f.puntoVenta pv "
                + "GROUP BY pv.numero, pv.descripcion "
                + "ORDER BY pv.numero",
                Object[].class)
                .getResultList();

        for (Object[] fila : filas) {
            String puntoVenta = (String) fila[0];
            Long cantidad = (Long) fila[1];
            Double total = (Double) fila[2];
            System.out.println(puntoVenta + " | Facturas: " + cantidad + " | Total: $" + total);
        }
    }

    // 16. Agrupamiento con condición de grupo (HAVING)
    public void consulta16(long minimoFacturas) {
        titulo("16. Usuarios de carga con más de " + minimoFacturas + " facturas");

        List<Object[]> filas = em.createQuery(
                "SELECT u.usuario, u.nombre, u.apellido, COUNT(f) "
                + "FROM FacturaVenta f JOIN f.usuarioCarga u "
                + "GROUP BY u.id, u.usuario, u.nombre, u.apellido "
                + "HAVING COUNT(f) > :minimo",
                Object[].class)
                .setParameter("minimo", minimoFacturas)
                .getResultList();

        for (Object[] fila : filas) {
            System.out.println(fila[0] + " (" + fila[1] + " " + fila[2] + ") | Facturas: " + fila[3]);
        }
    }

    // 17. Agrupamiento y agregación sobre entidades relacionadas
    public void consulta17() {
        titulo("17. Unidades vendidas y subtotal acumulado por marca");

        List<Object[]> filas = em.createQuery(
                "SELECT m.denominacion, SUM(d.cantidad), SUM(d.importeSubtotal) "
                + "FROM FacturaVentaDetalle d "
                + "JOIN d.listaPrecioArticulo lpa "
                + "JOIN lpa.articulo a "
                + "JOIN a.marca m "
                + "GROUP BY m.denominacion "
                + "ORDER BY m.denominacion",
                Object[].class)
                .getResultList();

        for (Object[] fila : filas) {
            String marca = (String) fila[0];
            Double unidades = (Double) fila[1];
            Double subtotal = (Double) fila[2];
            System.out.println(marca + " | Unidades: " + unidades + " | Subtotal: $" + subtotal);
        }
    }

        // ======================= NIVEL 5 =======================

    // 18. Subconsulta correlacionada con EXISTS
    public void consulta18() {
        titulo("18. Marcas con al menos un artículo facturado");

        List<Marca> marcas = em.createQuery(
                "SELECT m FROM Marca m "
                + "WHERE EXISTS ("
                + "    SELECT d FROM FacturaVentaDetalle d "
                + "    WHERE d.listaPrecioArticulo.articulo.marca = m"
                + ") "
                + "ORDER BY m.denominacion",
                Marca.class)
                .getResultList();

        for (Marca m : marcas) {
            System.out.println(m.getDenominacion());
        }
    }

    // 19. Subconsulta correlacionada con NOT EXISTS
    public void consulta19() {
        titulo("19. Artículos que nunca fueron facturados");

        List<Articulo> articulos = em.createQuery(
                "SELECT a FROM Articulo a "
                + "WHERE NOT EXISTS ("
                + "    SELECT d FROM FacturaVentaDetalle d "
                + "    WHERE d.listaPrecioArticulo.articulo = a"
                + ")",
                Articulo.class)
                .getResultList();

        for (Articulo a : articulos) {
            System.out.println(a.getCodigo() + " - " + a.getDenominacion());
        }
    }

    // 20. Proyección condicional (CASE WHEN)
    public void consulta20() {
        titulo("20. Facturas clasificadas por categoría de importe");

        List<Object[]> filas = em.createQuery(
                "SELECT f.numero, f.importeTotal, "
                + "CASE "
                + "    WHEN f.importeTotal > 50000 THEN 'ALTO VALOR' "
                + "    WHEN f.importeTotal >= 10000 THEN 'MEDIO VALOR' "
                + "    ELSE 'BAJO VALOR' "
                + "END AS categoria "
                + "FROM FacturaVenta f "
                + "ORDER BY f.importeTotal DESC",
                Object[].class)
                .getResultList();

        for (Object[] fila : filas) {
            Long numero = (Long) fila[0];
            Double total = (Double) fila[1];
            String categoria = (String) fila[2];
            System.out.println("Nº " + numero + " | $" + total + " | " + categoria);
        }
    }

    // ======================= Helpers =======================

    public static Date fecha(int anio, int mes, int dia) {
        return Date.from(LocalDate.of(anio, mes, dia)
                .atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private void titulo(String texto) {
        System.out.println("\n========== " + texto + " ==========");
    }

    private void imprimirFactura(FacturaVenta f) {
        System.out.println("Nº " + f.getNumero()
                + " | " + formato.format(f.getFechaEmision())
                + " | " + f.getEstado()
                + " | $" + f.getImporteTotal());
    }
}
package com.tp.facturacion;

import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.tp.facturacion.model.*;

public class Main {

    public static void main(String[] args) {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("FacturacionPU");
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();

            Date ahora = new Date();

            //datos maestros

            // Usuario: audita a todos los demás. No lleva auditoría propia.
            Usuario admin = new Usuario();
            admin.setUsuario("admin");
            admin.setClave("1234");
            admin.setNombre("Giovanni");
            admin.setApellido("Colombo");
            em.persist(admin);

            PuntoVenta puntoVenta = new PuntoVenta();
            puntoVenta.setNumero(1);
            puntoVenta.setDescripcion("Casa Central");
            puntoVenta.setTipoEmision("ELECTRONICA");
            puntoVenta.setDomicilioComercial("Av. Siempreviva 742");
            auditar(puntoVenta, admin, ahora);
            em.persist(puntoVenta);

            Rubro rubro = new Rubro();
            rubro.setDenominacion("Tecnologia");
            rubro.setCodigo(100);
            auditar(rubro, admin, ahora);
            em.persist(rubro);

            Marca marca = new Marca();
            marca.setDenominacion("Generica");
            marca.setCodigo(200);
            auditar(marca, admin, ahora);
            em.persist(marca);

            Articulo articulo = new Articulo();
            articulo.setCodigo("ART001");
            articulo.setDenominacion("Teclado mecanico");
            articulo.setRubro(rubro);
            articulo.setMarca(marca);
            auditar(articulo, admin, ahora);
            em.persist(articulo);

            ListaPrecio listaPrecio = new ListaPrecio();
            listaPrecio.setCodigo("LP2026");
            listaPrecio.setDenominacion("Lista general 2026");
            auditar(listaPrecio, admin, ahora);
            em.persist(listaPrecio);

            ListaPrecioArticulo lpa = new ListaPrecioArticulo();
            lpa.setListaPrecio(listaPrecio);
            lpa.setArticulo(articulo);
            lpa.setPrecioVenta(25000.0);
            auditar(lpa, admin, ahora);
            em.persist(lpa);

            // ===== FASE 2: LA FACTURA (cabecera + detalles) =====

            FacturaVenta factura = new FacturaVenta();
            factura.setNumero(1L);
            factura.setFechaEmision(ahora);
            factura.setPuntoVenta(puntoVenta);
            factura.setEstado("EMITIDA");
            factura.setImporteCobrado(0.0);
            auditar(factura, admin, ahora);

            // Detalle 1 — asociado BIDIRECCIONALMENTE con el helper
            FacturaVentaDetalle d1 = new FacturaVentaDetalle();
            d1.setListaPrecioArticulo(lpa);
            d1.setDescripcion("Teclado mecanico");
            d1.setCantidad(2);
            d1.setPrecioUnitario(25000.0);
            d1.setImporteNeto(50000.0);
            d1.setImporteIva(10500.0);
            d1.setImporteSubtotal(60500.0);
            factura.addDetalle(d1);   // agrega a la lista Y setea d1.factura = factura

            // Detalle 2
            FacturaVentaDetalle d2 = new FacturaVentaDetalle();
            d2.setListaPrecioArticulo(lpa);
            d2.setDescripcion("Teclado mecanico (unidad extra)");
            d2.setCantidad(1);
            d2.setPrecioUnitario(25000.0);
            d2.setImporteNeto(25000.0);
            d2.setImporteIva(5250.0);
            d2.setImporteSubtotal(30250.0);
            factura.addDetalle(d2);

            factura.setImporteTotal(60500.0 + 30250.0);
            factura.setImporteSaldo(60500.0 + 30250.0);

            // UN SOLO persist. La cascada guarda los detalles
            em.persist(factura);

            em.getTransaction().commit();
            System.out.println(">>> Factura y detalles persistidos con un solo em.persist (cascade). OK");

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
            emf.close();
        }
    }

    // Setea los 4 campos de auditoría obligatorios de cualquier AuditoriaApp
    private static void auditar(AuditoriaApp entidad, Usuario usuario, Date fecha) {
        entidad.setFechaAlta(fecha);
        entidad.setFechaModificacion(fecha);
        entidad.setUsuarioCarga(usuario);
        entidad.setUsuarioModificacion(usuario);
    }
}

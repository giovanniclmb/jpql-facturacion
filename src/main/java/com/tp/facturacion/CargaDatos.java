package com.tp.facturacion;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import javax.persistence.EntityManager;

import com.tp.facturacion.model.*;

public class CargaDatos {

    private final EntityManager em;
    private final Date ahora = new Date();
    private TipoMoneda pesos;

    public CargaDatos(EntityManager em) {
        this.em = em;
    }

    public void cargar() {
        // Si ya hay facturas, no volvemos a cargar (evita duplicados)
        Long existentes = em.createQuery(
                "SELECT COUNT(f) FROM FacturaVenta f", Long.class).getSingleResult();
        if (existentes > 0) {
            System.out.println(">>> La base ya tiene datos: se omite la carga.");
            return;
        }

        em.getTransaction().begin();

        // ---------- Usuarios ----------
        Usuario admin = crearUsuario("admin", "Giovanni", "Perez");
        Usuario vendedor = crearUsuario("vendedor", "Laura", "Diaz");

        // ---------- Puntos de venta (números 1, 2, 3 y 5) ----------
        PuntoVenta pvCentral = crearPuntoVenta(1, "Casa Central", admin);
        PuntoVenta pvGodoyCruz = crearPuntoVenta(2, "Sucursal Godoy Cruz", admin);
        PuntoVenta pvLujan = crearPuntoVenta(3, "Sucursal Lujan", admin);
        PuntoVenta pvMaipu = crearPuntoVenta(5, "Sucursal Maipu", admin);

        // ---------- Rubros y marcas ----------
        Rubro electronica = crearRubro(100, "Electrónica", admin);
        Rubro hogar = crearRubro(200, "Hogar", admin);

        Marca logitech = crearMarca(1, "Logitech", admin);
        Marca samsung = crearMarca(2, "Samsung", admin);
        Marca philips = crearMarca(3, "Philips", admin); // nunca se vende

        // ---------- Artículos ----------
        Articulo teclado = crearArticulo("ART001", "Teclado mecanico", electronica, logitech, admin);
        Articulo mouse = crearArticulo("ART002", "Mouse inalambrico", electronica, logitech, admin);
        Articulo pad = crearArticulo("ART003", "Pad para mouse", electronica, logitech, admin);
        Articulo monitor = crearArticulo("ART004", "Monitor 27 pulgadas", electronica, samsung, admin);
        Articulo licuadora = crearArticulo("ART005", "Licuadora", hogar, philips, admin); // nunca facturada
        Articulo cable = crearArticulo("ART006", "Cable HDMI generico", electronica, null, admin); // SIN marca, nunca facturado

        // ---------- Lista de precios ----------
        ListaPrecio listaGeneral = new ListaPrecio();
        listaGeneral.setCodigo("LP2026");
        listaGeneral.setDenominacion("Lista general 2026");
        auditar(listaGeneral, admin);
        em.persist(listaGeneral);

        ListaPrecioArticulo lpTeclado = crearPrecio(listaGeneral, teclado, 25000, admin);
        ListaPrecioArticulo lpMouse = crearPrecio(listaGeneral, mouse, 12000, admin);
        ListaPrecioArticulo lpPad = crearPrecio(listaGeneral, pad, 4000, admin);
        ListaPrecioArticulo lpMonitor = crearPrecio(listaGeneral, monitor, 180000, admin);
        crearPrecio(listaGeneral, licuadora, 60000, admin);
        crearPrecio(listaGeneral, cable, 3000, admin);

        // ---------- Condiciones de IVA y moneda ----------
        CondicionIva responsableInscripto = crearCondicionIva(1, "Responsable Inscripto", admin);
        CondicionIva consumidorFinal = crearCondicionIva(5, "Consumidor Final", admin);

        pesos = new TipoMoneda();
        pesos.setCodigoAfip("PES");
        pesos.setDenominacion("Peso Argentino");
        pesos.setSimbolo("$");
        auditar(pesos, admin);
        em.persist(pesos);

        // ---------- Clientes ----------
        Cliente distribuidora = crearCliente("30-71234567-8", "Distribuidora Mendoza SA",
                "ventas@distmza.com", "Belgrano", "1500", admin);
        Cliente juan = crearCliente("20-30111222-3", "Juan Lopez",
                "juan@mail.com", "San Martin", "850", admin);
        Cliente maria = crearCliente("27-28999888-1", "Maria Gomez",
                "maria@mail.com", "Las Heras", "320", admin);

        // ---------- Facturas ----------
        FacturaVenta f1 = nuevaFactura(1, fecha(2026, 1, 10), pvCentral, juan, consumidorFinal, "EMITIDA", admin);
        agregarDetalle(f1, lpTeclado, 1);
        agregarDetalle(f1, lpMouse, 1);
        guardar(f1);

        FacturaVenta f2 = nuevaFactura(2, fecha(2026, 2, 5), pvCentral, distribuidora, responsableInscripto, "EMITIDA", admin);
        agregarDetalle(f2, lpMonitor, 2);
        guardar(f2);

        FacturaVenta f3 = nuevaFactura(3, fecha(2026, 2, 20), pvGodoyCruz, maria, consumidorFinal, "EMITIDA", admin);
        agregarDetalle(f3, lpPad, 1);
        guardar(f3);

        FacturaVenta f4 = nuevaFactura(4, fecha(2026, 3, 1), pvGodoyCruz, juan, consumidorFinal, "ANULADA", admin);
        agregarDetalle(f4, lpTeclado, 2);
        f4.setFechaAnulacion(fecha(2026, 3, 2));
        guardar(f4);

        FacturaVenta f5 = nuevaFactura(5, fecha(2026, 3, 15), pvLujan, distribuidora, responsableInscripto, "EMITIDA", admin);
        agregarDetalle(f5, lpMonitor, 1);
        guardar(f5);

        FacturaVenta f6 = nuevaFactura(6, fecha(2026, 4, 2), pvMaipu, maria, consumidorFinal, "PENDIENTE", admin);
        agregarDetalle(f6, lpPad, 1);
        agregarDetalle(f6, lpMouse, 1);
        guardar(f6);

        FacturaVenta f7 = nuevaFactura(7, fecha(2026, 4, 20), pvMaipu, juan, consumidorFinal, "EMITIDA", vendedor);
        agregarDetalle(f7, lpPad, 1);
        guardar(f7);

        FacturaVenta f8 = nuevaFactura(8, fecha(2026, 5, 10), pvCentral, distribuidora, responsableInscripto, "EMITIDA", vendedor);
        agregarDetalle(f8, lpTeclado, 1);
        agregarDetalle(f8, lpMonitor, 1);
        guardar(f8);

        em.getTransaction().commit();
        System.out.println(">>> Datos de prueba cargados: 8 facturas.");
    }

    // ================= Helpers =================

    private void auditar(AuditoriaApp entidad, Usuario usuario) {
        entidad.setFechaAlta(ahora);
        entidad.setFechaModificacion(ahora);
        entidad.setUsuarioCarga(usuario);
        entidad.setUsuarioModificacion(usuario);
    }

    private Date fecha(int anio, int mes, int dia) {
        return Date.from(LocalDate.of(anio, mes, dia)
                .atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private Usuario crearUsuario(String usuario, String nombre, String apellido) {
        Usuario u = new Usuario();
        u.setUsuario(usuario);
        u.setClave("1234");
        u.setNombre(nombre);
        u.setApellido(apellido);
        em.persist(u);
        return u;
    }

    private PuntoVenta crearPuntoVenta(int numero, String descripcion, Usuario usuario) {
        PuntoVenta pv = new PuntoVenta();
        pv.setNumero(numero);
        pv.setDescripcion(descripcion);
        pv.setTipoEmision("ELECTRONICA");
        pv.setDomicilioComercial("Mendoza");
        auditar(pv, usuario);
        em.persist(pv);
        return pv;
    }

    private Rubro crearRubro(int codigo, String denominacion, Usuario usuario) {
        Rubro r = new Rubro();
        r.setCodigo(codigo);
        r.setDenominacion(denominacion);
        auditar(r, usuario);
        em.persist(r);
        return r;
    }

    private Marca crearMarca(int codigo, String denominacion, Usuario usuario) {
        Marca m = new Marca();
        m.setCodigo(codigo);
        m.setDenominacion(denominacion);
        auditar(m, usuario);
        em.persist(m);
        return m;
    }

    private Articulo crearArticulo(String codigo, String denominacion, Rubro rubro, Marca marca, Usuario usuario) {
        Articulo a = new Articulo();
        a.setCodigo(codigo);
        a.setDenominacion(denominacion);
        a.setRubro(rubro);
        a.setMarca(marca); // puede ser null
        auditar(a, usuario);
        em.persist(a);
        return a;
    }

    private ListaPrecioArticulo crearPrecio(ListaPrecio lista, Articulo articulo, double precio, Usuario usuario) {
        ListaPrecioArticulo lpa = new ListaPrecioArticulo();
        lpa.setListaPrecio(lista);
        lpa.setArticulo(articulo);
        lpa.setPrecioVenta(precio);
        auditar(lpa, usuario);
        em.persist(lpa);
        return lpa;
    }

    private CondicionIva crearCondicionIva(int codigoAfip, String denominacion, Usuario usuario) {
        CondicionIva c = new CondicionIva();
        c.setCodigoAfip(codigoAfip);
        c.setDenominacion(denominacion);
        auditar(c, usuario);
        em.persist(c);
        return c;
    }

    private Cliente crearCliente(String cuit, String denominacion, String email,
                                 String calle, String numero, Usuario usuario) {
        Contacto contacto = new Contacto();
        contacto.setEmail(email);
        em.persist(contacto);

        Domicilio domicilio = new Domicilio();
        domicilio.setNombreCalle(calle);
        domicilio.setNumeroCalle(numero);
        em.persist(domicilio);

        Cliente c = new Cliente();
        c.setCuitCuil(cuit);
        c.setDenominacion(denominacion);
        c.setContacto(contacto);
        c.setDomicilio(domicilio);
        auditar(c, usuario);
        em.persist(c);
        return c;
    }

    private FacturaVenta nuevaFactura(long numero, Date fechaEmision, PuntoVenta pv, Cliente cliente,
                                      CondicionIva condicionIva, String estado, Usuario usuario) {
        FacturaVenta f = new FacturaVenta();
        f.setNumero(numero);
        f.setFechaEmision(fechaEmision);
        f.setPuntoVenta(pv);
        f.setCliente(cliente);
        f.setCondicionIva(condicionIva);
        f.setMoneda(pesos);
        f.setEstado(estado);
        auditar(f, usuario);
        return f;
    }

    private void agregarDetalle(FacturaVenta factura, ListaPrecioArticulo lpa, double cantidad) {
        FacturaVentaDetalle d = new FacturaVentaDetalle();
        d.setListaPrecioArticulo(lpa);
        d.setDescripcion(lpa.getArticulo().getDenominacion());
        d.setCantidad(cantidad);
        d.setPrecioUnitario(lpa.getPrecioVenta());
        d.setPorcentajeBonificacion(0);
        double neto = cantidad * lpa.getPrecioVenta();
        d.setImporteNeto(neto);
        d.setImporteIva(neto * 0.21);
        d.setImporteSubtotal(neto * 1.21);
        factura.addDetalle(d); // asociación bidireccional
    }

    private void guardar(FacturaVenta factura) {
        double total = 0;
        for (FacturaVentaDetalle d : factura.getDetalles()) {
            total += d.getImporteSubtotal();
        }
        factura.setImporteTotal(total);
        factura.setImporteSaldo(total);
        factura.setImporteCobrado(0);
        em.persist(factura); // un solo persist: la cascada guarda los detalles
    }
}
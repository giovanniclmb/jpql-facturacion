package com.tp.facturacion;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Arrays;

public class MainJPQL {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("FacturacionPU");
        EntityManager em = emf.createEntityManager();

        try {
            new CargaDatos(em).cargar();

    ConsultasJPQL consultas = new ConsultasJPQL(em);

            // ---------- NIVEL 1 ----------
            consultas.consulta01();
            consultas.consulta02();
            consultas.consulta03("Electrónica");
            consultas.consulta04(ConsultasJPQL.fecha(2026, 2, 1), ConsultasJPQL.fecha(2026, 3, 31));

            // ---------- NIVEL 2 ----------
            consultas.consulta05("EMITIDA", 10000);
            consultas.consulta06("mendoza", "20-");
            consultas.consulta07();
            consultas.consulta08("EMITIDA");
            consultas.consulta09(Arrays.asList(1, 2, 5));

            // ---------- NIVEL 3 ----------
            consultas.consulta10("vendedor");
            consultas.consulta11(1);
            consultas.consulta12();
            consultas.consulta13("Logitech");
            consultas.consulta14();

            // ---------- NIVEL 4 ----------
            consultas.consulta15();
            consultas.consulta16(5);
            consultas.consulta17();

            // ---------- NIVEL 5 ----------
            consultas.consulta18();
            consultas.consulta19();
            consultas.consulta20();

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
}
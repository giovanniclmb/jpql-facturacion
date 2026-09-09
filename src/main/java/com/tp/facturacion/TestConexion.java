package com.tp.facturacion;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class TestConexion {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("FacturacionPU");
        System.out.println(">>> Unidad de persistencia OK: conexión establecida.");
        emf.close();
    }
}
package org.cafeteria.cafeteria.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

public final class JPAUtil {
    private static final EntityManagerFactory EMF = build();

    private static EntityManagerFactory build() {
        // DEBUG: muestra si el XML está en el classpath
        var url = Thread.currentThread().getContextClassLoader().getResource("META-INF/persistence.xml");
        System.out.println("DEBUG persistence.xml: " + url);

        try {
            // Intenta con el PU del XML
            return Persistence.createEntityManagerFactory("CafeteriaPU");
        } catch (Exception ex) {
            System.err.println("[WARN] No se encontró 'CafeteriaPU'. Usando fallback sin XML.");
            ex.printStackTrace();

            // Fallback directo (ObjectDB sin XML)
            Map<String, String> props = new HashMap<>();
            props.put("jakarta.persistence.jdbc.url", "objectdb:./data/cafeteria.odb");
            props.put("jakarta.persistence.schema-generation.database.action", "create");

            return Persistence.createEntityManagerFactory("objectdb:./data/cafeteria.odb", props);
        }
    }

    public static EntityManager em() { return EMF.createEntityManager(); }
    public static void close() { EMF.close(); }
    private JPAUtil() {}
}

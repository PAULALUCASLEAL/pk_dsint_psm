package dsi.mitologia;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.definition.KiePackage;
import org.kie.api.definition.rule.Rule;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.utils.KieHelper;

/**
 * Prueba de humo de la integracion con Drools 7.74.1 (infraestructura, no mito).
 *
 * <p>La regla {@code ping -> pong} vive ahora en {@code src/test/resources/dsi/mitologia/pruebas} y se
 * compila aparte con {@link KieHelper}; ya no se carga con las reglas mitologicas de la KieBase
 * {@code mitologiaKB}. Que las reglas reales se cargan desde {@code kmodule.xml} lo comprueba
 * {@code ReglasPerseoAndromedaTest}.</p>
 */
public class PruebaIntegracionDroolsTest {

    /** Inserta "ping" y espera exactamente una anotacion "pong" en el global {@code registro}. */
    @Test
    public void laReglaDePruebaSeEjecuta() {
        KieBase base = new KieHelper()
                .addResource(ResourceFactory.newClassPathResource("dsi/mitologia/pruebas/prueba-integracion.drl"),
                        ResourceType.DRL)
                .build();
        KieSession sesion = base.newKieSession();
        List<String> registro = new ArrayList<String>();
        try {
            sesion.setGlobal("registro", registro);
            sesion.insert("ping");
            sesion.fireAllRules();
        } finally {
            sesion.dispose();
        }
        assertEquals(Collections.singletonList("pong"), registro);
    }

    /** La KieBase de ejecucion normal ya no contiene la regla temporal. */
    @Test
    public void laReglaDePruebaNoSeCargaConLasReglasDelMito() {
        KieBase base = KieServices.Factory.get().getKieClasspathContainer().getKieBase("mitologiaKB");
        for (KiePackage p : base.getKiePackages()) {
            for (Rule r : p.getRules()) {
                assertFalse(r.getName(), r.getName().startsWith("Prueba de integracion"));
            }
        }
    }
}

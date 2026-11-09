package dsi.mitologia.motor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.utils.KieHelper;

import dsi.mitologia.escenario.EscenarioMitologico;
import dsi.mitologia.escenario.EstadoAventura;
import dsi.mitologia.escenario.Objetivo;
import dsi.mitologia.escenario.ResultadoMision;
import dsi.mitologia.escenario.TipoObjetivo;
import dsi.mitologia.eventos.EventoMitologico;
import dsi.mitologia.hechos.Posesion;
import dsi.mitologia.modelo.DominioDePrueba;

/**
 * Prueba 12 y comprobacion del mantenimiento de la verdad con hechos del modelo. Son pruebas TECNICAS:
 * usan {@code prueba-tecnica-modelo.drl}, no las reglas del mito.
 *
 * <p>Por que importan: demuestran que el modelo Java es apto para Drools antes de escribir las reglas
 * de la fase 1.b. Si fallan, el problema esta en el modelo o en {@link MotorReglas} (getters que Drools
 * no ve, equals/hashCode mal definidos, hechos no insertados, global no enlazado), y no en las reglas
 * del mito.</p>
 *
 * <p>Papel en el mito: Perseo obtiene el CascoHades de Hades y, por tenerlo, la Invisibilidad; es el
 * mismo patron que las reglas reales usaran para las SandaliasAladas (Vuelo) o la CabezaMedusa
 * (Petrificacion).</p>
 */
public class IntegracionDroolsModeloTest {

    /** Sesion nueva por prueba, creada con KieHelper sobre el DRL tecnico (no la KieBase principal). */
    private KieSession sesion;
    /** Escenario de prueba: objetivo "Perseo obtiene Invisibilidad" sobre {@link DominioDePrueba}. */
    private EscenarioMitologico escenario;

    /**
     * Compila el DRL tecnico desde el classpath de test y prepara un escenario en el que Perseo ya
     * tiene el CascoHades. La transferencia se hace en Java solo como dato de entrada (arrange); la
     * capacidad que derive de ella debe deducirla la regla.
     */
    @Before
    public void preparar() {
        // KieHelper en vez de kmodule.xml: asi el DRL de pruebas nunca entra en la KieBase de produccion.
        KieBase base = new KieHelper()
                .addResource(ResourceFactory.newClassPathResource("dsi/mitologia/pruebas/prueba-tecnica-modelo.drl"),
                        ResourceType.DRL)
                .build();
        sesion = base.newKieSession();
        escenario = new EscenarioMitologico("tecnico", "Puede Perseo obtener Invisibilidad",
                new Objetivo(TipoObjetivo.OBTENER_CAPACIDAD, "Perseo", "Invisibilidad"), DominioDePrueba.crear());
        EstadoAventura e = escenario.getEstado();
        e.transferirObjeto(e.getPersonaje("Hades"), e.getPersonaje("Perseo"), e.getObjeto("CascoHades"));
    }

    /** Libera la memoria de trabajo: {@link MotorReglas} no cierra la sesion, lo hace el llamador. */
    @After
    public void cerrar() {
        sesion.dispose();
    }

    /**
     * Prueba 12: Drools reconoce los hechos del modelo. La regla tecnica casa un {@code Personaje} por
     * sus propiedades booleanas {@code heroe} y {@code semidios} y por {@code estadoVital}, y una
     * {@code Posesion} unida a el; y anota un evento en el global {@code registro}.
     *
     * <p>Detectaria: getters con nombre no JavaBean, tipos multiples mal expuestos, Posesion que no
     * referencia al mismo objeto Personaje insertado, o global {@code registro} no enlazado por
     * {@link MotorReglas}. Cubre el aserto OWL de que Perseo es Heroe y Semidios a la vez.</p>
     */
    // Prueba 12
    @Test
    public void droolsReconoceLosHechosDelModelo() {
        // Act
        MotorReglas.ejecutar(sesion, escenario);
        // Assert: un solo evento (solo Perseo es heroe y semidios y posee algo) con actor, objeto y regla.
        assertEquals(1, escenario.getRegistro().size());
        EventoMitologico ev = escenario.getRegistro().getEvento(1);
        assertEquals("Perseo", ev.getActor().getId());
        assertEquals("CascoHades", ev.getObjeto().getId());
        // El nombre de la regla queda en el evento para poder justificar cada paso de la historia.
        assertEquals("Prueba tecnica: heroe vivo que posee un objeto", ev.getRegla());
    }

    /**
     * Mantenimiento de la verdad: la Invisibilidad insertada con {@code insertLogical} existe mientras
     * exista la Posesion del CascoHades que la justifica, y Drools la retira sola al borrar esa Posesion.
     *
     * <p>Por que importa: es el mecanismo que se usara para que perder un objeto haga perder la capacidad
     * sin escribir reglas de "borrado" (que serian programar). Detectaria que el motor no retracta hechos
     * logicos, que {@code sincronizarDesde} no refleja la retirada, o que el objetivo no se reevalua.
     * Relacion con la ontologia: CascoHades proporciona_capacidad Invisibilidad.</p>
     */
    @Test
    public void laCapacidadLogicaDesapareceAlPerderElObjeto() {
        // Act 1: la regla deriva la capacidad a partir de la posesion.
        MotorReglas.ejecutar(sesion, escenario);
        // Assert 1: objetivo cumplido y capacidad presente en el estado sincronizado.
        assertEquals(ResultadoMision.CONSEGUIDO, escenario.getResultado());
        assertTrue(escenario.getEstado().tieneCapacidad("Perseo", "Invisibilidad"));

        // Se retira la posesion del casco desde fuera (como haria un delete en una regla)
        for (Object h : sesion.getObjects()) {
            if (h instanceof Posesion && ((Posesion) h).getObjeto().getId().equals("CascoHades")) {
                FactHandle fh = sesion.getFactHandle(h);
                sesion.delete(fh);
            }
        }
        // Act 2: se vuelve a disparar y a sincronizar a mano (no se reinsertan los hechos).
        sesion.fireAllRules();
        escenario.getEstado().sincronizarDesde(sesion.getObjects());
        // Assert 2: Drools ha retirado la capacidad logica y el objetivo deja de cumplirse.
        assertFalse(escenario.getEstado().tieneCapacidad("Perseo", "Invisibilidad"));
        assertEquals(ResultadoMision.NO_CONSEGUIDO, escenario.evaluarResultado());
    }
}

package dsi.mitologia.motor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.Message;
import org.kie.api.builder.Results;
import org.kie.api.definition.KiePackage;
import org.kie.api.definition.rule.Rule;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;

import dsi.mitologia.escenario.DominioMitologico;
import dsi.mitologia.escenario.EscenarioMitologico;
import dsi.mitologia.escenario.EstadoAventura;
import dsi.mitologia.escenario.Objetivo;
import dsi.mitologia.escenario.PreparadorEscenario;
import dsi.mitologia.escenario.ResultadoMision;
import dsi.mitologia.escenario.TipoObjetivo;
import dsi.mitologia.eventos.EventoMitologico;
import dsi.mitologia.eventos.TipoAccion;
import dsi.mitologia.hechos.Cautiverio;
import dsi.mitologia.hechos.Muerte;
import dsi.mitologia.hechos.Posesion;
import dsi.mitologia.hechos.VarianteMito;
import dsi.mitologia.modelo.enums.EstadoLibertad;
import dsi.mitologia.modelo.enums.EstadoVital;

/**
 * Pruebas de las reglas reales del mito de Perseo y Andromeda (fase 1.b) sobre la KieBase
 * {@code mitologiaKB} de {@code kmodule.xml}, con el dominio cargado de {@code ../ontologia/mitologia.owl}.
 *
 * <p>Cubre los escenarios del enunciado (1 y 2), los del catalogo de reglas (C: sin zurron, D: sin vuelo,
 * E: Pegaso, F: espada, G: reinicio) y las propiedades de control: terminacion sin bucles, un solo
 * poseedor por objeto, nadie muere dos veces, los muertos no actuan y el cautiverio desaparece.</p>
 *
 * <p>Cada prueba crea su propia KieSession y la cierra: los escenarios son independientes.</p>
 */
public class ReglasPerseoAndromedaTest {

    /** Limite de disparos: muy por encima de lo que necesita un escenario; alcanzarlo indicaria un bucle. */
    private static final int LIMITE = 1000;

    private static KieContainer contenedor;
    private static DominioMitologico dominio;

    @BeforeClass
    public static void cargar() {
        contenedor = KieServices.Factory.get().getKieClasspathContainer();
        dominio = PreparadorEscenario.cargarDominio(new File("../ontologia/mitologia.owl"));
    }

    // ------------------------------------------------------------------ escenarios

    private static PreparadorEscenario base(String id) {
        return new PreparadorEscenario(id, "Puede Perseo liberar a Andromeda?",
                new Objetivo(TipoObjetivo.LIBERAR, "Perseo", "Andromeda"), dominio);
    }

    /** Escenario 1 del enunciado (pag. 11). */
    private static PreparadorEscenario escenario1(String id) {
        return base(id)
                .favor("Perseo", "Atenea").favor("Perseo", "Hermes")
                .favor("Perseo", "Hades").favor("Perseo", "Hefesto")
                .enojo("Casiopea", "Poseidon");
    }

    /** Escenario 2 del enunciado (pag. 12). */
    private static PreparadorEscenario escenario2(String id) {
        return base(id)
                .favor("Perseo", "Atenea").favor("Perseo", "Hermes").favor("Perseo", "Hefesto")
                .enojo("Casiopea", "Poseidon")
                .noTiene("Atenea", "EscudoBronce").noTiene("Hades", "CascoHades")
                .noTiene("Ninfas", "SandaliasAladas")
                .tiene("Perseo", "AnilloInvisibilidad").tiene("Perseo", "EspejoMano")
                .tiene("Perseo", "ConjuroVuelo");
    }

    // ------------------------------------------------------------------ utilidades

    /** Ejecuta en una sesion nueva y comprueba que las reglas terminan antes del limite. */
    private static EscenarioMitologico ejecutar(PreparadorEscenario preparador) {
        EscenarioMitologico esc = preparador.construir();
        KieSession sesion = contenedor.newKieSession("mitologiaSesion");
        try {
            int disparadas = MotorReglas.ejecutar(sesion, esc, LIMITE);
            assertTrue("Posible bucle: se alcanzo el limite de " + LIMITE + " disparos", disparadas < LIMITE);
            assertEquals("La agenda debe quedar vacia (nada pendiente)", 0, sesion.fireAllRules());
        } finally {
            sesion.dispose();
        }
        imprimir(esc);
        return esc;
    }

    private static void imprimir(EscenarioMitologico esc) {
        System.out.println("=== Escenario " + esc.getId() + " -> " + esc.getResultado());
        for (EventoMitologico e : historia(esc)) System.out.println("  " + e.getId() + " " + e.getDescripcion());
        for (EventoMitologico e : esc.getRegistro().getEventos()) {
            if (e.getRegla() != null && e.getRegla().startsWith("R31")) System.out.println("  " + e.getDescripcion());
        }
    }

    /** Eventos de la historia (sin la informacion inicial ni los diagnosticos). */
    private static List<EventoMitologico> historia(EscenarioMitologico esc) {
        List<EventoMitologico> l = new ArrayList<EventoMitologico>();
        for (EventoMitologico e : esc.getRegistro().getEventos()) {
            if (e.getAccion() != TipoAccion.INFORMACION || (e.getRegla() != null && e.getRegla().startsWith("R2"))) {
                l.add(e);
            }
        }
        return l;
    }

    /**
     * Primer evento con esa accion y actor cuyo destinatario, objeto o capacidad tenga el id dado
     * (o cualquiera si {@code id} es null).
     */
    private static EventoMitologico evento(EscenarioMitologico esc, TipoAccion accion, String actor, String id) {
        for (EventoMitologico e : esc.getRegistro().getEventos()) {
            if (e.getAccion() != accion) continue;
            if (actor != null && (e.getActor() == null || !actor.equals(e.getActor().getId()))) continue;
            if (id == null
                    || (e.getDestinatario() != null && id.equals(e.getDestinatario().getId()))
                    || (e.getObjeto() != null && id.equals(e.getObjeto().getId()))
                    || (e.getCapacidad() != null && id.equals(e.getCapacidad().getId()))) {
                return e;
            }
        }
        return null;
    }

    private static int orden(EscenarioMitologico esc, TipoAccion accion, String actor, String id) {
        EventoMitologico e = evento(esc, accion, actor, id);
        assertNotNull("Falta el evento " + accion + " " + actor + " " + id, e);
        return e.getOrden();
    }

    private static void antes(EscenarioMitologico esc, int a, int b, String que) {
        assertTrue("Orden causal incorrecto: " + que + " (" + a + " >= " + b + ")", a < b);
    }

    private static int contar(EscenarioMitologico esc, TipoAccion accion, String actor, String id) {
        int n = 0;
        for (EventoMitologico e : esc.getRegistro().getEventos()) {
            if (e.getAccion() == accion && (actor == null || (e.getActor() != null && actor.equals(e.getActor().getId())))
                    && (id == null || (e.getDestinatario() != null && id.equals(e.getDestinatario().getId()))
                        || (e.getObjeto() != null && id.equals(e.getObjeto().getId())))) {
                n++;
            }
        }
        return n;
    }

    private static boolean hayDiagnostico(EscenarioMitologico esc, String texto) {
        for (EventoMitologico e : esc.getRegistro().getEventos()) {
            if (e.getRegla() != null && e.getRegla().startsWith("R31") && e.getDescripcion().contains(texto)) return true;
        }
        return false;
    }

    // ------------------------------------------------------------------ carga de las reglas

    @Test
    public void lasReglasRealesCompilanSinErrores() {
        Results r = KieServices.Factory.get().getKieClasspathContainer().verify();
        assertTrue(r.getMessages(Message.Level.ERROR).toString(), r.getMessages(Message.Level.ERROR).isEmpty());
        List<String> nombres = new ArrayList<String>();
        for (KiePackage p : contenedor.getKieBase("mitologiaKB").getKiePackages()) {
            for (Rule regla : p.getRules()) nombres.add(regla.getName());
        }
        // Una regla de cada fichero .drl
        assertTrue(nombres.contains("R00 Mostrar la informacion inicial antes de la historia"));
        assertTrue(nombres.contains("R01 Un dios enojado libera a la criatura que retenia"));
        assertTrue(nombres.contains("R03-R04-R06 Un dios entrega su objeto a quien tiene su favor"));
        assertTrue(nombres.contains("R09-R12 Capacidad proporcionada por un objeto poseido"));
        assertTrue(nombres.contains("R13 Localizar a las Grayas con el favor de Hermes y Atenea"));
        assertTrue(nombres.contains("R23-R24 Matar a Medusa con arma, Reflejo, Invisibilidad y Vuelo"));
        assertTrue(nombres.contains("R29 Liberar al preso de la criatura muerta"));
    }

    // ------------------------------------------------------------------ escenario 1

    @Test
    public void escenario1PerseoLiberaAAndromedaSiguiendoLaSecuenciaDelMito() {
        EscenarioMitologico esc = ejecutar(escenario1("F1-1"));
        EstadoAventura e = esc.getEstado();
        assertEquals(ResultadoMision.CONSEGUIDO, esc.getResultado());

        int cetoLibre = orden(esc, TipoAccion.QUEDAR_LIBRE, "Ceto", "Poseidon");
        int presa = orden(esc, TipoAccion.APRESAR, "Ceto", "Andromeda");
        int escudo = orden(esc, TipoAccion.OBTENER_OBJETO, "Perseo", "EscudoBronce");
        int hoz = orden(esc, TipoAccion.OBTENER_OBJETO, "Perseo", "HozAcero");
        int espejo = orden(esc, TipoAccion.TRANSFORMAR_OBJETO, "Perseo", "EscudoEspejo");
        int reflejo = orden(esc, TipoAccion.OBTENER_CAPACIDAD, "Perseo", "Reflejo");
        int casco = orden(esc, TipoAccion.OBTENER_OBJETO, "Perseo", "CascoHades");
        int invis = orden(esc, TipoAccion.OBTENER_CAPACIDAD, "Perseo", "Invisibilidad");
        int grayas = orden(esc, TipoAccion.LOCALIZAR, "Perseo", "Grayas");
        int robo = orden(esc, TipoAccion.ROBAR_OBJETO, "Perseo", "Grayas");
        int intercambio = orden(esc, TipoAccion.INTERCAMBIAR_OBJETO, "Perseo", "Grayas");
        int ninfas = orden(esc, TipoAccion.LOCALIZAR, "Perseo", "Ninfas");
        int zurron = orden(esc, TipoAccion.OBTENER_OBJETO, "Perseo", "ZurronMagico");
        int sandalias = orden(esc, TipoAccion.OBTENER_OBJETO, "Perseo", "SandaliasAladas");
        int vuelo = orden(esc, TipoAccion.OBTENER_CAPACIDAD, "Perseo", "Vuelo");
        int medusa = orden(esc, TipoAccion.MATAR, "Perseo", "Medusa");
        int cabeza = orden(esc, TipoAccion.OBTENER_OBJETO, "Perseo", "CabezaMedusa");
        int ceto = orden(esc, TipoAccion.MATAR, "Perseo", "Ceto");
        int libera = orden(esc, TipoAccion.LIBERAR, "Perseo", "Andromeda");

        // Orden parcial que exige el enunciado
        antes(esc, cetoLibre, presa, "Ceto libre -> Andromeda presa");
        antes(esc, escudo, espejo, "escudo de bronce -> escudo-espejo");
        antes(esc, espejo, reflejo, "escudo-espejo -> Reflejo");
        antes(esc, casco, invis, "casco -> Invisibilidad");
        antes(esc, grayas, robo, "localizar Grayas -> robar");
        antes(esc, invis, robo, "Invisibilidad -> robar");
        antes(esc, robo, intercambio, "robar -> intercambiar");
        antes(esc, intercambio, ninfas, "intercambiar -> localizar Ninfas");
        antes(esc, ninfas, zurron, "Ninfas -> zurron");
        antes(esc, ninfas, sandalias, "Ninfas -> sandalias");
        antes(esc, sandalias, vuelo, "sandalias -> Vuelo");
        antes(esc, hoz, medusa, "hoz -> Medusa");
        antes(esc, reflejo, medusa, "Reflejo -> Medusa");
        antes(esc, vuelo, medusa, "Vuelo -> Medusa");
        antes(esc, medusa, cabeza, "matar Medusa -> cabeza");
        antes(esc, cabeza, ceto, "cabeza -> matar Ceto");
        antes(esc, presa, ceto, "Andromeda presa -> matar Ceto");
        antes(esc, ceto, libera, "matar Ceto -> liberar");

        // Justificaciones como en la salida del enunciado
        EventoMitologico mata = evento(esc, TipoAccion.MATAR, "Perseo", "Medusa");
        assertEquals("Perseo mata a Medusa debido a tener HozAcero, Espejo (EscudoEspejo), "
                + "Invisibilidad (CascoHades) y Vuelo (SandaliasAladas)", mata.getDescripcion());
        assertEquals("Ceto queda libre debido al enojo de Poseidon hacia Casiopea",
                evento(esc, TipoAccion.QUEDAR_LIBRE, "Ceto", null).getDescripcion());
        assertNotNull("Debe explicarse que el zurron le salva",
                evento(esc, TipoAccion.INFORMACION, "Perseo", "CabezaMedusa"));
        assertNotNull(evento(esc, TipoAccion.CUMPLIR_OBJETIVO, "Perseo", "Andromeda"));

        // Estado final coherente
        assertEquals(EstadoVital.VIVO, e.getPersonaje("Perseo").getEstadoVital());
        assertEquals(EstadoVital.MUERTO, e.getPersonaje("Medusa").getEstadoVital());
        assertEquals(EstadoVital.MUERTO, e.getPersonaje("Ceto").getEstadoVital());
        assertEquals(EstadoLibertad.LIBRE, e.getPersonaje("Andromeda").getEstadoLibertad());
        assertNull("La liberacion debe eliminar el cautiverio", e.captorDe(e.getPersonaje("Andromeda")));
        assertTrue(e.posee("Perseo", "CabezaMedusa"));
        assertTrue(e.posee("Perseo", "ZurronMagico"));
        assertFalse("El escudo de bronce se transformo", e.posee("Perseo", "EscudoBronce"));
        assertTrue("El ojo/diente vuelve a las Grayas", e.posee("Grayas", "OjoGrayas") && e.posee("Grayas", "DienteGrayas"));
        assertTrue("La espada solo se entrega en su variante", e.posee("Hefesto", "EspadaIndestructible"));
        assertTrue(e.haMatado("Perseo", "Medusa"));
        assertTrue(e.haMatado("Perseo", "Ceto"));
        assertFalse("No hay diagnostico de fallo", hayDiagnostico(esc, "No, no se puede"));
    }

    // ------------------------------------------------------------------ escenario 2

    @Test
    public void escenario2PerseoUsaAnilloEspejoDeManoYConjuro() {
        EscenarioMitologico esc = ejecutar(escenario2("F1-2"));
        EstadoAventura e = esc.getEstado();
        assertEquals(ResultadoMision.CONSEGUIDO, esc.getResultado());

        // Los hechos negativos se respetan: ni escudo, ni casco, ni sandalias.
        assertNull(evento(esc, TipoAccion.OBTENER_OBJETO, "Perseo", "EscudoBronce"));
        assertNull(evento(esc, TipoAccion.TRANSFORMAR_OBJETO, "Perseo", "EscudoEspejo"));
        assertNull(evento(esc, TipoAccion.OBTENER_OBJETO, "Perseo", "CascoHades"));
        assertNull(evento(esc, TipoAccion.OBTENER_OBJETO, "Perseo", "SandaliasAladas"));
        assertFalse(e.posee("Perseo", "CascoHades"));

        // Medios alternativos
        int invis = orden(esc, TipoAccion.OBTENER_CAPACIDAD, "Perseo", "AnilloInvisibilidad");
        int vuelo = orden(esc, TipoAccion.OBTENER_CAPACIDAD, "Perseo", "ConjuroVuelo");
        int reflejo = orden(esc, TipoAccion.OBTENER_CAPACIDAD, "Perseo", "EspejoMano");
        int robo = orden(esc, TipoAccion.ROBAR_OBJETO, "Perseo", "Grayas");
        assertTrue(evento(esc, TipoAccion.ROBAR_OBJETO, "Perseo", "Grayas").getDescripcion()
                .contains("Invisibilidad (AnilloInvisibilidad)"));
        int zurron = orden(esc, TipoAccion.OBTENER_OBJETO, "Perseo", "ZurronMagico");
        int medusa = orden(esc, TipoAccion.MATAR, "Perseo", "Medusa");
        int libera = orden(esc, TipoAccion.LIBERAR, "Perseo", "Andromeda");
        antes(esc, invis, robo, "anillo -> robar");
        antes(esc, robo, zurron, "robar -> zurron");
        antes(esc, vuelo, medusa, "conjuro -> Medusa");
        antes(esc, reflejo, medusa, "espejo de mano -> Medusa");
        antes(esc, medusa, libera, "Medusa -> liberar");
        assertEquals("Perseo mata a Medusa debido a tener HozAcero, Espejo (EspejoMano), "
                + "Invisibilidad (AnilloInvisibilidad) y Vuelo (ConjuroVuelo)",
                evento(esc, TipoAccion.MATAR, "Perseo", "Medusa").getDescripcion());
        assertEquals(EstadoVital.VIVO, e.getPersonaje("Perseo").getEstadoVital());
    }

    // ------------------------------------------------------------------ escenario 3 (prueba C)

    @Test
    public void escenario3SinZurronPerseoMuereYNoRescataAAndromeda() {
        EscenarioMitologico esc = ejecutar(escenario1("F1-3").noTiene("Ninfas", "ZurronMagico"));
        EstadoAventura e = esc.getEstado();
        assertEquals(ResultadoMision.NO_CONSEGUIDO, esc.getResultado());
        int cabeza = orden(esc, TipoAccion.OBTENER_OBJETO, "Perseo", "CabezaMedusa");
        int muere = orden(esc, TipoAccion.MORIR, "Perseo", "CabezaMedusa");
        antes(esc, cabeza, muere, "obtener cabeza -> morir");
        assertEquals("Muere una sola vez", 1, contar(esc, TipoAccion.MORIR, "Perseo", null));
        assertEquals(EstadoVital.MUERTO, e.getPersonaje("Perseo").getEstadoVital());
        assertEquals(EstadoVital.MUERTO, e.getPersonaje("Medusa").getEstadoVital());
        assertEquals(EstadoVital.VIVO, e.getPersonaje("Ceto").getEstadoVital());
        assertEquals(EstadoLibertad.PRESO, e.getPersonaje("Andromeda").getEstadoLibertad());
        assertFalse(e.haLiberado("Perseo", "Andromeda"));
        assertFalse("Un muerto no conserva capacidades", e.tieneCapacidad("Perseo", "Petrificacion"));
        // Ningun acontecimiento posterior a la muerte tiene a Perseo como actor
        for (EventoMitologico ev : historia(esc)) {
            if (ev.getOrden() > muere && ev.getActor() != null) {
                assertFalse("Perseo actua muerto: " + ev, "Perseo".equals(ev.getActor().getId()));
            }
        }
        assertTrue(hayDiagnostico(esc, "Perseo ha muerto"));
        assertTrue(hayDiagnostico(esc, "No, no se puede demostrar"));
    }

    // ------------------------------------------------------------------ escenario 4 (prueba D)

    @Test
    public void escenario4SinNingunaFormaDeVolarNoPuedeMatarAMedusa() {
        EscenarioMitologico esc = ejecutar(escenario1("F1-4").noTiene("Ninfas", "SandaliasAladas"));
        EstadoAventura e = esc.getEstado();
        assertEquals(ResultadoMision.NO_CONSEGUIDO, esc.getResultado());
        assertFalse(e.tieneCapacidad("Perseo", "Vuelo"));
        assertNull(evento(esc, TipoAccion.MATAR, "Perseo", "Medusa"));
        assertEquals(EstadoVital.VIVO, e.getPersonaje("Medusa").getEstadoVital());
        assertEquals(EstadoVital.VIVO, e.getPersonaje("Ceto").getEstadoVital());
        assertEquals(EstadoLibertad.PRESO, e.getPersonaje("Andromeda").getEstadoLibertad());
        // El resto de la ruta si se recorre: tiene invisibilidad, reflejo, arma y el zurron
        assertTrue(e.tieneCapacidad("Perseo", "Invisibilidad"));
        assertTrue(e.tieneCapacidad("Perseo", "Reflejo"));
        assertTrue(e.posee("Perseo", "ZurronMagico"));
        assertTrue(hayDiagnostico(esc, "no tiene Capacidad Vuelo"));
    }

    // ------------------------------------------------------------------ prueba E: Pegaso

    @Test
    public void varianteDePegasoSustituyeALasSandalias() {
        EscenarioMitologico esc = ejecutar(escenario1("F1-E").noTiene("Ninfas", "SandaliasAladas")
                .variante(VarianteMito.PEGASO));
        assertEquals(ResultadoMision.CONSEGUIDO, esc.getResultado());
        int brida = orden(esc, TipoAccion.OBTENER_OBJETO, "Perseo", "BridaDorada");
        int captura = orden(esc, TipoAccion.APRESAR, "Perseo", "Pegaso");
        int vuelo = orden(esc, TipoAccion.OBTENER_CAPACIDAD, "Perseo", "Pegaso");
        int medusa = orden(esc, TipoAccion.MATAR, "Perseo", "Medusa");
        antes(esc, brida, captura, "brida -> capturar Pegaso");
        antes(esc, captura, vuelo, "capturar -> Vuelo");
        antes(esc, vuelo, medusa, "Vuelo -> Medusa");
        assertTrue(evento(esc, TipoAccion.MATAR, "Perseo", "Medusa").getDescripcion().contains("Vuelo (Pegaso)"));
    }

    @Test
    public void versionClasicaDePegasoNoPermiteUsarloParaLlegarAMedusa() {
        EscenarioMitologico esc = ejecutar(escenario1("F1-E2").noTiene("Ninfas", "SandaliasAladas")
                .variante(VarianteMito.PEGASO).variante(VarianteMito.PEGASO_CLASICO));
        assertEquals(ResultadoMision.NO_CONSEGUIDO, esc.getResultado());
        assertNull(evento(esc, TipoAccion.APRESAR, "Perseo", "Pegaso"));
        assertEquals(EstadoVital.VIVO, esc.getEstado().getPersonaje("Medusa").getEstadoVital());
    }

    // ------------------------------------------------------------------ prueba F: espada

    @Test
    public void laEspadaDeHefestoSustituyeALaHozSoloConSuVariante() {
        EscenarioMitologico conVariante = ejecutar(escenario1("F1-F").noTiene("Hermes", "HozAcero")
                .variante(VarianteMito.ESPADA_HEFESTO));
        assertEquals(ResultadoMision.CONSEGUIDO, conVariante.getResultado());
        assertEquals("EspadaIndestructible",
                evento(conVariante, TipoAccion.MATAR, "Perseo", "Medusa").getObjeto().getId());

        EscenarioMitologico sinVariante = ejecutar(escenario1("F1-F2").noTiene("Hermes", "HozAcero"));
        assertEquals(ResultadoMision.NO_CONSEGUIDO, sinVariante.getResultado());
        assertNull(evento(sinVariante, TipoAccion.OBTENER_OBJETO, "Perseo", "EspadaIndestructible"));
        assertTrue(hayDiagnostico(sinVariante, "ningun arma"));
    }

    // ------------------------------------------------------------------ prueba G: reinicio

    @Test
    public void losEscenariosSonIndependientesEntreEjecuciones() {
        EscenarioMitologico a1 = ejecutar(escenario1("G-A1"));
        EscenarioMitologico b = ejecutar(escenario2("G-B"));
        EscenarioMitologico c = ejecutar(escenario1("G-C").noTiene("Ninfas", "ZurronMagico"));
        EscenarioMitologico a2 = ejecutar(escenario1("G-A2"));
        assertEquals(ResultadoMision.CONSEGUIDO, a1.getResultado());
        assertEquals(ResultadoMision.CONSEGUIDO, b.getResultado());
        assertEquals(ResultadoMision.NO_CONSEGUIDO, c.getResultado());
        assertEquals(ResultadoMision.CONSEGUIDO, a2.getResultado());
        assertEquals(descripciones(a1), descripciones(a2));
        // Nada se filtra al estado de un escenario nuevo sin ejecutar
        EstadoAventura limpio = escenario1("G-limpio").construir().getEstado();
        assertEquals(EstadoVital.VIVO, limpio.getPersonaje("Perseo").getEstadoVital());
        assertEquals(EstadoVital.VIVO, limpio.getPersonaje("Medusa").getEstadoVital());
        assertEquals(EstadoLibertad.LIBRE, limpio.getPersonaje("Andromeda").getEstadoLibertad());
        assertTrue(limpio.posee("Atenea", "EscudoBronce"));
        assertTrue(limpio.posee("Hades", "CascoHades"));
        assertTrue(limpio.getMuertes().isEmpty());
        assertFalse(limpio.posee("Perseo", "AnilloInvisibilidad"));
    }

    private static List<String> descripciones(EscenarioMitologico esc) {
        List<String> l = new ArrayList<String>();
        for (EventoMitologico e : esc.getRegistro().getEventos()) l.add(e.getDescripcion());
        return l;
    }

    // ------------------------------------------------------------------ propiedades de control

    @Test
    public void sinFavoresPerseoNoObtieneNadaPeroElEnojoDelOwlSeAplica() {
        // Sin condiciones solo queda el conocimiento por defecto del OWL, que ya incluye
        // "Casiopea tiene_enojo Poseidon": Ceto queda libre y apresa a Andromeda, pero Perseo,
        // sin ningun favor, no recibe objetos ni puede rescatarla.
        EscenarioMitologico esc = ejecutar(base("vacio"));
        assertEquals(ResultadoMision.NO_CONSEGUIDO, esc.getResultado());
        assertNotNull(evento(esc, TipoAccion.QUEDAR_LIBRE, "Ceto", "Poseidon"));
        assertNull(evento(esc, TipoAccion.OBTENER_OBJETO, "Perseo", null));
        assertNull(evento(esc, TipoAccion.LOCALIZAR, "Perseo", null));
        assertEquals(EstadoLibertad.PRESO, esc.getEstado().getPersonaje("Andromeda").getEstadoLibertad());
        assertTrue(hayDiagnostico(esc, "ningun arma"));
    }

    @Test
    public void sinEnojoCetoNoQuedaLibreNiApresaAAndromeda() {
        // Hecho de la ontologia retirado explicitamente por el escenario (Java, no regla).
        PreparadorEscenario p = escenario1("sin-enojo");
        EstadoAventura e = p.construir().getEstado();
        e.retirarEnojo(e.getPersonaje("Casiopea"), e.getPersonaje("Poseidon"));
        EscenarioMitologico esc = ejecutar(p);
        assertNull(evento(esc, TipoAccion.QUEDAR_LIBRE, "Ceto", null));
        assertNull(evento(esc, TipoAccion.APRESAR, "Ceto", "Andromeda"));
        assertEquals(EstadoLibertad.LIBRE, esc.getEstado().getPersonaje("Andromeda").getEstadoLibertad());
        // Perseo puede matar a Medusa, pero no hay nadie a quien liberar
        assertNotNull(evento(esc, TipoAccion.MATAR, "Perseo", "Medusa"));
        assertNull(evento(esc, TipoAccion.MATAR, "Perseo", "Ceto"));
        assertEquals(ResultadoMision.NO_CONSEGUIDO, esc.getResultado());
    }

    @Test
    public void cadaObjetoTieneUnSoloPoseedorYNadieMuereDosVeces() {
        for (PreparadorEscenario p : new PreparadorEscenario[] {escenario1("U1"), escenario2("U2"),
                escenario1("U3").noTiene("Ninfas", "ZurronMagico")}) {
            EscenarioMitologico esc = ejecutar(p);
            List<String> objetos = new ArrayList<String>();
            for (Posesion pos : esc.getEstado().getPosesiones()) {
                assertFalse("Objeto con dos poseedores: " + pos, objetos.contains(pos.getObjeto().getId()));
                objetos.add(pos.getObjeto().getId());
            }
            List<String> victimas = new ArrayList<String>();
            for (Muerte m : esc.getEstado().getMuertes()) {
                assertFalse("Muere dos veces: " + m, victimas.contains(m.getVictima().getId()));
                victimas.add(m.getVictima().getId());
            }
            for (Cautiverio c : esc.getEstado().getCautiverios()) {
                assertEquals("Cautiverio de alguien libre: " + c, EstadoLibertad.PRESO, c.getPreso().getEstadoLibertad());
            }
        }
    }

    @Test
    public void laCapacidadDesapareceSiSePierdeElObjetoQueLaDa() {
        EscenarioMitologico esc = escenario2("perdida").construir();
        KieSession sesion = contenedor.newKieSession("mitologiaSesion");
        try {
            MotorReglas.ejecutar(sesion, esc, LIMITE);
            assertTrue(esc.getEstado().tieneCapacidad("Perseo", "Invisibilidad"));
            List<FactHandle> aBorrar = new ArrayList<FactHandle>();
            for (Object h : sesion.getObjects()) {
                if (h instanceof Posesion && ((Posesion) h).getObjeto().getId().equals("AnilloInvisibilidad")) {
                    aBorrar.add(sesion.getFactHandle(h));
                }
            }
            assertEquals(1, aBorrar.size());
            for (FactHandle fh : aBorrar) sesion.delete(fh);
            sesion.fireAllRules();
            esc.getEstado().sincronizarDesde(sesion.getObjects());
            assertFalse("Sin anillo no hay invisibilidad", esc.getEstado().tieneCapacidad("Perseo", "Invisibilidad"));
            assertTrue("El resto de capacidades sigue", esc.getEstado().tieneCapacidad("Perseo", "Vuelo"));
        } finally {
            sesion.dispose();
        }
    }

    // ------------------------------------------------------------------ robustez frente al orden

    /**
     * La resolucion de conflictos de Drools depende del orden de insercion (recencia). El resultado de
     * cada escenario no debe depender de ese orden: se repite cada uno con 25 ordenes aleatorios.
     */
    @Test
    public void elResultadoNoDependeDelOrdenDeInsercionDeLosHechos() {
        for (int semilla = 1; semilla <= 25; semilla++) {
            assertEquals(ResultadoMision.CONSEGUIDO, barajado(escenario1("O1"), semilla));
            assertEquals(ResultadoMision.CONSEGUIDO, barajado(escenario2("O2"), semilla));
            assertEquals(ResultadoMision.NO_CONSEGUIDO, barajado(escenario1("O3").noTiene("Ninfas", "ZurronMagico"), semilla));
            assertEquals(ResultadoMision.NO_CONSEGUIDO, barajado(escenario1("O4").noTiene("Ninfas", "SandaliasAladas"), semilla));
            assertEquals(ResultadoMision.CONSEGUIDO, barajado(escenario1("O5").noTiene("Ninfas", "SandaliasAladas")
                    .variante(VarianteMito.PEGASO), semilla));
            assertEquals(ResultadoMision.CONSEGUIDO, barajado(escenario1("O6").noTiene("Hermes", "HozAcero")
                    .variante(VarianteMito.ESPADA_HEFESTO), semilla));
        }
    }

    /** Igual que MotorReglas.ejecutar pero insertando los hechos en un orden aleatorio reproducible. */
    private static ResultadoMision barajado(PreparadorEscenario p, int semilla) {
        EscenarioMitologico esc = p.construir();
        KieSession sesion = contenedor.newKieSession("mitologiaSesion");
        try {
            sesion.setGlobal(MotorReglas.GLOBAL_REGISTRO, esc.getRegistro());
            List<Object> hechos = esc.getHechosParaDrools();
            Collections.shuffle(hechos, new Random(semilla));
            for (Object h : hechos) sesion.insert(h);
            assertTrue(sesion.fireAllRules(LIMITE) < LIMITE);
            esc.getEstado().sincronizarDesde(sesion.getObjects());
            return esc.evaluarResultado();
        } finally {
            sesion.dispose();
        }
    }
}

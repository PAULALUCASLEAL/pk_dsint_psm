package dsi.mitologia.motor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import dsi.mitologia.entrada.LectorEscenario;
import dsi.mitologia.escenario.DominioMitologico;
import dsi.mitologia.escenario.EscenarioMitologico;
import dsi.mitologia.escenario.EstadoAventura;
import dsi.mitologia.escenario.PreparadorEscenario;
import dsi.mitologia.escenario.ResultadoMision;
import dsi.mitologia.eventos.EventoMitologico;
import dsi.mitologia.eventos.TipoAccion;
import dsi.mitologia.hechos.Ubicacion;
import dsi.mitologia.modelo.enums.EstadoVital;

public class ReglasCretaTeseoTest {

    private static KieContainer contenedor;
    private static DominioMitologico dominio;

    @BeforeClass
    public static void cargar() {
        contenedor = KieServices.Factory.get().getKieClasspathContainer();
        dominio = PreparadorEscenario.cargarDominio(new File("../ontologia/mitologia.owl"));
    }

    @Test
    public void teseoConElHiloMataAlMinotauroYSale() throws Exception {
        EscenarioMitologico esc = ejecutar(
                "Puede Teseo matar a Minotauro?",
                "Condiciones:",
                "Teseo tiene el favor de Ariadna");
        EstadoAventura e = esc.getEstado();
        assertEquals(ResultadoMision.CONSEGUIDO, esc.getResultado());
        assertEquals(EstadoVital.MUERTO, e.getPersonaje("Minotauro").getEstadoVital());
        assertEquals("MundoExterior", lugarDe(e, "Teseo"));
        assertTrue(e.posee("Teseo", "CabezaMinotauro"));
        assertTrue(e.posee("Teseo", "CuernoMinotauro"));
    }

    @Test
    public void laHistoriaVaEnOrden() throws Exception {
        EscenarioMitologico esc = ejecutar(
                "Puede Teseo matar a Minotauro?",
                "Condiciones:",
                "Teseo tiene el favor de Ariadna");
        List<String> pasos = descripciones(esc);
        int hilo = indice(pasos, "Teseo obtiene HiloAriadna debido a tener el favor de Ariadna");
        int entra = indice(pasos, "Teseo entra en LaberintoCreta");
        int mata = indice(pasos, "Teseo mata a Minotauro");
        int sale = indice(pasos, "Teseo sale de LaberintoCreta");
        assertTrue(hilo >= 0 && hilo < entra && entra < mata && mata < sale);
    }

    @Test
    public void sinElHiloTeseoNoEntra() throws Exception {
        EscenarioMitologico esc = ejecutar(
                "Puede Teseo matar a Minotauro?",
                "Condiciones:",
                "Perseo tiene el favor de Atenea");
        EstadoAventura e = esc.getEstado();
        assertEquals(ResultadoMision.NO_CONSEGUIDO, esc.getResultado());
        assertEquals(EstadoVital.VIVO, e.getPersonaje("Minotauro").getEstadoVital());
        assertEquals("MundoExterior", lugarDe(e, "Teseo"));
    }

    @Test
    public void laHistoriaDePerseoNoPasaPorElLaberinto() throws Exception {
        EscenarioMitologico esc = ejecutar(
                "Puede Perseo liberar a Andromeda?",
                "Condiciones:",
                "Perseo tiene el favor de Atenea",
                "Perseo tiene el favor de Hermes",
                "Perseo tiene el favor de Hades",
                "Perseo tiene el favor de Hefesto",
                "Casiopea tiene el enojo de Poseidon");
        assertEquals(ResultadoMision.CONSEGUIDO, esc.getResultado());
        for (EventoMitologico ev : esc.getRegistro().getEventos()) {
            assertFalse(ev.getAccion() == TipoAccion.ENTRAR || ev.getAccion() == TipoAccion.SALIR);
        }
    }

    private static EscenarioMitologico ejecutar(String... lineas) throws Exception {
        File archivo = File.createTempFile("EscenarioPrueba", ".txt");
        try {
            Files.write(archivo.toPath(), String.join("\n", lineas).getBytes(StandardCharsets.UTF_8));
            EscenarioMitologico esc = new LectorEscenario().leer(archivo, dominio);
            KieSession sesion = contenedor.newKieSession("mitologiaSesion");
            try {
                MotorReglas.ejecutar(sesion, esc, 1000);
            } finally {
                sesion.dispose();
            }
            return esc;
        } finally {
            Files.deleteIfExists(archivo.toPath());
        }
    }

    private static String lugarDe(EstadoAventura e, String personaje) {
        for (Ubicacion u : e.getUbicaciones()) {
            if (u.getPersonaje().getId().equals(personaje)) return u.getLugar().getId();
        }
        return null;
    }

    private static List<String> descripciones(EscenarioMitologico esc) {
        List<String> lista = new ArrayList<String>();
        for (EventoMitologico ev : esc.getRegistro().getEventos()) {
            lista.add(ev.getDescripcion());
        }
        return lista;
    }

    private static int indice(List<String> pasos, String inicio) {
        for (int i = 0; i < pasos.size(); i++) {
            if (pasos.get(i).startsWith(inicio)) return i;
        }
        return -1;
    }
}

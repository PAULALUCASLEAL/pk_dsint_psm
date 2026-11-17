package dsi.mitologia.motor;

import static org.junit.Assert.assertEquals;
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
import dsi.mitologia.eventos.EventoMitologico;
import dsi.mitologia.hechos.Enojo;
import dsi.mitologia.hechos.Ubicacion;
import dsi.mitologia.modelo.enums.EstadoLibertad;

public class ReglasCretaDedaloTest {

    private static KieContainer contenedor;
    private static DominioMitologico dominio;

    @BeforeClass
    public static void cargar() {
        contenedor = KieServices.Factory.get().getKieClasspathContainer();
        dominio = PreparadorEscenario.cargarDominio(new File("../ontologia/mitologia.owl"));
    }

    @Test
    public void trasLaHuidaDeTeseoMinosEncierraADedaloYAIcaro() throws Exception {
        EstadoAventura e = ejecutar(
                "Puede Teseo matar a Minotauro?",
                "Condiciones:",
                "Teseo tiene el favor de Ariadna").getEstado();
        assertTrue(hayEnojo(e, "Dedalo", "Minos"));
        assertEncerrado(e, "Dedalo");
        assertEncerrado(e, "Icaro");
        assertTrue(e.tienePreso("Minos", "Dedalo"));
        assertTrue(e.tienePreso("Minos", "Icaro"));
    }

    @Test
    public void laHistoriaVaEnOrden() throws Exception {
        List<String> pasos = descripciones(ejecutar(
                "Puede Teseo matar a Minotauro?",
                "Condiciones:",
                "Teseo tiene el favor de Ariadna"));
        int sale = indice(pasos, "Teseo sale de LaberintoCreta");
        int enojo = indice(pasos, "Minos se enoja con Dedalo");
        int dedalo = indice(pasos, "Minos encierra a Dedalo");
        int icaro = indice(pasos, "Minos encierra a Icaro");
        assertTrue(sale >= 0 && sale < enojo && enojo < dedalo && dedalo < icaro);
    }

    @Test
    public void siTeseoNoEntraDedaloSigueLibre() throws Exception {
        EstadoAventura e = ejecutar(
                "Puede Teseo matar a Minotauro?",
                "Condiciones:",
                "Perseo tiene el favor de Atenea").getEstado();
        assertEquals(EstadoLibertad.LIBRE, e.getPersonaje("Dedalo").getEstadoLibertad());
        assertEquals("MundoExterior", lugarDe(e, "Dedalo"));
    }

    @Test
    public void elEnojoDeMinosDesdeElEscenarioTambienEncierra() throws Exception {
        EstadoAventura e = ejecutar(
                "Puede Perseo liberar a Andromeda?",
                "Condiciones:",
                "Dedalo tiene el enojo de Minos").getEstado();
        assertEncerrado(e, "Dedalo");
        assertEncerrado(e, "Icaro");
    }

    private static void assertEncerrado(EstadoAventura e, String id) {
        assertEquals(EstadoLibertad.PRESO, e.getPersonaje(id).getEstadoLibertad());
        assertEquals("LaberintoCreta", lugarDe(e, id));
    }

    private static boolean hayEnojo(EstadoAventura e, String personaje, String quien) {
        for (Enojo en : e.getEnojos()) {
            if (en.getPersonaje().getId().equals(personaje) && en.getDios().getId().equals(quien)) return true;
        }
        return false;
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

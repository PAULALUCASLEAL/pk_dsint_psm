package dsi.mitologia.motor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import dsi.mitologia.entrada.LectorEscenario;
import dsi.mitologia.escenario.DominioMitologico;
import dsi.mitologia.escenario.EscenarioMitologico;
import dsi.mitologia.escenario.PreparadorEscenario;
import dsi.mitologia.eventos.EventoMitologico;
import dsi.mitologia.eventos.TipoAccion;
import dsi.mitologia.modelo.enums.EstadoLibertad;

public class FavorYEnojoEntrePersonajesTest {

    private static KieContainer contenedor;
    private static DominioMitologico dominio;

    @BeforeClass
    public static void cargar() {
        contenedor = KieServices.Factory.get().getKieClasspathContainer();
        dominio = PreparadorEscenario.cargarDominio(new File("../ontologia/mitologia.owl"));
    }

    @Test
    public void ariadnaDaElHiloATeseoSiTieneSuFavor() throws Exception {
        EscenarioMitologico esc = ejecutar(
                "Puede Teseo matar a Minotauro?",
                "Condiciones:",
                "Teseo tiene el favor de Ariadna");
        assertTrue(esc.getEstado().posee("Teseo", "HiloAriadna"));
        boolean contado = false;
        for (EventoMitologico ev : esc.getRegistro().getEventos()) {
            if (ev.getDescripcion().equals("Teseo obtiene HiloAriadna debido a tener el favor de Ariadna")) {
                contado = true;
            }
        }
        assertTrue(contado);
    }

    @Test
    public void sinElFavorAriadnaConservaElHilo() throws Exception {
        EscenarioMitologico esc = ejecutar(
                "Puede Teseo matar a Minotauro?",
                "Condiciones:",
                "Perseo tiene el favor de Atenea");
        assertTrue(esc.getEstado().posee("Ariadna", "HiloAriadna"));
    }

    @Test
    public void elEnojoDeMinosSeAceptaYNoSueltaCriaturas() throws Exception {
        EscenarioMitologico esc = ejecutar(
                "Puede Perseo liberar a Andromeda?",
                "Condiciones:",
                "Dedalo tiene el enojo de Minos");
        boolean hayEnojoDeMinos = false;
        for (dsi.mitologia.hechos.Enojo en : esc.getEstado().getEnojos()) {
            if (en.getDios().getId().equals("Minos") && en.getPersonaje().getId().equals("Dedalo")) {
                hayEnojoDeMinos = true;
            }
        }
        assertTrue(hayEnojoDeMinos);
        for (EventoMitologico ev : esc.getRegistro().getEventos()) {
            if (ev.getAccion() == TipoAccion.QUEDAR_LIBRE) {
                assertFalse(ev.getDestinatario().getId().equals("Minos"));
            }
        }
        assertEquals(EstadoLibertad.LIBRE, esc.getEstado().getPersonaje("Dedalo").getEstadoLibertad());
    }

    private static EscenarioMitologico ejecutar(String... lineas) throws Exception {
        File archivo = File.createTempFile("EscenarioPrueba", ".txt");
        try {
            Files.write(archivo.toPath(), String.join("\n", lineas).getBytes(StandardCharsets.UTF_8));
            EscenarioMitologico esc = new LectorEscenario().leer(archivo, dominio);
            KieSession sesion = contenedor.newKieSession("mitologiaSesion");
            try {
                MotorReglas.ejecutar(sesion, esc);
            } finally {
                sesion.dispose();
            }
            return esc;
        } finally {
            Files.deleteIfExists(archivo.toPath());
        }
    }
}

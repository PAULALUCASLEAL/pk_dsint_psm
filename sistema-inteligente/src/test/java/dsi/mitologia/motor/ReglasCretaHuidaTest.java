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
import dsi.mitologia.escenario.ResultadoMision;
import dsi.mitologia.eventos.EventoMitologico;
import dsi.mitologia.hechos.EstadoFisicoObjeto;
import dsi.mitologia.hechos.Ubicacion;
import dsi.mitologia.modelo.enums.EstadoLibertad;
import dsi.mitologia.modelo.enums.EstadoVital;

public class ReglasCretaHuidaTest {

    private static KieContainer contenedor;
    private static DominioMitologico dominio;

    @BeforeClass
    public static void cargar() {
        contenedor = KieServices.Factory.get().getKieClasspathContainer();
        dominio = PreparadorEscenario.cargarDominio(new File("../ontologia/mitologia.owl"));
    }

    @Test
    public void elFinalEsElQuePideElEnunciado() throws Exception {
        EscenarioMitologico esc = creta();
        EstadoAventura e = esc.getEstado();
        assertEquals(ResultadoMision.CONSEGUIDO, esc.getResultado());
        assertTrue(e.haMatado("Teseo", "Minotauro"));
        assertEquals(EstadoVital.MUERTO, e.getPersonaje("Icaro").getEstadoVital());
        assertEquals(EstadoVital.VIVO, e.getPersonaje("Dedalo").getEstadoVital());
        assertEquals(EstadoLibertad.LIBRE, e.getPersonaje("Dedalo").getEstadoLibertad());
        assertEquals("MundoExterior", lugarDe(e, "Dedalo"));
    }

    @Test
    public void lasAlasDeIcaroQuedanRotasYLasDeDedaloIntactas() throws Exception {
        EstadoAventura e = creta().getEstado();
        assertTrue(e.posee("Dedalo", "AlasDedalo"));
        assertTrue(e.posee("Icaro", "AlasIcaro"));
        assertEquals("roto", condicionDe(e, "AlasIcaro"));
        assertEquals("intacto", condicionDe(e, "AlasDedalo"));
    }

    @Test
    public void laHuidaVaEnOrden() throws Exception {
        List<String> pasos = descripciones(creta());
        int encierra = indice(pasos, "Minos encierra a Icaro");
        int fabrica = indice(pasos, "Dedalo fabrica AlasDedalo");
        int escapa = indice(pasos, "Dedalo escapa volando");
        int muere = indice(pasos, "Icaro muere al escapar volando");
        assertTrue(encierra >= 0 && encierra < fabrica && fabrica < escapa && escapa < muere);
    }

    @Test
    public void sinTeseoNadieFabricaAlas() throws Exception {
        EstadoAventura e = ejecutar(
                "Puede Teseo matar a Minotauro?",
                "Condiciones:",
                "Perseo tiene el favor de Atenea").getEstado();
        assertEquals(EstadoVital.VIVO, e.getPersonaje("Icaro").getEstadoVital());
        assertEquals(null, e.poseedorDe(e.getObjeto("AlasDedalo")));
    }

    private static EscenarioMitologico creta() throws Exception {
        return ejecutar("Puede Teseo matar a Minotauro?", "Condiciones:", "Teseo tiene el favor de Ariadna");
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

    private static String condicionDe(EstadoAventura e, String objeto) {
        for (EstadoFisicoObjeto ef : e.getEstadosFisicos()) {
            if (ef.getObjeto().getId().equals(objeto)) return ef.getCondicion();
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

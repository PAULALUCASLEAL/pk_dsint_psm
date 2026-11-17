package dsi.mitologia.ontologia;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import dsi.mitologia.escenario.DominioMitologico;
import dsi.mitologia.escenario.EstadoAventura;
import dsi.mitologia.hechos.EstadoFisicoObjeto;
import dsi.mitologia.hechos.Ubicacion;

public class EstadoInicialCretaTest {

    private static DominioMitologico dominio;

    @BeforeClass
    public static void cargar() {
        dominio = new CargadorOntologia().cargar(new File("../ontologia/mitologia.owl"));
    }

    @Test
    public void nadieTieneFavoresNiEnojosHumanosAlEmpezar() {
        EstadoAventura e = EstadoAventura.inicial(dominio);
        assertTrue(!e.tieneFavor(e.getPersonaje("Teseo"), e.getPersonaje("Ariadna")));
        for (dsi.mitologia.hechos.Enojo en : e.getEnojos()) {
            assertTrue(en.getDios().isDios());
        }
    }

    @Test
    public void elMinotauroEmpiezaDentroDelLaberinto() {
        assertEquals("LaberintoCreta", lugarDe(EstadoAventura.inicial(dominio), "Minotauro"));
    }

    @Test
    public void teseoYDedaloEmpiezanFuera() {
        EstadoAventura e = EstadoAventura.inicial(dominio);
        assertEquals("MundoExterior", lugarDe(e, "Teseo"));
        assertEquals("MundoExterior", lugarDe(e, "Dedalo"));
    }

    @Test
    public void lasAlasEmpiezanIntactas() {
        EstadoAventura e = EstadoAventura.inicial(dominio);
        assertEquals("intacto", condicionDe(e, "AlasIcaro"));
        assertEquals("intacto", condicionDe(e, "AlasDedalo"));
    }

    @Test
    public void losHechosDeCretaSobrevivenAlViajeADrools() {
        EstadoAventura e = EstadoAventura.inicial(dominio);
        int ubicaciones = e.getUbicaciones().size();
        int estados = e.getEstadosFisicos().size();
        List<Object> sesion = e.getHechos();
        e.sincronizarDesde(sesion);
        assertTrue(ubicaciones > 0);
        assertEquals(ubicaciones, e.getUbicaciones().size());
        assertEquals(estados, e.getEstadosFisicos().size());
    }

    @Test
    public void cadaEscenarioTieneSusPropiasUbicaciones() {
        EstadoAventura a = EstadoAventura.inicial(dominio);
        EstadoAventura b = a.copia();
        ubicacionDe(a, "Teseo").setLugar(dominio.getLugar("LaberintoCreta"));
        assertEquals("MundoExterior", lugarDe(b, "Teseo"));
    }

    private static Ubicacion ubicacionDe(EstadoAventura e, String personaje) {
        for (Ubicacion u : e.getUbicaciones()) {
            if (u.getPersonaje().getId().equals(personaje)) return u;
        }
        return null;
    }

    private static String lugarDe(EstadoAventura e, String personaje) {
        Ubicacion u = ubicacionDe(e, personaje);
        return u == null ? null : u.getLugar().getId();
    }

    private static String condicionDe(EstadoAventura e, String objeto) {
        for (EstadoFisicoObjeto ef : e.getEstadosFisicos()) {
            if (ef.getObjeto().getId().equals(objeto)) return ef.getCondicion();
        }
        return null;
    }
}

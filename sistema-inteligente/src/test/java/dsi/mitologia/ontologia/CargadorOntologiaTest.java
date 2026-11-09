package dsi.mitologia.ontologia;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;

import dsi.mitologia.escenario.DominioMitologico;
import dsi.mitologia.escenario.EstadoAventura;
import dsi.mitologia.modelo.Personaje;
import dsi.mitologia.modelo.enums.CategoriaObjeto;
import dsi.mitologia.modelo.enums.EstadoLibertad;

/**
 * Comprueba que el modelo cargado coincide con lo que dice {@code ../ontologia/mitologia.owl}.
 *
 * <p>Por que importa: el OWL es la fuente del conocimiento por defecto de todos los escenarios. Si
 * alguien edita la ontologia en Protege (renombra un individuo, cambia una propiedad o la serializa en
 * otro formato) estas pruebas avisan antes de que las reglas razonen sobre un dominio incompleto. Los
 * numeros esperados se han contado en el propio fichero OWL; si la ontologia cambia a proposito, hay
 * que actualizarlos aqui.</p>
 *
 * <p>La ruta es relativa al directorio {@code sistema-inteligente}, que es desde donde Maven ejecuta
 * los tests.</p>
 */
public class CargadorOntologiaTest {

    /** Dominio cargado una sola vez: las pruebas solo leen, y {@link EstadoAventura#inicial} copia. */
    private static DominioMitologico dominio;
    /** Cargador usado, para consultar sus avisos. */
    private static CargadorOntologia cargador;

    /** Lee la ontologia real del proyecto (no una copia de test), para probar el fichero que se entrega. */
    @BeforeClass
    public static void cargar() {
        cargador = new CargadorOntologia();
        dominio = cargador.cargar(new File("../ontologia/mitologia.owl"));
    }

    /**
     * Se cargan todos los individuos y sin ningun aviso: 15 personajes (Andromeda, Atenea, Casiopea,
     * Ceto, Danae, Grayas, Hades, Hefesto, Hermes, Medusa, Ninfas, Pegaso, Perseo, Poseidon, Zeus),
     * 11 objetos y 4 capacidades (Invisibilidad, Petrificacion, Reflejo, Vuelo).
     *
     * <p>Detectaria individuos perdidos por un cambio de serializacion, clases nuevas no traducidas o
     * propiedades nuevas que el cargador ignoraria en silencio.</p>
     */
    @Test
    public void cargaTodosLosIndividuos() {
        assertEquals(15, dominio.getPersonajes().size());
        assertEquals(11, dominio.getObjetos().size());
        assertEquals(4, dominio.getCapacidades().size());
        // Cero avisos: con la ontologia buena todo debe traducirse; el mensaje muestra los avisos si no.
        assertTrue("Sin avisos: " + cargador.getAvisos(), cargador.getAvisos().isEmpty());
    }

    /**
     * Las clases OWL se traducen a tipos y categorias Java: Perseo tiene dos rdf:type (Heroe y Semidios),
     * las Ninfas son Dios y la HozAcero es Arma.
     *
     * <p>Detectaria que solo se lea el primer rdf:type, o que se confundan subclases de Personaje con
     * subclases de Objeto. Papel en el mito: las reglas casaran sobre el tipo del personaje (Perseo como
     * Heroe protagonista); y que las Ninfas sean Dios es lo que les permite conceder un favor, porque el
     * rango de tiene_favor es Dios.</p>
     */
    @Test
    public void perseoTieneLosTiposDelOwl() {
        Personaje perseo = dominio.getPersonaje("Perseo");
        assertTrue(perseo.isHeroe());
        assertTrue(perseo.isSemidios());
        assertTrue(dominio.getPersonaje("Ninfas").isDios());
        assertTrue(dominio.getObjeto("HozAcero").esDeCategoria(CategoriaObjeto.ARMA));
    }

    /**
     * Posesiones por defecto y conocimiento estatico de capacidades, escritos en el OWL a veces con
     * {@code tiene_objeto} y a veces con su inversa {@code esta_en_posesion_de}.
     *
     * <p>Detectaria inversas mal normalizadas (posesion al reves o duplicada), objetos sin dueno a los
     * que se les invente uno, o asertos {@code proporciona_capacidad} perdidos. Papel en el mito: Hades
     * guarda el casco, las Ninfas las sandalias, las Grayas el ojo; el EscudoEspejo no existe hasta que
     * se transforma el de bronce; las Sandalias dan Vuelo y la CabezaMedusa da Petrificacion.</p>
     */
    @Test
    public void posesionesYCapacidadesDelOwl() {
        // Arrange: estado inicial de un escenario a partir del dominio cargado.
        EstadoAventura e = EstadoAventura.inicial(dominio);
        // Assert: duenos por defecto.
        assertTrue(e.posee("Hades", "CascoHades"));
        assertTrue(e.posee("Ninfas", "SandaliasAladas"));
        assertTrue(e.posee("Grayas", "OjoGrayas"));
        assertEquals(null, e.poseedorDe(e.getObjeto("EscudoEspejo")));
        // Assert: objetos que proporcionan capacidades (exactamente los 4 asertos del OWL).
        assertTrue(e.proporciona(e.getObjeto("SandaliasAladas"), e.getCapacidad("Vuelo")));
        assertTrue(e.proporciona(e.getObjeto("CabezaMedusa"), e.getCapacidad("Petrificacion")));
        assertEquals(4, dominio.getProporciona().size());
    }

    /**
     * Relaciones dinamicas y estados de partida: Andromeda esta presa (estado_libertad) y su captor es
     * Ceto (esta_preso_por / apresa_a, escritos en ambos lados); Casiopea tiene el enojo de Poseidon;
     * hay 3 filiaciones (Perseo hijo de Zeus y de Danae, Andromeda hija de Casiopea) y ningun favor.
     *
     * <p>Detectaria un cautiverio duplicado por leer las dos inversas, o un favor inventado: los favores
     * de Atenea, Hermes, Hefesto y Hades NO estan en el OWL, deben llegar como condiciones iniciales del
     * escenario. Papel en el mito: es el punto de partida de la historia (Andromeda encadenada ante Ceto
     * por la ofensa de Casiopea a Poseidon).</p>
     */
    @Test
    public void relacionesDinamicasYEstadosDelOwl() {
        EstadoAventura e = EstadoAventura.inicial(dominio);
        Personaje andromeda = e.getPersonaje("Andromeda");
        assertEquals(EstadoLibertad.PRESO, andromeda.getEstadoLibertad());
        assertEquals("Ceto", e.captorDe(andromeda).getId());
        assertTrue(e.tieneEnojo(e.getPersonaje("Casiopea"), e.getPersonaje("Poseidon")));
        assertEquals(3, e.getFiliaciones().size());
        // Sin favores por defecto: el OWL no contiene ningun aserto tiene_favor.
        assertFalse(e.getFavores().iterator().hasNext());
    }
}

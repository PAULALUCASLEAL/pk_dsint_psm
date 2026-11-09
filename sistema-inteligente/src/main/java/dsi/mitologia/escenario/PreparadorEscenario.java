package dsi.mitologia.escenario;

import java.io.File;
import java.util.EnumSet;

import dsi.mitologia.hechos.VarianteMito;
import dsi.mitologia.modelo.Objeto;
import dsi.mitologia.modelo.Personaje;
import dsi.mitologia.modelo.enums.CategoriaObjeto;
import dsi.mitologia.ontologia.CargadorOntologia;

/**
 * Prepara un {@link EscenarioMitologico} de la fase 1.b a partir del OWL y de las condiciones del
 * fichero de pregunta ("Perseo tiene el favor de Atenea", "Atenea no tiene Escudo de Bronce"...).
 *
 * <p>Relacion con la nota O del enunciado: esta clase SOLO procesa datos de entrada. No deduce nada del
 * mito: traduce cada condicion a una operacion de {@link EstadoAventura} (anadir un favor, retirar una
 * posesion, dar un objeto) y guarda su texto. Toda la cadena causal la deducen las reglas Drools.</p>
 *
 * <p>Reglas del catalogo que implementa (son control de entrada, no reglas DRL):</p>
 * <ul>
 *   <li>R32-R34: negaciones explicitas ({@link #noTiene}) que retiran la posesion por defecto del OWL.</li>
 *   <li>R35: objetos alternativos del heroe ({@link #tiene}).</li>
 *   <li>R46: solo se niega lo que el escenario niega; que el OWL no diga algo no lo hace falso.</li>
 *   <li>R47: el escenario prevalece sobre el conocimiento por defecto de la ontologia.</li>
 *   <li>R48: cada escenario parte de una copia nueva del estado (ver {@link EstadoAventura#inicial}).</li>
 * </ul>
 *
 * <p>Estado previo al mito (discrepancia D2 de MODELO_JAVA.md): el OWL describe a Andromeda ya presa de
 * Ceto, que es el <em>resultado</em> de la primera parte del mito, pero el enunciado espera que el sistema
 * deduzca "Ceto queda libre debido al enojo de Poseidon" y "Andromeda queda presa debido a estar Ceto
 * libre". Por eso el punto de partida de cada escenario es el momento anterior: Andromeda libre y Ceto
 * retenido por Poseidon (el enunciado dice que Ceto "fue liberada por Poseidon", luego antes estaba
 * retenida por el). Es una decision de diseno documentada en REGLAS_DROOLS.md.</p>
 */
public final class PreparadorEscenario {

    private final EscenarioMitologico escenario;
    private final EstadoAventura estado;

    /**
     * Carga el OWL y anade los objetos de los escenarios alternativos que la ontologia no contiene
     * (discrepancia D5). Se llama una vez y el dominio resultante se comparte entre escenarios.
     */
    public static DominioMitologico cargarDominio(File owl) {
        DominioMitologico d = new CargadorOntologia().cargar(owl);
        ampliarDominio(d);
        return d;
    }

    /**
     * Conocimiento adicional del dominio que exigen los escenarios del enunciado y que el OWL no tiene.
     *
     * <ul>
     *   <li>AnilloInvisibilidad proporciona Invisibilidad, EspejoMano proporciona Reflejo y ConjuroVuelo
     *       proporciona Vuelo: el segundo escenario del enunciado los usa como sustitutos del casco, del
     *       escudo-espejo y de las sandalias. El conjuro se modela como Objeto (decision D6).</li>
     *   <li>Pegaso tiene la capacidad propia de Vuelo (es el caballo alado): necesaria para la variante en
     *       la que Perseo vuela montandolo.</li>
     * </ul>
     * Si una version futura del OWL ya los contiene, no se duplican.
     */
    public static void ampliarDominio(DominioMitologico d) {
        objetoSiFalta(d, "AnilloInvisibilidad", CategoriaObjeto.ARTEFACTO_MAGICO);
        objetoSiFalta(d, "EspejoMano", CategoriaObjeto.PROTECCION);
        objetoSiFalta(d, "ConjuroVuelo", CategoriaObjeto.ARTEFACTO_MAGICO);
        d.registrarProporciona("AnilloInvisibilidad", "Invisibilidad");
        d.registrarProporciona("EspejoMano", "Reflejo");
        d.registrarProporciona("ConjuroVuelo", "Vuelo");
        d.registrarCapacidadPropia("Pegaso", "Vuelo");
    }

    private static void objetoSiFalta(DominioMitologico d, String id, CategoriaObjeto categoria) {
        if (!d.existeObjeto(id)) d.registrarObjeto(new Objeto(id, id, EnumSet.of(categoria)));
    }

    /**
     * Crea el escenario con su copia del estado y aplica el estado previo al mito.
     */
    public PreparadorEscenario(String id, String pregunta, Objetivo objetivo, DominioMitologico dominio) {
        this.escenario = new EscenarioMitologico(id, pregunta, objetivo, dominio);
        this.estado = escenario.getEstado();
        aplicarEstadoPrevioAlMito();
    }

    /** Andromeda libre (aun no apresada) y Ceto retenido por Poseidon (aun no liberado). */
    private void aplicarEstadoPrevioAlMito() {
        Personaje ceto = estado.getPersonaje("Ceto");
        Personaje andromeda = estado.getPersonaje("Andromeda");
        estado.retirarCautiverio(ceto, andromeda);
        if (estado.captorDe(ceto) == null) estado.apresar(estado.getPersonaje("Poseidon"), ceto);
    }

    /** "X tiene el favor de D". */
    public PreparadorEscenario favor(String personajeId, String diosId) {
        estado.agregarFavor(estado.getPersonaje(personajeId), estado.getPersonaje(diosId));
        escenario.agregarCondicion(personajeId + " tiene el favor de " + diosId);
        return this;
    }

    /** "X tiene el enojo de D". */
    public PreparadorEscenario enojo(String personajeId, String diosId) {
        estado.agregarEnojo(estado.getPersonaje(personajeId), estado.getPersonaje(diosId));
        escenario.agregarCondicion(personajeId + " tiene el enojo de " + diosId);
        return this;
    }

    /**
     * "X tiene O" (R35). Si por defecto otro personaje lo tenia, el escenario prevalece (R47): se le
     * retira y se le da a X.
     */
    public PreparadorEscenario tiene(String personajeId, String objetoId) {
        Personaje p = estado.getPersonaje(personajeId);
        Objeto o = estado.getObjeto(objetoId);
        Personaje actual = estado.poseedorDe(o);
        if (actual == null || !actual.equals(p)) {
            if (actual != null) estado.retirarObjeto(actual, o);
            estado.otorgarObjeto(p, o);
        }
        escenario.agregarCondicion(personajeId + " tiene " + objetoId);
        return this;
    }

    /**
     * "X no tiene O" (R32-R34): negacion EXPLICITA. Retira la posesion por defecto si existia. Si X no lo
     * tenia, no hay nada que retirar, pero la condicion se guarda igualmente.
     */
    public PreparadorEscenario noTiene(String personajeId, String objetoId) {
        Personaje p = estado.getPersonaje(personajeId);
        Objeto o = estado.getObjeto(objetoId);
        if (estado.posee(p, o)) estado.retirarObjeto(p, o);
        escenario.agregarCondicion(personajeId + " no tiene " + objetoId);
        return this;
    }

    /** Activa una variante narrativa (constantes de {@link VarianteMito}). */
    public PreparadorEscenario variante(String nombre) {
        escenario.agregarVariante(new VarianteMito(nombre));
        escenario.agregarCondicion("Variante del mito: " + nombre);
        return this;
    }

    public EscenarioMitologico construir() {
        return escenario;
    }
}

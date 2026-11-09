package dsi.mitologia.modelo;

import java.util.EnumSet;

import dsi.mitologia.escenario.DominioMitologico;
import dsi.mitologia.modelo.enums.CategoriaObjeto;
import dsi.mitologia.modelo.enums.TipoPersonaje;

/**
 * Dominio pequeno construido a mano para las pruebas del modelo y del motor.
 *
 * <p>Decision de diseno: no depende del OWL. Asi las pruebas del modelo Java fallan solo si falla el
 * modelo, y no porque alguien haya editado {@code mitologia.owl} en Protege; la coherencia con la
 * ontologia ya la comprueba {@code CargadorOntologiaTest}. Se usan individuos reales del mito (Perseo,
 * Hades, Atenea, Medusa, Ceto, Andromeda, Pegaso) con los mismos identificadores que en el OWL para que
 * las pruebas se lean como pasos de la historia.</p>
 *
 * <p>Incluye dos objetos que NO estan en el OWL (AnilloInvisibilidad, ConjuroVuelo) para probar fuentes
 * alternativas de una misma capacidad: que perder una fuente no quite la capacidad si queda otra, y que
 * una capacidad pueda venir de un objeto, de un conjuro o de una montura. Pegaso se incluye como
 * montura posible para el Vuelo.</p>
 *
 * <p>Relacion con la ontologia: reproduce las posesiones por defecto Hades tiene_objeto CascoHades y
 * Atenea tiene_objeto EscudoBronce, y los asertos CascoHades proporciona_capacidad Invisibilidad y
 * EscudoEspejo proporciona_capacidad Reflejo. EscudoEspejo queda sin dueno, como en el OWL, porque en
 * el mito es el EscudoBronce transformado.</p>
 */
public final class DominioDePrueba {

    /** Clase de utilidad: solo expone la factoria {@link #crear()}. */
    private DominioDePrueba() {
    }

    /** Devuelve un dominio nuevo en cada llamada, para que ninguna prueba contamine a otra. */
    public static DominioMitologico crear() {
        DominioMitologico d = new DominioMitologico();
        // Personajes: Perseo con dos tipos a la vez (Heroe y Semidios), como en el OWL.
        d.registrarPersonaje(new Personaje("Perseo", "Perseo", EnumSet.of(TipoPersonaje.HEROE, TipoPersonaje.SEMIDIOS)));
        d.registrarPersonaje(new Personaje("Hades", "Hades", EnumSet.of(TipoPersonaje.DIOS)));
        d.registrarPersonaje(new Personaje("Atenea", "Atenea", EnumSet.of(TipoPersonaje.DIOS)));
        d.registrarPersonaje(new Personaje("Medusa", "Medusa", EnumSet.of(TipoPersonaje.CRIATURA)));
        d.registrarPersonaje(new Personaje("Ceto", "Ceto", EnumSet.of(TipoPersonaje.CRIATURA)));
        d.registrarPersonaje(new Personaje("Andromeda", "Andromeda", EnumSet.of(TipoPersonaje.MORTAL)));
        d.registrarPersonaje(new Personaje("Pegaso", "Pegaso", EnumSet.of(TipoPersonaje.CRIATURA)));

        // Objetos: los tres primeros existen en el OWL; anillo y conjuro son solo de prueba.
        d.registrarObjeto(new Objeto("CascoHades", "Casco de Hades", EnumSet.of(CategoriaObjeto.PROTECCION)));
        d.registrarObjeto(new Objeto("EscudoBronce", "Escudo de Bronce", EnumSet.of(CategoriaObjeto.PROTECCION)));
        d.registrarObjeto(new Objeto("EscudoEspejo", "Escudo-Espejo", EnumSet.of(CategoriaObjeto.PROTECCION)));
        d.registrarObjeto(new Objeto("AnilloInvisibilidad", "Anillo de Invisibilidad", EnumSet.of(CategoriaObjeto.ARTEFACTO_MAGICO)));
        d.registrarObjeto(new Objeto("ConjuroVuelo", "Conjuro de Vuelo", EnumSet.of(CategoriaObjeto.ARTEFACTO_MAGICO)));

        d.registrarCapacidad(new Capacidad("Invisibilidad", "Invisibilidad"));
        d.registrarCapacidad(new Capacidad("Vuelo", "Vuelo"));
        d.registrarCapacidad(new Capacidad("Reflejo", "Reflejo"));

        // Conocimiento estatico: Invisibilidad tiene dos fuentes para probar el mantenimiento de la verdad.
        d.registrarProporciona("CascoHades", "Invisibilidad");
        d.registrarProporciona("AnilloInvisibilidad", "Invisibilidad");
        d.registrarProporciona("EscudoEspejo", "Reflejo");
        d.registrarProporciona("ConjuroVuelo", "Vuelo");

        // Estado por defecto: solo los duenos del OWL; anillo y conjuro empiezan sin dueno.
        d.registrarPosesion("Hades", "CascoHades");
        d.registrarPosesion("Atenea", "EscudoBronce");
        return d;
    }
}

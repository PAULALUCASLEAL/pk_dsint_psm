package dsi.mitologia.modelo.enums;

/**
 * De donde procede una capacidad disponible.
 *
 * <p>Relacion con la ontologia: la ontologia solo tiene {@code tiene_capacidad} (Personaje -&gt;
 * Capacidad) y {@code proporciona_capacidad} (Objeto -&gt; Capacidad); el origen es una decision del
 * modelo Java para poder retirar la capacidad cuando desaparece su fuente. No tiene equivalente OWL.</p>
 *
 * <p>Papel en el mito: Perseo obtiene Invisibilidad del CascoHades (OBJETO), Vuelo de las
 * SandaliasAladas (OBJETO), de un Conjuro de Vuelo (CONJURO) o de Pegaso con la BridaDorada (MONTURA).
 * Si Perseo entrega un objeto (p. ej. el OjoGrayas a las Ninfas) o lo pierde, las capacidades cuyo
 * origen era ese objeto deben desaparecer, pero no las que vienen de otra fuente.</p>
 *
 * <p>Decision de diseno: guardar el origen en el hecho {@code CapacidadDisponible} junto con la fuente
 * concreta, en lugar de recalcular las capacidades desde las posesiones. Asi las reglas de retirada son
 * simples ({@code delete} del hecho cuya fuente ya no se posee) y la historia puede explicar de donde
 * salio cada capacidad. Alternativa descartada: usar solo {@code tiene_capacidad} sin origen, que no
 * permite distinguir dos fuentes de la misma capacidad.</p>
 */
public enum OrigenCapacidad {
    /** Capacidad propia del personaje (p. ej. la fuerza de Heracles). Sin fuente. */
    PROPIA,
    /** La da un objeto que el personaje posee (CascoHades -> Invisibilidad). */
    OBJETO,
    /** La da un conjuro que el personaje posee (se representa como un Objeto). */
    CONJURO,
    /** La da una criatura que el personaje tiene capturada y monta (Pegaso -> Vuelo). */
    MONTURA
}

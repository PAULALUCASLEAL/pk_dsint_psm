package dsi.mitologia.hechos;

import dsi.mitologia.modelo.Objeto;
import dsi.mitologia.modelo.Personaje;

/**
 * Hecho dinamico: quien posee actualmente un objeto.
 *
 * <p>Relacion con la ontologia: corresponde al par de ObjectProperties inversas
 * {@code tiene_objeto} (Personaje -> Objeto, allValuesFrom Objeto) /
 * {@code esta_en_posesion_de} (Objeto -> Personaje). Un unico hecho representa ambas direcciones.</p>
 *
 * <p>Papel en el mito: en el OWL, Atenea tiene_objeto EscudoBronce, Hermes HozAcero, Hades
 * CascoHades, Hefesto EspadaIndestructible, las Grayas OjoGrayas y DienteGrayas, las Ninfas
 * SandaliasAladas y ZurronMagico, y Medusa CabezaMedusa. La aventura consiste en que estas
 * posesiones pasen a Perseo (por favor, robo, intercambio o muerte de Medusa).</p>
 *
 * <p>Uso en reglas: {@code $pos : Posesion(poseedor.id == "Atenea", objeto.id == "EscudoBronce")}
 * y {@code Favor(personaje == $h, dios.id == "Atenea")} ->
 * {@code modify($pos) { setPoseedor($h) }}.</p>
 *
 * <p>Decision de diseno: es la UNICA fuente de verdad de las posesiones: ni Personaje tiene una
 * lista de objetos ni Objeto tiene un propietario. Asi no hay dos representaciones que puedan
 * contradecirse y los antecedentes son patrones simples (notas K/L del profesor). Hay como mucho
 * una Posesion por objeto (lo garantiza {@code EstadoAventura}); un objeto sin Posesion no lo
 * tiene nadie.</p>
 *
 * <p>Transferir = cambiar el poseedor del mismo hecho. En Drools:
 * {@code modify($pos) { setPoseedor($heroe) }}. Por eso el objeto es fijo y solo el poseedor
 * cambia, y la igualdad es por identidad (el hecho conserva su identidad al transferirse).
 * Alternativa descartada: delete + insert de una Posesion nueva; perderia la identidad del hecho,
 * generaria dos eventos en la agenda en vez de uno y haria mas facil dejar el objeto duplicado.
 * Con {@code modify}, Drools reevalua las reglas y retira automaticamente las CapacidadDisponible
 * insertadas logicamente que dependian del poseedor anterior.</p>
 *
 * <p>Por ser mutable (setPoseedor) no se redefinen equals/hashCode: con igualdad por contenido el
 * hash cambiaria al transferir y el hecho se perderia en colecciones hash y en la memoria de
 * trabajo. Es la unica clase no final del paquete y la unica con setter por este motivo.</p>
 */
public class Posesion {

    /** Poseedor actual (sujeto de tiene_objeto). Mutable: cambia en cada transferencia. Nunca null. */
    private Personaje poseedor;
    /** Objeto poseido (sujeto de esta_en_posesion_de). Fijo: identifica al hecho. */
    private final Objeto objeto;

    /**
     * Crea la posesion inicial (desde el OWL o las condiciones de entrada) o la de un objeto que
     * aparece en la aventura (p. ej. la CabezaMedusa al morir Medusa, o el EscudoEspejo).
     * El poseedor se valida reutilizando {@link #setPoseedor}.
     */
    public Posesion(Personaje poseedor, Objeto objeto) {
        if (objeto == null) throw new IllegalArgumentException("Posesion necesita objeto");
        setPoseedor(poseedor);
        this.objeto = objeto;
    }

    /** Poseedor actual del objeto. */
    public Personaje getPoseedor() {
        return poseedor;
    }

    /**
     * Cambia el poseedor; es el setter que usa {@code modify} en los consecuentes de las reglas
     * de transferencia (entrega por favor, robo, intercambio).
     */
    public void setPoseedor(Personaje poseedor) {
        // Un hecho Posesion sin poseedor seria una posesion "fantasma": se exige retirar el hecho.
        if (poseedor == null) {
            throw new IllegalArgumentException("Para que nadie posea el objeto se retira la Posesion");
        }
        this.poseedor = poseedor;
    }

    /** Objeto poseido; no cambia durante la vida del hecho. */
    public Objeto getObjeto() {
        return objeto;
    }

    /** Texto legible para trazas: poseedor tiene objeto. */
    @Override
    public String toString() {
        return "Posesion(" + poseedor.getId() + " tiene " + objeto.getId() + ")";
    }
}

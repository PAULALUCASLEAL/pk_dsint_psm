package dsi.mitologia.escenario;

/**
 * Resultado de un escenario tras ejecutar las reglas.
 *
 * <p>Decide si la respuesta del fichero RespuestaF1_y.txt empieza por "Si, ... puede ..." o por
 * "No, ... no puede ...". Lo calcula {@link EscenarioMitologico#evaluarResultado()} a partir de
 * {@link Objetivo#seCumpleEn(EstadoAventura)} una vez que Drools ha terminado.</p>
 *
 * <p>Decision de diseno: se usa un enum de tres valores en lugar de un {@code boolean} para
 * distinguir "todavia no se ha ejecutado" de "se ha ejecutado y no se consigue"; asi la salida
 * nunca escribe un "No" por error si se consulta el resultado antes de disparar las reglas.</p>
 */
public enum ResultadoMision {
    /** Todavia no se ha evaluado (valor inicial de todo escenario recien creado). */
    PENDIENTE,
    /** El objetivo se cumple en el estado final (p. ej. existe el hecho Liberacion(Perseo, Andromeda)). */
    CONSEGUIDO,
    /** Las reglas terminaron (no queda nada por disparar) y el objetivo no se cumple. */
    NO_CONSEGUIDO
}

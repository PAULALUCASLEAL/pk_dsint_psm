package dsi.mitologia.escenario;

/**
 * Lo que pregunta el escenario: "Puede &lt;protagonista&gt; &lt;tipo&gt; &lt;destino&gt;?".
 *
 * <p>Ejemplos: (LIBERAR, Perseo, Andromeda), (OBTENER_OBJETO, Perseo, BridaDorada),
 * (OBTENER_CAPACIDAD, Perseo, Vuelo). Es la traduccion directa de la primera linea del fichero
 * de escenario, p. ej. "Puede Perseo liberar a Andromeda?".</p>
 *
 * <p>Papel en el mito: en el escenario principal el objetivo es (LIBERAR, Perseo, Andromeda); toda
 * la cadena causal (Escudo de Bronce, Grayas, Ninfas, Medusa, Ceto...) se dispara por reglas hasta
 * que aparece el hecho {@code Liberacion(Perseo, Andromeda)}.</p>
 *
 * <p>Decision de diseno: se guardan identificadores (los nombres de los individuos del OWL) y no
 * referencias a {@code Personaje}, porque el parser de escenarios los lee como texto y porque cada
 * escenario tiene sus propias copias de los personajes; un objetivo con ids sirve para cualquiera.
 * Es inmutable y se inserta en Drools como un hecho mas, de modo que las reglas pueden orientarse
 * a la mision (p. ej. solo intentar liberar a Andromeda si es lo que se pregunta).</p>
 */
public final class Objetivo {

    /** Mision preguntada (matar, apresar, liberar, obtener objeto, conseguir capacidad, localizar). */
    private final TipoObjetivo tipo;
    /** Id del personaje que debe cumplir la mision (sujeto de la pregunta, p. ej. "Perseo"). */
    private final String protagonistaId;
    /** Id del destino: personaje, objeto o capacidad segun el tipo (p. ej. "Andromeda", "Vuelo"). */
    private final String destinoId;

    /**
     * Crea el objetivo a partir de lo que el parser extrae de la pregunta.
     *
     * <p>No se comprueba aqui que los ids existan en el dominio: esa validacion depende del dominio
     * cargado y la hace el estado al evaluar ({@link #seCumpleEn(EstadoAventura)} lanza excepcion si
     * el id no existe). Asi el objetivo no depende de ninguna instancia concreta.</p>
     *
     * @throws IllegalArgumentException si falta alguno de los tres datos
     */
    public Objetivo(TipoObjetivo tipo, String protagonistaId, String destinoId) {
        if (tipo == null || protagonistaId == null || destinoId == null) {
            throw new IllegalArgumentException("El objetivo necesita tipo, protagonista y destino");
        }
        this.tipo = tipo;
        this.protagonistaId = protagonistaId;
        this.destinoId = destinoId;
    }

    /** Tipo de mision; lo usan las reglas para filtrar y la salida para elegir el verbo de la respuesta. */
    public TipoObjetivo getTipo() {
        return tipo;
    }

    /** Id del protagonista (sujeto de la frase "Si, Perseo puede ..."). */
    public String getProtagonistaId() {
        return protagonistaId;
    }

    /** Id del destino de la mision (complemento de la frase "... liberar a Andromeda"). */
    public String getDestinoId() {
        return destinoId;
    }

    /**
     * Comprueba (sin inferir nada) si el estado final contiene el hecho que pide el objetivo.
     * Solo mira hechos; las reglas son las que los habran producido.
     *
     * <p>Decision de diseno: el profesor exige que Java no infiera. Por eso este metodo es una
     * simple consulta de pertenencia sobre el estado sincronizado desde la memoria de trabajo; toda
     * la deduccion de si Perseo puede o no liberar a Andromeda la hacen las reglas Drools. Si las
     * reglas no han generado el hecho, la respuesta es "No" aunque "pudiera" deducirse en Java.</p>
     *
     * @param estado estado final del escenario, ya sincronizado con la KieSession
     * @return {@code true} si existe el hecho correspondiente a la mision
     */
    public boolean seCumpleEn(EstadoAventura estado) {
        // Cada caso consulta el hecho de la ObjectProperty OWL asociada a la mision.
        switch (tipo) {
            case MATAR:
                return estado.haMatado(protagonistaId, destinoId);
            case APRESAR:
                return estado.tienePreso(protagonistaId, destinoId);
            case LIBERAR:
                return estado.haLiberado(protagonistaId, destinoId);
            case OBTENER_OBJETO:
                return estado.posee(protagonistaId, destinoId);
            case OBTENER_CAPACIDAD:
                return estado.tieneCapacidad(protagonistaId, destinoId);
            case LOCALIZAR:
                return estado.haLocalizado(protagonistaId, destinoId);
            default:
                // Proteccion ante un valor nuevo del enum que se olvide anadir aqui.
                throw new IllegalStateException("Tipo de objetivo sin comprobar: " + tipo);
        }
    }

    /** Representacion para trazas y depuracion, p. ej. "Objetivo(Perseo LIBERAR Andromeda)". */
    @Override
    public String toString() {
        return "Objetivo(" + protagonistaId + " " + tipo + " " + destinoId + ")";
    }
}

package dsi.mitologia.escenario;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dsi.mitologia.eventos.RegistroEventos;
import dsi.mitologia.hechos.VarianteMito;

/**
 * Una ejecucion: pregunta + objetivo + condiciones iniciales + estado propio + eventos + resultado.
 *
 * Cada escenario crea su propio {EstadoAventura} (cómo están los hechos en ese momento, lo que es cierto AHORA) a partir del dominio y su propio
 * {RegistroEventos} (el historial de lo que ha ocurrido, lo que HA PASADO), asi que ejecutar uno no altera a los demas.
 *
 *El escenario mitológico recoge tanto lo que es cierto ahora, lo que ha pasado, la pregunta y el objetivo. 
 */
public class EscenarioMitologico {

    
    private final String id;
    //Pregunta tal cual aparece en el fichero ("Puede Perseo liberar a Andromeda?"). 
    private final String pregunta;
    //Mision estructurada extraida de la pregunta; se inserta en Drools y decide el resultado. 
    private final Objetivo objetivo;
    //Estado dinamico propio (copias de personajes y hechos); nunca compartido con otro escenario.
    private final EstadoAventura estado;
    //Secuencia de eventos que anotan las reglas (global {registro}); base de la explicacion.
    private final RegistroEventos registro = new RegistroEventos();
    //Texto original de las condiciones iniciales, en el orden del fichero, para repetirlas en la salida. 
    private final List<String> condiciones = new ArrayList<String>();
    //Resultado de la mision; PENDIENTE hasta que se evalua tras disparar las reglas. 
    private ResultadoMision resultado = ResultadoMision.PENDIENTE;
    //Variantes narrativas activadas por el escenario (hechos de control, ver {VarianteMito}). 
    private final List<VarianteMito> variantes = new ArrayList<VarianteMito>();

    /**
     * Crea el escenario y su estado inicial (estado por defecto del dominio ya aplicado).
     *
     * Las condiciones iniciales del fichero se aplican despues, sobre {getEstado()}, porque
     * modifican el conocimiento por defecto (p. ej. quitan a Atenea el Escudo de Bronce).
     *
     * @param id identificador del escenario (obligatorio)
     * @param pregunta texto de la pregunta (puede ser {null} si se crea por programa)
     * @param objetivo mision a comprobar (obligatoria)
     * @param dominio conocimiento por defecto cargado de la ontologia
     */
    public EscenarioMitologico(String id, String pregunta, Objetivo objetivo, DominioMitologico dominio) {
        if (id == null || objetivo == null) throw new IllegalArgumentException("El escenario necesita id y objetivo");
        this.id = id;
        this.pregunta = pregunta;
        this.objetivo = objetivo;
        // Copia propia del estado por defecto: aqui se garantiza la independencia entre escenarios.
        this.estado = EstadoAventura.inicial(dominio);
    }

    public String getId() {
        return id;
    }

    public String getPregunta() {
        return pregunta;
    }


    public Objetivo getObjetivo() {
        return objetivo;
    }
    
    public EstadoAventura getEstado() {
        return estado;
    }
    
    public RegistroEventos getRegistro() {
        return registro;
    }

    /**
     * Texto original de una condicion inicial (para mostrarla en la respuesta).
     *
     * Solo guarda el texto; no la interpreta. La interpretacion (convertirla en una operacion del
     * estado) es responsabilidad del parser, que es procesado de datos de entrada y no inferencia.
     */
    public void agregarCondicion(String condicion) {
        condiciones.add(condicion);
    }

    public List<String> getCondiciones() {
        return Collections.unmodifiableList(condiciones);
    }

    /** Activa una variante del mito para este escenario (se inserta como hecho en Drools). */
    public void agregarVariante(VarianteMito variante) {
        if (variante != null && !variantes.contains(variante)) variantes.add(variante);
    }

    /** Variantes activas (solo lectura). */
    public List<VarianteMito> getVariantes() {
        return Collections.unmodifiableList(variantes);
    }

    //Resultado actual; PENDIENTE si todavia no se ha llamado a evaluarResultado()}. 
    public ResultadoMision getResultado() {
        return resultado;
    }

    /**
     * Hechos para la KieSession: los del estado mas el propio objetivo y las variantes activas.
     *
     * Se inserta tambien el {Objetivo} para que las reglas puedan razonar dirigidas a la
     * mision preguntada. La lista devuelta es nueva en cada llamada, asi que anadirle el objetivo no
     * altera el estado.
     */
    //Recoge los hechos del estado actual, añade el objetivo y las variantes para que el motor Drools pueda razonar con ellos. 
    //Por ejemplo, Drools recibirá información como: Favor(Perseo, Atenea), Posesion(Atenea, EscudoBronce), Objetivo(Perseo, LIBERAR, Andromeda), evaluarResultado(): comprueba si hemos ganado
    public List<Object> getHechosParaDrools() {
        List<Object> hechos = estado.getHechos();
        hechos.add(objetivo);
        hechos.addAll(variantes);
        return hechos;
    }

    /**
     * Evalua el objetivo sobre el estado actual y guarda el resultado.
     *
     * <Debe llamarse despues de disparar las reglas y de sincronizar el estado; solo comprueba
     * hecho, no infiere.
     */
    //Esta función no ejecuta las reglas ni libera a Andrómeda. Solo comprueba si ya se ha conseguido liberarla.
    public ResultadoMision evaluarResultado() {
        resultado = objetivo.seCumpleEn(estado) ? ResultadoMision.CONSEGUIDO : ResultadoMision.NO_CONSEGUIDO;
        return resultado;
    }
}

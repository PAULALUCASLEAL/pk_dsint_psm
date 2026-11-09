package dsi.mitologia.eventos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Lista ordenada de los eventos de UN escenario. Se pasa a Drools como global {@code registro}.
 *
 * <p>Papel en el mito: aqui queda la secuencia que se escribe tras "Si, Perseo puede liberar a
 * Andromeda debido a que:" (Ceto queda libre, Andromeda presa, Perseo obtiene el Escudo de Bronce,
 * ..., Perseo petrifica a Ceto, Perseo libera a Andromeda).</p>
 *
 * <p>Decision de diseno: las reglas solo anotan eventos en sus consecuentes y la salida los lee
 * despues para escribir RespuestaF1_y.txt, sin depender de {@code System.out}. El numero de orden
 * refleja el orden real de disparo, y junto con las causas de cada evento permite reconstruir la
 * explicacion causal y el orden parcial que pide el enunciado (dos eventos sin relacion causal
 * pueden narrarse en cualquier orden; uno nunca va antes que sus causas).</p>
 *
 * <p>No es estatico: cada escenario tiene el suyo, para que la explicacion de un escenario no
 * arrastre eventos de otro.</p>
 *
 * <p>Alternativa descartada: insertar los eventos como hechos en la memoria de trabajo. Se descarta
 * porque podrian activar reglas por error y obligarian a recuperarlos y ordenarlos al final; un
 * global de solo anotacion no participa en el razonamiento.</p>
 */
public class RegistroEventos {

    /** Eventos en orden de registro; la posicion i guarda el evento con orden i + 1. */
    private final List<EventoMitologico> eventos = new ArrayList<EventoMitologico>();

    /**
     * Asigna el siguiente numero de orden (1, 2, 3...) y guarda el evento.
     *
     * <p>El orden lo pone el registro y no la regla, para que sea siempre consecutivo y unico aunque
     * varias reglas registren eventos. Devuelve el evento creado para que la regla pueda citarlo como
     * causa de eventos posteriores ({@code EventoMitologico.Constructor#causas}).</p>
     */
    public EventoMitologico registrar(EventoMitologico.Constructor constructor) {
        EventoMitologico evento = constructor.construir(eventos.size() + 1);
        eventos.add(evento);
        return evento;
    }

    /** Todos los eventos en orden (solo lectura); la salida los recorre para escribir la respuesta. */
    public List<EventoMitologico> getEventos() {
        return Collections.unmodifiableList(eventos);
    }

    /**
     * Evento por numero de orden (empieza en 1).
     *
     * <p>Sirve para resolver las causas de un evento, que se guardan como numeros de orden.</p>
     */
    public EventoMitologico getEvento(int orden) {
        return eventos.get(orden - 1);
    }

    /** Numero de eventos registrados. */
    public int size() {
        return eventos.size();
    }
}

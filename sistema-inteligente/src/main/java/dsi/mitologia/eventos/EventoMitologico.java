package dsi.mitologia.eventos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dsi.mitologia.modelo.Capacidad;
import dsi.mitologia.modelo.EntidadMitologica;
import dsi.mitologia.modelo.Objeto;
import dsi.mitologia.modelo.Personaje;

/**
 * Un acontecimiento de la historia, con su explicacion ("... debido a ...").
 *
 * <p>Cada evento es una linea de la secuencia causal de la respuesta: "X hace Y debido a Z". Por
 * ejemplo: "Perseo obtiene Escudo de Bronce debido a que tiene el favor de Atenea" se representa con
 * accion OBTENER_OBJETO, actor Perseo, objeto EscudoBronce y justificacion "tener el favor de Atenea".
 * "Ceto queda libre debido al enojo de Poseidon hacia Casiopea" es un QUEDAR_LIBRE sin actor.</p>
 *
 * <p>Relacion con la ontologia: el evento narra el momento en que aparece (o desaparece) un hecho de
 * una propiedad OWL ({@code mata_a}, {@code libera_a}, {@code localiza_a}, {@code tiene_objeto}...,
 * ver {@link TipoAccion}). El hecho vive en la memoria de trabajo; el evento solo cuenta por que.</p>
 *
 * <p>Inmutable. Lo crea {@link RegistroEventos#registrar(Constructor)}, que le asigna el orden.
 * Uso desde una regla:</p>
 * <pre>
 * registro.registrar(EventoMitologico.nuevo(TipoAccion.OBTENER_OBJETO, $h)
 *     .objeto($o).justificacion("tener el favor de Atenea")
 *     .regla(drools.getRule().getName()));
 * </pre>
 * <p>{@code causas} guarda el orden de los eventos que lo provocaron (relacion causal).</p>
 *
 * <p>Decision de diseno: se guardan orden, causas y nombre de regla porque con ellos la salida puede
 * (1) narrar en el orden real de disparo, (2) construir el orden parcial del enunciado a partir del
 * grafo de causas y (3) depurar que regla produjo cada paso. Las causas son numeros de orden y no
 * referencias para que el evento siga siendo un valor simple e inmutable.</p>
 */
public final class EventoMitologico {

    /** Numero de orden dentro del escenario (1, 2, 3...); lo asigna el registro. */
    private final int orden;
    /** Que ocurre (verbo de la linea de la respuesta). */
    private final TipoAccion accion;
    /** Quien realiza la accion; {@code null} si no hay autor (Ceto queda libre, muerte sin asesino). */
    private final Personaje actor;
    /** A quien o a que afecta la accion: personaje (Medusa, Andromeda) u objeto localizado. */
    private final EntidadMitologica destinatario;
    /** Objeto implicado, si lo hay (EscudoBronce, OjoGrayas, CabezaMedusa...). */
    private final Objeto objeto;
    /** Capacidad implicada, si la hay (Reflejo, Invisibilidad, Vuelo, Petrificacion). */
    private final Capacidad capacidad;
    /** Texto libre opcional que sustituye o completa la frase generada a partir de los campos. */
    private final String descripcion;
    /** Motivo del evento, lo que sigue a "debido a" (p. ej. "tener el favor de Hermes"). */
    private final String justificacion;
    /** Nombre de la regla Drools que lo registro (trazabilidad y depuracion). */
    private final String regla;
    /** Numeros de orden de los eventos que lo causan; lista inmutable, vacia si no hay causa previa. */
    private final List<Integer> causas;

    /** Solo lo usa {@link Constructor#construir(int)}: copia los datos del constructor y fija el orden. */
    private EventoMitologico(int orden, Constructor c) {
        this.orden = orden;
        this.accion = c.accion;
        this.actor = c.actor;
        this.destinatario = c.destinatario;
        this.objeto = c.objeto;
        this.capacidad = c.capacidad;
        this.descripcion = c.descripcion;
        this.justificacion = c.justificacion;
        this.regla = c.regla;
        // Copia defensiva: el constructor podria seguir usandose despues sin alterar este evento.
        this.causas = Collections.unmodifiableList(new ArrayList<Integer>(c.causas));
    }

    /**
     * Empieza a construir un evento. El actor puede ser {@code null} (p. ej. "Ceto queda libre").
     *
     * <p>Se llama desde los consecuentes de las reglas; el evento no existe hasta que se pasa a
     * {@link RegistroEventos#registrar(Constructor)}.</p>
     */
    public static Constructor nuevo(TipoAccion accion, Personaje actor) {
        return new Constructor(accion, actor);
    }

    /** Numero de orden (empieza en 1). */
    public int getOrden() {
        return orden;
    }

    /** Identificador estable dentro del escenario: "E1", "E2"... */
    public String getId() {
        return "E" + orden;
    }

    /** Accion narrada. */
    public TipoAccion getAccion() {
        return accion;
    }

    /** Autor de la accion o {@code null}. */
    public Personaje getActor() {
        return actor;
    }

    /** Personaje u objeto afectado, o {@code null}. */
    public EntidadMitologica getDestinatario() {
        return destinatario;
    }

    /** Objeto implicado, o {@code null}. */
    public Objeto getObjeto() {
        return objeto;
    }

    /** Capacidad implicada, o {@code null}. */
    public Capacidad getCapacidad() {
        return capacidad;
    }

    /** Descripcion libre opcional, o {@code null}. */
    public String getDescripcion() {
        return descripcion;
    }

    /** Motivo ("debido a ...") o {@code null}. */
    public String getJustificacion() {
        return justificacion;
    }

    /** Nombre de la regla que registro el evento, o {@code null}. */
    public String getRegla() {
        return regla;
    }

    /** Ordenes de los eventos causa (solo lectura); permite reconstruir el orden parcial. */
    public List<Integer> getCausas() {
        return causas;
    }

    /**
     * Representacion compacta para trazas, p. ej.
     * "E3 OBTENER_OBJETO actor=Perseo objeto=EscudoBronce debido a ... [regla]".
     * No es el formato final de RespuestaF1_y.txt, que lo genera la salida.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getId()).append(' ').append(accion);
        if (actor != null) sb.append(" actor=").append(actor.getId());
        if (destinatario != null) sb.append(" destinatario=").append(destinatario.getId());
        if (objeto != null) sb.append(" objeto=").append(objeto.getId());
        if (capacidad != null) sb.append(" capacidad=").append(capacidad.getId());
        if (justificacion != null) sb.append(" debido a ").append(justificacion);
        if (regla != null) sb.append(" [").append(regla).append(']');
        return sb.toString();
    }

    /**
     * Constructor por pasos (patron builder) para no tener un constructor con diez parametros.
     *
     * <p>Decision de diseno: cada tipo de accion usa campos distintos (un MATAR lleva destinatario;
     * un OBTENER_CAPACIDAD lleva capacidad y quiza objeto), asi que el builder permite rellenar solo
     * los necesarios y deja el consecuente de la regla legible. Es mutable, pero el evento que
     * produce no.</p>
     */
    public static final class Constructor {
        /** Accion obligatoria del evento. */
        private final TipoAccion accion;
        /** Actor (puede ser {@code null}). */
        private final Personaje actor;
        /** Destinatario opcional. */
        private EntidadMitologica destinatario;
        /** Objeto opcional. */
        private Objeto objeto;
        /** Capacidad opcional. */
        private Capacidad capacidad;
        /** Descripcion libre opcional. */
        private String descripcion;
        /** Justificacion opcional ("debido a ..."). */
        private String justificacion;
        /** Nombre de la regla opcional. */
        private String regla;
        /** Ordenes de los eventos causa acumulados con {@link #causas(EventoMitologico...)}. */
        private final List<Integer> causas = new ArrayList<Integer>();

        /** Privado: se obtiene con {@link EventoMitologico#nuevo(TipoAccion, Personaje)}. */
        private Constructor(TipoAccion accion, Personaje actor) {
            if (accion == null) throw new IllegalArgumentException("El evento necesita una accion");
            this.accion = accion;
            this.actor = actor;
        }

        /** Fija el personaje u objeto afectado (Medusa en "Perseo mata a Medusa"). */
        public Constructor destinatario(EntidadMitologica destinatario) {
            this.destinatario = destinatario;
            return this;
        }

        /** Fija el objeto implicado (CascoHades en "Perseo obtiene Casco de Hades"). */
        public Constructor objeto(Objeto objeto) {
            this.objeto = objeto;
            return this;
        }

        /** Fija la capacidad implicada (Invisibilidad en "Perseo obtiene Invisibilidad"). */
        public Constructor capacidad(Capacidad capacidad) {
            this.capacidad = capacidad;
            return this;
        }

        /** Fija un texto libre que la salida puede usar en lugar de la frase generada. */
        public Constructor descripcion(String descripcion) {
            this.descripcion = descripcion;
            return this;
        }

        /** Fija el motivo que se escribira tras "debido a". */
        public Constructor justificacion(String justificacion) {
            this.justificacion = justificacion;
            return this;
        }

        /** Fija el nombre de la regla (en DRL: {@code drools.getRule().getName()}). */
        public Constructor regla(String regla) {
            this.regla = regla;
            return this;
        }

        /**
         * Eventos anteriores que causan este.
         *
         * <p>Ej.: "Perseo localiza a las Grayas" tiene como causas los eventos de favor de Hermes y de
         * Atenea. Se ignoran los {@code null} para que la regla pueda pasar eventos opcionales.</p>
         */
        public Constructor causas(EventoMitologico... previos) {
            for (EventoMitologico e : previos) {
                if (e != null) causas.add(e.getOrden());
            }
            return this;
        }

        /**
         * Crea el evento con el orden dado. Visibilidad de paquete a proposito: solo
         * {@link RegistroEventos} puede crear eventos, asi ninguno queda sin registrar ni con orden falso.
         */
        EventoMitologico construir(int orden) {
            return new EventoMitologico(orden, this);
        }
    }
}

package dsi.mitologia.escenario;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import dsi.mitologia.hechos.CapacidadDisponible;
import dsi.mitologia.hechos.Cautiverio;
import dsi.mitologia.hechos.Enojo;
import dsi.mitologia.hechos.Favor;
import dsi.mitologia.hechos.Filiacion;
import dsi.mitologia.hechos.Liberacion;
import dsi.mitologia.hechos.Localizacion;
import dsi.mitologia.hechos.Muerte;
import dsi.mitologia.hechos.Posesion;
import dsi.mitologia.hechos.ProporcionaCapacidad;
import dsi.mitologia.modelo.Capacidad;
import dsi.mitologia.modelo.EntidadMitologica;
import dsi.mitologia.modelo.Objeto;
import dsi.mitologia.modelo.Personaje;
import dsi.mitologia.modelo.enums.EstadoLibertad;
import dsi.mitologia.modelo.enums.EstadoVital;
import dsi.mitologia.modelo.enums.OrigenCapacidad;

/**
 * Estado de UNA aventura: copias propias de los personajes y todos los hechos dinamicos.
 *
 * <p>Relacion con la ontologia: cada lista guarda los hechos desempaquetados de una ObjectProperty
 * del OWL ({@code tiene_objeto} -> Posesion, {@code tiene_capacidad} -> CapacidadDisponible,
 * {@code tiene_favor} -> Favor, {@code tiene_enojo} -> Enojo, {@code es_hijo_de} -> Filiacion,
 * {@code localiza_a} -> Localizacion, {@code apresa_a} -> Cautiverio, {@code mata_a} -> Muerte,
 * {@code libera_a} -> Liberacion). Las DatatypeProperties {@code estado_vital} y
 * {@code estado_libertad} viven en las copias de {@link Personaje}. Las propiedades inversas
 * ({@code esta_en_posesion_de}, {@code esta_preso_por}...) no se duplican: se leen del mismo hecho.</p>
 *
 * <p>Responsabilidades:</p>
 * <ol>
 *   <li>Preparar la memoria de trabajo inicial (estado por defecto + condiciones del escenario).</li>
 *   <li>Ofrecer operaciones de cambio de estado que comprueban la integridad
 *       (sin posesiones duplicadas, sin matar a un muerto, sin liberar a un libre...).
 *       Estas operaciones NO deciden cuando ocurre algo: eso lo deciden las reglas.</li>
 *   <li>Entregar los hechos a Drools ({@link #getHechos()}) y recoger el estado final
 *       ({@link #sincronizarDesde(Collection)}).</li>
 * </ol>
 *
 * <p>Papel en el mito: al empezar, Atenea posee EscudoBronce, Ceto tiene presa a Andromeda, etc.
 * El parser aplica las condiciones ("Atenea no tiene Escudo de Bronce" -> {@link #retirarObjeto}),
 * las reglas producen los cambios (Perseo recibe la HozAcero, mata a Medusa...) y al final el estado
 * sincronizado permite comprobar si existe {@code Liberacion(Perseo, Andromeda)}.</p>
 *
 * <p>Errores de integridad: {@link IllegalStateException}. Altas repetidas (un favor que ya
 * existe) devuelven {@code false} sin duplicar.</p>
 *
 * <p>No hay nada estatico: dos escenarios no comparten ningun objeto mutable.</p>
 *
 * <p>Decision de diseno: los personajes son copias propias (no los prototipos del dominio) porque
 * su estado vital y de libertad cambia; si se compartieran, matar a Medusa en un escenario la dejaria
 * muerta en el siguiente. Objetos y capacidades no tienen estado mutable y se toman del dominio.</p>
 */
public class EstadoAventura {

    /** Dominio del que se copio este estado; aporta objetos, capacidades y proporciona_capacidad. */
    private final DominioMitologico dominio;
    /** Copias propias de los personajes, por id; son las que se insertan en Drools. */
    private final Map<String, Personaje> personajes = new LinkedHashMap<String, Personaje>();

    /** Hechos {@code tiene_objeto}; fuente de verdad de quien posee cada objeto (uno por objeto). */
    private final List<Posesion> posesiones = new ArrayList<Posesion>();
    /** Hechos {@code tiene_capacidad}, uno por cada fuente (objeto, conjuro, montura o propia). */
    private final List<CapacidadDisponible> capacidades = new ArrayList<CapacidadDisponible>();
    /** Hechos {@code tiene_favor} (personaje, dios), p. ej. Perseo tiene el favor de Atenea. */
    private final List<Favor> favores = new ArrayList<Favor>();
    /** Hechos {@code tiene_enojo} (personaje, dios), p. ej. Casiopea tiene el enojo de Poseidon. */
    private final List<Enojo> enojos = new ArrayList<Enojo>();
    /** Hechos {@code es_hijo_de} (hijo, progenitor); estaticos, pero ligados a las copias. */
    private final List<Filiacion> filiaciones = new ArrayList<Filiacion>();
    /** Hechos {@code localiza_a} (localizador, personaje u objeto), p. ej. Perseo localiza a las Grayas. */
    private final List<Localizacion> localizaciones = new ArrayList<Localizacion>();
    /** Hechos {@code apresa_a} vigentes (captor, preso); coherentes con estado_libertad del preso. */
    private final List<Cautiverio> cautiverios = new ArrayList<Cautiverio>();
    /** Hechos {@code mata_a} (asesino, victima); las muertes sin autor solo cambian estado_vital. */
    private final List<Muerte> muertes = new ArrayList<Muerte>();
    /** Hechos {@code libera_a} (libertador, liberado); historicos, no se retiran. */
    private final List<Liberacion> liberaciones = new ArrayList<Liberacion>();

    /** Privado: los estados se crean con {@link #inicial(DominioMitologico)} o {@link #copia()}. */
    private EstadoAventura(DominioMitologico dominio) {
        if (dominio == null) throw new IllegalArgumentException("Falta el dominio");
        this.dominio = dominio;
    }

    /**
     * Estado inicial de un escenario: copia los personajes y aplica el estado por defecto del dominio.
     *
     * <p>Lo llama el constructor de {@link EscenarioMitologico}. Las relaciones por defecto se
     * aplican con las mismas operaciones que usaran el parser y las reglas, de forma que la integridad
     * (un objeto, un poseedor; favores solo de dioses) se comprueba tambien sobre los datos del OWL.</p>
     */
    public static EstadoAventura inicial(DominioMitologico dominio) {
        EstadoAventura e = new EstadoAventura(dominio);
        // Primero las copias de personajes: todos los hechos siguientes deben apuntar a ellas.
        for (Personaje prototipo : dominio.getPersonajes()) {
            e.personajes.put(prototipo.getId(), prototipo.copia());
        }
        for (String[] f : dominio.getFiliaciones()) {
            e.filiaciones.add(new Filiacion(e.getPersonaje(f[0]), e.getPersonaje(f[1])));
        }
        for (String[] p : dominio.getPosesiones()) {
            e.otorgarObjeto(e.getPersonaje(p[0]), e.getObjeto(p[1]));
        }
        for (String[] f : dominio.getFavores()) {
            e.agregarFavor(e.getPersonaje(f[0]), e.getPersonaje(f[1]));
        }
        for (String[] f : dominio.getEnojos()) {
            e.agregarEnojo(e.getPersonaje(f[0]), e.getPersonaje(f[1]));
        }
        for (String[] c : dominio.getCautiverios()) {
            // Por defecto el preso puede venir ya marcado como preso en el OWL: no se usa apresar().
            Personaje preso = e.getPersonaje(c[1]);
            preso.setEstadoLibertad(EstadoLibertad.PRESO);
            e.cautiverios.add(new Cautiverio(e.getPersonaje(c[0]), preso));
        }
        // Las capacidades propias van al final: concederCapacidad exige que el personaje este vivo.
        for (String[] c : dominio.getCapacidadesPropias()) {
            e.concederCapacidad(CapacidadDisponible.propia(e.getPersonaje(c[0]), e.getCapacidad(c[1])));
        }
        return e;
    }

    /**
     * Copia profunda independiente (para comparar alternativas desde un mismo punto).
     *
     * <p>Se duplican los personajes y se recrean todos los hechos apuntando a las nuevas copias, de
     * modo que modificar la copia (o ejecutar reglas sobre ella) no altera el original. Sirve, p. ej.,
     * para probar desde el mismo estado dos condiciones distintas y comparar sus respuestas.
     * Alternativa descartada: copia superficial de las listas; los hechos seguirian apuntando a los
     * mismos personajes y un cambio de estado vital se veria en ambos estados.</p>
     */
    public EstadoAventura copia() {
        EstadoAventura e = new EstadoAventura(dominio);
        for (Personaje p : personajes.values()) e.personajes.put(p.getId(), p.copia());
        // Posesion es mutable (cambia el poseedor): se crea un hecho nuevo, nunca se comparte.
        for (Posesion p : posesiones) {
            e.posesiones.add(new Posesion(e.getPersonaje(p.getPoseedor().getId()), p.getObjeto()));
        }
        // En el resto, e.mio(...) sustituye cada personaje por su copia en el nuevo estado.
        for (CapacidadDisponible c : capacidades) e.capacidades.add(e.recrear(c));
        for (Favor f : favores) e.favores.add(new Favor(e.mio(f.getPersonaje()), e.mio(f.getDios())));
        for (Enojo f : enojos) e.enojos.add(new Enojo(e.mio(f.getPersonaje()), e.mio(f.getDios())));
        for (Filiacion f : filiaciones) e.filiaciones.add(new Filiacion(e.mio(f.getHijo()), e.mio(f.getProgenitor())));
        for (Localizacion l : localizaciones) e.localizaciones.add(e.recrear(l));
        for (Cautiverio c : cautiverios) e.cautiverios.add(new Cautiverio(e.mio(c.getCaptor()), e.mio(c.getPreso())));
        for (Muerte m : muertes) e.muertes.add(new Muerte(e.mio(m.getAsesino()), e.mio(m.getVictima())));
        for (Liberacion l : liberaciones) e.liberaciones.add(new Liberacion(e.mio(l.getLibertador()), e.mio(l.getLiberado())));
        return e;
    }

    // =====================================================================
    // Consultas de entidades
    // =====================================================================

    /** Dominio de origen (conocimiento por defecto, compartido y de solo lectura). */
    public DominioMitologico getDominio() {
        return dominio;
    }

    /**
     * Copia del personaje propia de este escenario.
     *
     * @throws IllegalArgumentException si no existe ningun personaje con ese id
     */
    public Personaje getPersonaje(String id) {
        Personaje p = personajes.get(id);
        if (p == null) throw new IllegalArgumentException("No existe el personaje '" + id + "'");
        return p;
    }

    /** Objeto por id (se delega en el dominio porque los objetos no tienen estado propio). */
    public Objeto getObjeto(String id) {
        return dominio.getObjeto(id);
    }

    /** Capacidad por id (se delega en el dominio por el mismo motivo). */
    public Capacidad getCapacidad(String id) {
        return dominio.getCapacidad(id);
    }

    /** Copias de los personajes de este escenario (solo lectura). */
    public Collection<Personaje> getPersonajes() {
        return Collections.unmodifiableCollection(personajes.values());
    }

    // =====================================================================
    // Posesiones (fuente de verdad: los hechos Posesion)
    // =====================================================================

    /** Quien posee el objeto, o {@code null} si no lo tiene nadie. */
    public Personaje poseedorDe(Objeto objeto) {
        Posesion p = posesionDe(objeto);
        return p == null ? null : p.getPoseedor();
    }

    /** Si el personaje posee ahora el objeto ({@code tiene_objeto}). */
    public boolean posee(Personaje personaje, Objeto objeto) {
        Posesion p = posesionDe(objeto);
        return p != null && p.getPoseedor().equals(personaje);
    }

    /** Version por ids; la usa {@link Objetivo#seCumpleEn(EstadoAventura)} para OBTENER_OBJETO. */
    public boolean posee(String personajeId, String objetoId) {
        return posee(getPersonaje(personajeId), getObjeto(objetoId));
    }

    /** Objetos que posee un personaje (lista nueva; p. ej. las Ninfas -> SandaliasAladas, ZurronMagico). */
    public List<Objeto> objetosDe(Personaje personaje) {
        List<Objeto> lista = new ArrayList<Objeto>();
        for (Posesion p : posesiones) {
            if (p.getPoseedor().equals(personaje)) lista.add(p.getObjeto());
        }
        return lista;
    }

    /**
     * Da un objeto que no tiene nadie (estado inicial, objeto que aparece, objeto creado).
     *
     * <p>Integridad: un objeto solo tiene un poseedor; si ya lo tiene alguien hay que usar
     * {@link #transferirObjeto}. Asi se evita que Atenea y Perseo tengan a la vez el EscudoBronce.</p>
     *
     * @throws IllegalStateException si el objeto ya tiene poseedor
     */
    public Posesion otorgarObjeto(Personaje receptor, Objeto objeto) {
        Personaje actual = poseedorDe(objeto);
        if (actual != null) {
            throw new IllegalStateException(objeto.getId() + " ya lo posee " + actual.getId());
        }
        Posesion p = new Posesion(mio(receptor), objeto);
        posesiones.add(p);
        return p;
    }

    /**
     * Pasa el objeto de {@code origen} a {@code destino} modificando el MISMO hecho Posesion.
     * El origen pierde las capacidades que le daba ese objeto.
     *
     * <p>Ej.: Hermes da la HozAcero a Perseo; Perseo roba el OjoGrayas a las Grayas. Se modifica el
     * hecho en lugar de borrar y crear otro para que, dentro de Drools, baste un {@code modify} y se
     * conserve la identidad del hecho (un objeto, un hecho Posesion).</p>
     *
     * @throws IllegalStateException si el origen no posee el objeto, si origen y destino coinciden o
     *                               si el destino esta muerto
     */
    public Posesion transferirObjeto(Personaje origen, Personaje destino, Objeto objeto) {
        Posesion p = posesionDe(objeto);
        if (p == null || !p.getPoseedor().equals(origen)) {
            throw new IllegalStateException(origen.getId() + " no posee " + objeto.getId());
        }
        if (origen.equals(destino)) {
            throw new IllegalStateException(origen.getId() + " ya posee " + objeto.getId());
        }
        exigirVivo(destino, "recibir " + objeto.getId());
        p.setPoseedor(mio(destino));
        // Las capacidades dependen de la posesion: si el origen ya no tiene el objeto, no las conserva.
        retirarCapacidadesPorFuente(origen, objeto);
        return p;
    }

    /**
     * El poseedor deja de tener el objeto y nadie lo recibe ("Atenea no tiene Escudo de Bronce").
     *
     * <p>Es la operacion que usa el parser para las condiciones negativas que anulan el conocimiento
     * por defecto. Tambien retira las capacidades que daba el objeto.</p>
     *
     * @throws IllegalStateException si el personaje no posee el objeto
     */
    public void retirarObjeto(Personaje poseedor, Objeto objeto) {
        Posesion p = posesionDe(objeto);
        if (p == null || !p.getPoseedor().equals(poseedor)) {
            throw new IllegalStateException(poseedor.getId() + " no posee " + objeto.getId());
        }
        posesiones.remove(p);
        retirarCapacidadesPorFuente(poseedor, objeto);
    }

    /**
     * Sustituye un objeto por otro (Hefesto pule el EscudoBronce y queda EscudoEspejo).
     *
     * <p>Se modela como retirar + otorgar porque son individuos distintos en el OWL (solo
     * EscudoEspejo proporciona Reflejo). El resultado no debe tenerlo nadie todavia.</p>
     *
     * @throws IllegalStateException si el resultado ya existe en posesion de alguien o el poseedor
     *                               no tiene el original
     */
    public Posesion transformarObjeto(Personaje poseedor, Objeto original, Objeto resultado) {
        if (poseedorDe(resultado) != null) {
            throw new IllegalStateException(resultado.getId() + " ya existe en posesion de alguien");
        }
        retirarObjeto(poseedor, original);
        return otorgarObjeto(poseedor, resultado);
    }

    // =====================================================================
    // Capacidades
    // =====================================================================

    /** Consulta del conocimiento estatico {@code proporciona_capacidad} (p. ej. CascoHades -> Invisibilidad). */
    public boolean proporciona(Objeto objeto, Capacidad capacidad) {
        return dominio.getProporciona().contains(new ProporcionaCapacidad(objeto, capacidad));
    }

    /**
     * Registra que un personaje dispone de una capacidad por una fuente concreta.
     * Comprueba que la fuente es valida en ese momento (posee el objeto y el objeto da esa capacidad,
     * o tiene capturada la montura viva). Devuelve {@code false} si ya existia.
     *
     * <p>Decision de diseno: la comprobacion no decide que Perseo deba tener Vuelo; solo impide un
     * estado incoherente (Vuelo sin SandaliasAladas). La decision de conceder la toma la regla.</p>
     *
     * @throws IllegalStateException si el personaje esta muerto o la fuente no es valida
     */
    public boolean concederCapacidad(CapacidadDisponible c) {
        Personaje p = mio(c.getPersonaje());
        exigirVivo(p, "obtener " + c.getCapacidad().getId());
        OrigenCapacidad origen = c.getOrigen();
        if (origen == OrigenCapacidad.OBJETO || origen == OrigenCapacidad.CONJURO) {
            Objeto o = (Objeto) c.getFuente();
            if (!posee(p, o)) {
                throw new IllegalStateException(p.getId() + " no posee " + o.getId());
            }
            if (!proporciona(o, c.getCapacidad())) {
                throw new IllegalStateException(o.getId() + " no proporciona " + c.getCapacidad().getId());
            }
        } else if (origen == OrigenCapacidad.MONTURA) {
            // La montura (p. ej. Pegaso) debe estar presa por el jinete y viva.
            Personaje montura = mio((Personaje) c.getFuente());
            if (!tienePreso(p, montura) || montura.getEstadoVital() != EstadoVital.VIVO) {
                throw new IllegalStateException(p.getId() + " no tiene capturada a " + montura.getId());
            }
        }
        // PROPIA no necesita comprobacion de fuente.
        if (capacidades.contains(c)) return false;
        // Se guarda una version recreada para que apunte a la copia de personaje de este estado.
        capacidades.add(recrear(c));
        return true;
    }

    /** Retira un hecho de capacidad concreto (una fuente); devuelve si existia. */
    public boolean retirarCapacidad(CapacidadDisponible c) {
        return capacidades.remove(c);
    }

    /** Dispone de la capacidad por cualquier fuente? */
    public boolean tieneCapacidad(Personaje personaje, Capacidad capacidad) {
        return !fuentesDe(personaje, capacidad).isEmpty();
    }

    /** Version por ids; la usa {@link Objetivo#seCumpleEn(EstadoAventura)} para OBTENER_CAPACIDAD. */
    public boolean tieneCapacidad(String personajeId, String capacidadId) {
        return tieneCapacidad(getPersonaje(personajeId), getCapacidad(capacidadId));
    }

    /** Todas las fuentes por las que el personaje dispone de la capacidad (puede haber varias). */
    public List<CapacidadDisponible> fuentesDe(Personaje personaje, Capacidad capacidad) {
        List<CapacidadDisponible> lista = new ArrayList<CapacidadDisponible>();
        for (CapacidadDisponible c : capacidades) {
            if (c.getPersonaje().equals(personaje) && c.getCapacidad().equals(capacidad)) lista.add(c);
        }
        return lista;
    }

    // =====================================================================
    // Vida y muerte
    // =====================================================================

    /**
     * {@code asesino} mata a {@code victima}. El asesino puede ser {@code null} (muerte sin autor).
     *
     * <p>Pone {@code estado_vital} a MUERTO y, si hay asesino, registra el hecho {@code mata_a}
     * (Perseo mata a Medusa). Si la victima era montura de alguien, ese jinete pierde la capacidad.</p>
     *
     * @throws IllegalStateException si la victima ya estaba muerta, el asesino esta muerto o
     *                               coinciden asesino y victima
     */
    public void matar(Personaje asesino, Personaje victima) {
        Personaje v = mio(victima);
        if (v.getEstadoVital() == EstadoVital.MUERTO) {
            throw new IllegalStateException(v.getId() + " ya esta muerto");
        }
        if (asesino != null) {
            exigirVivo(asesino, "matar");
            if (asesino.equals(v)) throw new IllegalStateException("Un personaje no se mata a si mismo con matar()");
        }
        v.setEstadoVital(EstadoVital.MUERTO);
        if (asesino != null) muertes.add(new Muerte(mio(asesino), v));
        retirarCapacidadesDeMontura(v);
    }

    /** Muerte sin autor (p. ej. Icaro al caer, o el heroe que toca la cabeza de Medusa sin zurron). */
    public void morir(Personaje personaje) {
        matar(null, personaje);
    }

    /** Si existe el hecho {@code mata_a}; lo usa {@link Objetivo#seCumpleEn(EstadoAventura)} para MATAR. */
    public boolean haMatado(String asesinoId, String victimaId) {
        return muertes.contains(new Muerte(getPersonaje(asesinoId), getPersonaje(victimaId)));
    }

    // =====================================================================
    // Cautiverio y liberacion (Cautiverio sincronizado con estadoLibertad)
    // =====================================================================

    /**
     * {@code captor} apresa a {@code preso}: crea {@code apresa_a} y pone {@code estado_libertad} a
     * PRESO. Ambos deben estar vivos y el preso libre (no se apresa dos veces al mismo personaje).
     *
     * @throws IllegalStateException si se viola alguna de esas condiciones
     */
    public Cautiverio apresar(Personaje captor, Personaje preso) {
        Personaje c = mio(captor);
        Personaje p = mio(preso);
        exigirVivo(c, "apresar");
        exigirVivo(p, "ser apresado");
        if (c.equals(p)) throw new IllegalStateException("Nadie se apresa a si mismo");
        if (p.getEstadoLibertad() == EstadoLibertad.PRESO) {
            throw new IllegalStateException(p.getId() + " ya esta preso");
        }
        p.setEstadoLibertad(EstadoLibertad.PRESO);
        Cautiverio cautiverio = new Cautiverio(c, p);
        cautiverios.add(cautiverio);
        return cautiverio;
    }

    /**
     * Libera a un preso: retira todos sus Cautiverio, lo deja LIBRE y registra la Liberacion.
     *
     * <p>Es el paso final del mito (Perseo libera a Andromeda tras petrificar a Ceto). La Liberacion
     * es historica: queda como prueba de que el objetivo LIBERAR se cumplio. Si el liberado era una
     * montura, su antiguo captor deja de poder montarla.</p>
     *
     * @throws IllegalStateException si el libertador esta muerto o el personaje ya estaba libre
     */
    public Liberacion liberar(Personaje libertador, Personaje preso) {
        Personaje l = mio(libertador);
        Personaje p = mio(preso);
        exigirVivo(l, "liberar");
        if (p.getEstadoLibertad() != EstadoLibertad.PRESO) {
            throw new IllegalStateException(p.getId() + " ya esta libre");
        }
        // Se quitan todos los cautiverios del preso, no solo el de un captor concreto.
        for (Iterator<Cautiverio> it = cautiverios.iterator(); it.hasNext();) {
            if (it.next().getPreso().equals(p)) it.remove();
        }
        p.setEstadoLibertad(EstadoLibertad.LIBRE);
        retirarCapacidadesDeMontura(p);
        Liberacion liberacion = new Liberacion(l, p);
        if (!liberaciones.contains(liberacion)) liberaciones.add(liberacion);
        return liberacion;
    }

    /**
     * Deshace un cautiverio del conocimiento por defecto SIN registrar una liberacion.
     *
     * <p>Solo para preparar el estado inicial de un escenario (procesado de datos, no inferencia): el
     * OWL describe a Andromeda ya presa de Ceto, que es el resultado del mito, mientras que el enunciado
     * espera que el sistema deduzca esa captura. A diferencia de {@link #liberar}, no anade ninguna
     * {@link Liberacion}, porque nadie ha liberado a nadie: simplemente ese hecho no forma parte del
     * punto de partida.</p>
     *
     * @return {@code true} si existia el cautiverio
     */
    public boolean retirarCautiverio(Personaje captor, Personaje preso) {
        Personaje p = mio(preso);
        boolean estaba = cautiverios.remove(new Cautiverio(mio(captor), p));
        if (estaba && captorDe(p) == null) p.setEstadoLibertad(EstadoLibertad.LIBRE);
        return estaba;
    }

    /** Captor actual del preso, o {@code null}. */
    public Personaje captorDe(Personaje preso) {
        for (Cautiverio c : cautiverios) {
            if (c.getPreso().equals(preso)) return c.getCaptor();
        }
        return null;
    }

    /** Si existe el hecho {@code apresa_a} vigente (captor, preso). */
    public boolean tienePreso(Personaje captor, Personaje preso) {
        return cautiverios.contains(new Cautiverio(captor, preso));
    }

    /** Version por ids; la usa {@link Objetivo#seCumpleEn(EstadoAventura)} para APRESAR. */
    public boolean tienePreso(String captorId, String presoId) {
        return tienePreso(getPersonaje(captorId), getPersonaje(presoId));
    }

    /** Si existe el hecho {@code libera_a}; lo usa {@link Objetivo#seCumpleEn(EstadoAventura)} para LIBERAR. */
    public boolean haLiberado(String libertadorId, String liberadoId) {
        return liberaciones.contains(new Liberacion(getPersonaje(libertadorId), getPersonaje(liberadoId)));
    }

    // =====================================================================
    // Localizacion, favores y enojos
    // =====================================================================

    /** Registra {@code localiza_a} sobre un personaje (Perseo localiza a las Ninfas); false si ya existia. */
    public boolean registrarLocalizacion(Personaje localizador, Personaje localizado) {
        return anadirSiFalta(localizaciones, new Localizacion(mio(localizador), mio(localizado)));
    }

    /** Registra {@code localiza_a} sobre un objeto; false si ya existia. */
    public boolean registrarLocalizacion(Personaje localizador, Objeto localizado) {
        return anadirSiFalta(localizaciones, new Localizacion(mio(localizador), localizado));
    }

    /**
     * Si existe {@code localiza_a} (localizador, entidad); lo usa {@link Objetivo#seCumpleEn} para
     * LOCALIZAR. Se compara por id porque la entidad localizada puede ser personaje u objeto.
     */
    public boolean haLocalizado(String localizadorId, String entidadId) {
        for (Localizacion l : localizaciones) {
            if (l.getLocalizador().getId().equals(localizadorId) && l.getLocalizado().getId().equals(entidadId)) {
                return true;
            }
        }
        return false;
    }

    /** El rango OWL de tiene_favor es Dios: se rechaza un "dios" que no lo sea. */
    public boolean agregarFavor(Personaje personaje, Personaje dios) {
        exigirDios(dios);
        return anadirSiFalta(favores, new Favor(mio(personaje), mio(dios)));
    }

    /** Quita un favor (condicion negativa, p. ej. "Perseo no tiene el favor de Hermes"); devuelve si existia. */
    public boolean retirarFavor(Personaje personaje, Personaje dios) {
        return favores.remove(new Favor(personaje, dios));
    }

    /** Si existe {@code tiene_favor} (personaje, dios). */
    public boolean tieneFavor(Personaje personaje, Personaje dios) {
        return favores.contains(new Favor(personaje, dios));
    }

    /** Anade {@code tiene_enojo}; igual que el favor, el rango OWL exige que sea un Dios. */
    public boolean agregarEnojo(Personaje personaje, Personaje dios) {
        exigirDios(dios);
        return anadirSiFalta(enojos, new Enojo(mio(personaje), mio(dios)));
    }

    /** Quita un enojo (p. ej. "Casiopea no tiene el enojo de Poseidon"); devuelve si existia. */
    public boolean retirarEnojo(Personaje personaje, Personaje dios) {
        return enojos.remove(new Enojo(personaje, dios));
    }

    /** Si existe {@code tiene_enojo} (personaje, dios). */
    public boolean tieneEnojo(Personaje personaje, Personaje dios) {
        return enojos.contains(new Enojo(personaje, dios));
    }

    // =====================================================================
    // Acceso de solo lectura a los hechos
    // =====================================================================
    // Se devuelven vistas no modificables: los cambios solo se hacen con las operaciones de arriba,
    // que garantizan la integridad.

    /** Hechos Posesion actuales. */
    public List<Posesion> getPosesiones() {
        return Collections.unmodifiableList(posesiones);
    }

    /** Hechos CapacidadDisponible actuales. */
    public List<CapacidadDisponible> getCapacidades() {
        return Collections.unmodifiableList(capacidades);
    }

    /** Hechos Favor actuales. */
    public List<Favor> getFavores() {
        return Collections.unmodifiableList(favores);
    }

    /** Hechos Enojo actuales. */
    public List<Enojo> getEnojos() {
        return Collections.unmodifiableList(enojos);
    }

    /** Hechos Filiacion. */
    public List<Filiacion> getFiliaciones() {
        return Collections.unmodifiableList(filiaciones);
    }

    /** Hechos Localizacion. */
    public List<Localizacion> getLocalizaciones() {
        return Collections.unmodifiableList(localizaciones);
    }

    /** Hechos Cautiverio vigentes. */
    public List<Cautiverio> getCautiverios() {
        return Collections.unmodifiableList(cautiverios);
    }

    /** Hechos Muerte (con autor). */
    public List<Muerte> getMuertes() {
        return Collections.unmodifiableList(muertes);
    }

    /** Hechos Liberacion. */
    public List<Liberacion> getLiberaciones() {
        return Collections.unmodifiableList(liberaciones);
    }

    // =====================================================================
    // Puente con Drools (sin depender de la API de Drools)
    // =====================================================================
    // Decision de diseno: esta clase no importa nada de org.kie; solo MotorReglas conoce Drools.
    // Asi el estado se puede probar sin motor y el cambio de motor no afectaria al modelo.

    /**
     * Todos los hechos que hay que insertar en una KieSession nueva: personajes, objetos,
     * capacidades, conocimiento estatico y hechos dinamicos de este escenario.
     *
     * <p>Los hechos van desempaquetados (un objeto Java por relacion), como pide el profesor, para
     * que los patrones de las reglas sean simples. Devuelve una lista nueva en cada llamada.</p>
     */
    public List<Object> getHechos() {
        List<Object> hechos = new ArrayList<Object>();
        hechos.addAll(personajes.values());
        hechos.addAll(dominio.getObjetos());
        hechos.addAll(dominio.getCapacidades());
        hechos.addAll(dominio.getProporciona());
        hechos.addAll(filiaciones);
        hechos.addAll(posesiones);
        hechos.addAll(capacidades);
        hechos.addAll(favores);
        hechos.addAll(enojos);
        hechos.addAll(localizaciones);
        hechos.addAll(cautiverios);
        hechos.addAll(muertes);
        hechos.addAll(liberaciones);
        return hechos;
    }

    /**
     * Recoge el estado final desde la memoria de trabajo (p. ej. {@code kieSession.getObjects()}).
     * Las reglas habran insertado, modificado o retirado hechos; aqui solo se copian.
     *
     * <p>Se vacian las listas dinamicas y se rellenan con lo que quede en la sesion, de modo que un
     * hecho retirado por una regla (p. ej. la Posesion de las Grayas) desaparece tambien aqui. Las
     * filiaciones no se vacian porque son estaticas. Los objetos de otras clases se ignoran.</p>
     */
    public void sincronizarDesde(Collection<?> hechosSesion) {
        posesiones.clear();
        capacidades.clear();
        favores.clear();
        enojos.clear();
        localizaciones.clear();
        cautiverios.clear();
        muertes.clear();
        liberaciones.clear();
        for (Object h : hechosSesion) {
            if (h instanceof Personaje) personajes.put(((Personaje) h).getId(), (Personaje) h);
            else if (h instanceof Posesion) posesiones.add((Posesion) h);
            else if (h instanceof CapacidadDisponible) capacidades.add((CapacidadDisponible) h);
            else if (h instanceof Favor) favores.add((Favor) h);
            else if (h instanceof Enojo) enojos.add((Enojo) h);
            else if (h instanceof Localizacion) localizaciones.add((Localizacion) h);
            else if (h instanceof Cautiverio) cautiverios.add((Cautiverio) h);
            else if (h instanceof Muerte) muertes.add((Muerte) h);
            else if (h instanceof Liberacion) liberaciones.add((Liberacion) h);
        }
    }

    // =====================================================================
    // Auxiliares privados
    // =====================================================================

    /** Hecho Posesion del objeto o {@code null}; como hay a lo sumo uno por objeto, basta el primero. */
    private Posesion posesionDe(Objeto objeto) {
        for (Posesion p : posesiones) {
            if (p.getObjeto().equals(objeto)) return p;
        }
        return null;
    }

    /** Devuelve la copia de este escenario (evita mezclar personajes de otros escenarios). */
    private Personaje mio(Personaje p) {
        // Personaje compara por id, asi que el prototipo o la copia de otro estado serian "iguales";
        // por eso se resuelve siempre a la instancia propia antes de guardarla o modificarla.
        return getPersonaje(p.getId());
    }

    /** Quita las capacidades que el personaje tenia gracias a ese objeto (al perderlo o transferirlo). */
    private void retirarCapacidadesPorFuente(Personaje personaje, Objeto objeto) {
        for (Iterator<CapacidadDisponible> it = capacidades.iterator(); it.hasNext();) {
            CapacidadDisponible c = it.next();
            if (c.getPersonaje().equals(personaje) && objeto.equals(c.getFuente())) it.remove();
        }
    }

    /** Quita las capacidades de cualquier jinete cuya montura ha muerto o ha quedado libre. */
    private void retirarCapacidadesDeMontura(Personaje montura) {
        for (Iterator<CapacidadDisponible> it = capacidades.iterator(); it.hasNext();) {
            CapacidadDisponible c = it.next();
            if (c.getOrigen() == OrigenCapacidad.MONTURA && montura.equals(c.getFuente())) it.remove();
        }
    }

    /** Recrea una CapacidadDisponible apuntando a las copias de personaje de este estado. */
    private CapacidadDisponible recrear(CapacidadDisponible c) {
        Personaje p = mio(c.getPersonaje());
        switch (c.getOrigen()) {
            case PROPIA:
                return CapacidadDisponible.propia(p, c.getCapacidad());
            case OBJETO:
                return CapacidadDisponible.deObjeto(p, c.getCapacidad(), (Objeto) c.getFuente());
            case CONJURO:
                return CapacidadDisponible.deConjuro(p, c.getCapacidad(), (Objeto) c.getFuente());
            default:
                // MONTURA: la fuente es un personaje y tambien hay que resolverla a la copia propia.
                return CapacidadDisponible.deMontura(p, c.getCapacidad(), mio((Personaje) c.getFuente()));
        }
    }

    /** Recrea una Localizacion con las copias propias (el objeto localizado se comparte tal cual). */
    private Localizacion recrear(Localizacion l) {
        EntidadMitologica e = l.getLocalizado();
        return e instanceof Personaje
                ? new Localizacion(mio(l.getLocalizador()), mio((Personaje) e))
                : new Localizacion(mio(l.getLocalizador()), (Objeto) e);
    }

    /** Integridad: un muerto no puede actuar ni recibir (se mira la copia propia, no el argumento). */
    private void exigirVivo(Personaje p, String accion) {
        if (mio(p).getEstadoVital() != EstadoVital.VIVO) {
            throw new IllegalStateException(p.getId() + " esta muerto y no puede " + accion);
        }
    }

    /** Integridad con la ontologia: el rango de tiene_favor y tiene_enojo es la clase Dios. */
    private void exigirDios(Personaje dios) {
        if (!mio(dios).isDios()) {
            throw new IllegalStateException(dios.getId() + " no es un Dios (rango de tiene_favor/tiene_enojo)");
        }
    }

    /** Anade el hecho si no estaba (igualdad por valor de los hechos); devuelve si se anadio. */
    private static <T> boolean anadirSiFalta(List<T> lista, T hecho) {
        if (lista.contains(hecho)) return false;
        lista.add(hecho);
        return true;
    }
}

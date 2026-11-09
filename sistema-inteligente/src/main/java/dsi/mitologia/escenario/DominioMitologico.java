package dsi.mitologia.escenario;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import dsi.mitologia.hechos.ProporcionaCapacidad;
import dsi.mitologia.modelo.Capacidad;
import dsi.mitologia.modelo.Objeto;
import dsi.mitologia.modelo.Personaje;

/**
 * Conocimiento del dominio: que personajes, objetos y capacidades existen, que objeto da que
 * capacidad, el parentesco y el estado POR DEFECTO con el que empieza cualquier escenario
 * (quien posee que, favores, enojos, cautiverios).
 *
 * Relacion con la ontologia: es la version Java de mitologia.owl. Los individuos de las clases
 * Personaje (Dios, Heroe, Semidios, Mortal, Criatura), Objeto (Arma, Proteccion, ArtefactoMagico,
 * ParteAnatomica) y Capacidad se registran como entidades; las ObjectProperties se guardan como
 * relaciones por defecto: {proporciona_capacidad}, {es_hijo_de}, {tiene_objeto},
 * {tiene_favor}, {tiene_enojo}, {apresa_a} y {tiene_capacidad} (propias).
 *
 * Papel en el mito: aqui está LO QUE SE SABE antes de leer el escenario, por ejemplo, que Atenea
 * tiene el EscudoBronce, Hermes la HozAcero, Hades el CascoHades, las Grayas el OjoGrayas, que el
 * CascoHades proporciona Invisibilidad o que Perseo es hijo de Zeus y Danae. 
 *
 * Se rellena una vez (desde mitologia.owl) y no lo
 * modifica ninguna ejecucion: cada escenario crea su propio {EstadoAventura} copiando los
 * personajes. Los personajes guardados aqui son prototipos y nunca se insertan en Drools.
 *
 * Las relaciones por defecto se guardan como pares de identificadores para que cada
 * escenario construya hechos nuevos con sus propias copias.
 *
 * Decision de diseno: separar dominio (prototipos que no cambian) de estado (copias por
 * escenario); por ejemplo, si en uno Perseo mata a Medusa,
 * en el siguiente Medusa vuelve a empezar viva. 
 */
public class DominioMitologico {
   
    private final Map<String, Personaje> personajes = new LinkedHashMap<String, Personaje>();
    private final Map<String, Objeto> objetos = new LinkedHashMap<String, Objeto>();
    private final Map<String, Capacidad> capacidades = new LinkedHashMap<String, Capacidad>();
    private final List<ProporcionaCapacidad> proporciona = new ArrayList<ProporcionaCapacidad>();

    /** Pares {hijo, progenitor} de {es_hijo_de} (por ejemplo {Perseo, Zeus}). */
    private final List<String[]> filiaciones = new ArrayList<String[]>();
    /** Pares {poseedor, objeto} de {tiene_objeto} por defecto (por ejemplo {Atenea, EscudoBronce}). */
    private final List<String[]> posesiones = new ArrayList<String[]>();
    /** Pares {personaje, dios} de {tiene_favor} por defecto. */
    private final List<String[]> favores = new ArrayList<String[]>();
    /** Pares {personaje, dios} de {tiene_enojo} por defecto (por ejemplo {Casiopea, Poseidon}). */
    private final List<String[]> enojos = new ArrayList<String[]>();
    /** Pares {captor, preso} de {apresa_a} por defecto (por ejemplo {Ceto, Andromeda}). */
    private final List<String[]> cautiverios = new ArrayList<String[]>();
    /** Pares {personaje, capacidad} de {tiene_capacidad} innatas (sin objeto que las de). */
    private final List<String[]> capacidadesPropias = new ArrayList<String[]>();

    // ---- Entidades ----

    public Personaje registrarPersonaje(Personaje p) {
        comprobarIdLibre(p.getId());
        personajes.put(p.getId(), p);
        return p;
    }

    public Objeto registrarObjeto(Objeto o) {
        comprobarIdLibre(o.getId());
        objetos.put(o.getId(), o);
        return o;
    }

    public Capacidad registrarCapacidad(Capacidad c) {
        comprobarIdLibre(c.getId());
        capacidades.put(c.getId(), c);
        return c;
    }

    // ---- Conocimiento estatico ----

    /**
     * Registra que un objeto proporciona una capacidad ({proporciona_capacidad}), por ejemplo
     * EscudoEspejo -> Reflejo. Es conocimiento fijo: no cambia en ningun escenario y las reglas lo
     * usan para deducir capacidades a partir de posesiones. Ignora duplicados.
     */
    public void registrarProporciona(String objetoId, String capacidadId) {
        ProporcionaCapacidad pc = new ProporcionaCapacidad(getObjeto(objetoId), getCapacidad(capacidadId));
        if (!proporciona.contains(pc)) proporciona.add(pc);
    }

    /**
     * Registra {es_hijo_de} (hijo, progenitor). La inversa {tiene_hijo} no se guarda
     * aparte: se obtiene del mismo par. Valida que ambos personajes existan.
     */
    public void registrarFiliacion(String hijoId, String progenitorId) {
        anadirPar(filiaciones, getPersonaje(hijoId).getId(), getPersonaje(progenitorId).getId());
    }

    // ---- Estado por defecto ----

    /**
     * Registra la posesion por defecto de un objeto ({tiene_objeto}).
     *
     * Integridad: un objeto solo puede tener un poseedor. Se comprueba ya al cargar para detectar
     * errores en la ontologia antes de ejecutar ningun escenario.
     *
     * @throws IllegalStateException si el objeto ya pertenece por defecto a otro personaje
     */
    public void registrarPosesion(String personajeId, String objetoId) {
        // Llamadas solo para validar que existen (lanzan excepcion si no).
        getPersonaje(personajeId);
        getObjeto(objetoId);
        for (String[] par : posesiones) {
            if (par[1].equals(objetoId) && !par[0].equals(personajeId)) {
                throw new IllegalStateException(objetoId + " ya pertenece por defecto a " + par[0]);
            }
        }
        anadirPar(posesiones, personajeId, objetoId);
    }

    /** Registra {tiene_favor} por defecto (personaje, dios). El rango Dios se valida en el estado. */
    public void registrarFavor(String personajeId, String diosId) {
        anadirPar(favores, getPersonaje(personajeId).getId(), getPersonaje(diosId).getId());
    }

    /** Registra {tiene_enojo} por defecto (personaje, dios). El rango Dios se valida en el estado. */
    public void registrarEnojo(String personajeId, String diosId) {
        anadirPar(enojos, getPersonaje(personajeId).getId(), getPersonaje(diosId).getId());
    }

    /** Registra {apresa_a} por defecto (captor, preso). */
    public void registrarCautiverio(String captorId, String presoId) {
        anadirPar(cautiverios, getPersonaje(captorId).getId(), getPersonaje(presoId).getId());
    }

    /**
     * Registra una capacidad innata ({tiene_capacidad} sin objeto que la proporcione). En el
     * mito de Perseo todas las capacidades vienen de objetos (la Petrificacion la da CabezaMedusa),
     * pero se deja preparado para otros mitos (por ejemplo la fuerza de Heracles).
     */
    public void registrarCapacidadPropia(String personajeId, String capacidadId) {
        anadirPar(capacidadesPropias, getPersonaje(personajeId).getId(), getCapacidad(capacidadId).getId());
    }

    // ---- Getters ----
    public Personaje getPersonaje(String id) {
        return buscar(personajes, id, "personaje");
    }
    public Objeto getObjeto(String id) {
        return buscar(objetos, id, "objeto");
    }

    
    public Capacidad getCapacidad(String id) {
        return buscar(capacidades, id, "capacidad");
    }

    /** Si existe el personaje; lo usa el parser para reconocer nombres sin capturar excepciones. */
    public boolean existePersonaje(String id) {
        return personajes.containsKey(id);
    }

    /** Si existe el objeto; lo usa el parser para distinguir "X tiene Objeto" de "X tiene el favor de Y". */
    public boolean existeObjeto(String id) {
        return objetos.containsKey(id);
    }

    /** Si existe la capacidad; lo usa el parser al leer preguntas de tipo "conseguir capacidad". */
    public boolean existeCapacidad(String id) {
        return capacidades.containsKey(id);
    }

    /** Prototipos (no modificar ni insertar en Drools: usar las copias de EstadoAventura). */
    public Collection<Personaje> getPersonajes() {
        return Collections.unmodifiableCollection(personajes.values());
    }

    /** Todos los objetos (solo lectura); se insertan tal cual en cada KieSession. */
    public Collection<Objeto> getObjetos() {
        return Collections.unmodifiableCollection(objetos.values());
    }

    /** Todas las capacidades (solo lectura); se insertan tal cual en cada KieSession. */
    public Collection<Capacidad> getCapacidades() {
        return Collections.unmodifiableCollection(capacidades.values());
    }

    /** Hechos {proporciona_capacidad} (solo lectura); inmutables, se comparten entre escenarios. */
    public List<ProporcionaCapacidad> getProporciona() {
        return Collections.unmodifiableList(proporciona);
    }

    // Decision de diseno: los getters de pares tienen visibilidad de paquete porque solo los necesita
    // EstadoAventura.inicial(); asi nadie de fuera puede alterar el estado por defecto.

    /** Pares {hijo, progenitor} por defecto. */
    List<String[]> getFiliaciones() {
        return filiaciones;
    }

    /** Pares {poseedor, objeto} por defecto. */
    List<String[]> getPosesiones() {
        return posesiones;
    }

    /** Pares {personaje, dios} de favor por defecto. */
    List<String[]> getFavores() {
        return favores;
    }

    /** Pares {personaje, dios} de enojo por defecto. */
    List<String[]> getEnojos() {
        return enojos;
    }

    /** Pares {captor, preso} por defecto. */
    List<String[]> getCautiverios() {
        return cautiverios;
    }

    /** Pares {personaje, capacidad} de capacidades propias por defecto. */
    List<String[]> getCapacidadesPropias() {
        return capacidadesPropias;
    }

    // ---- Auxiliares ----

    /**
     * Los ids son unicos en todo el dominio (no solo dentro de su tipo), igual que los nombres de
     * individuos en el OWL; asi el parser puede resolver un nombre sin saber de antemano su clase.
     */
    private void comprobarIdLibre(String id) {
        if (personajes.containsKey(id) || objetos.containsKey(id) || capacidades.containsKey(id)) {
            throw new IllegalStateException("Identificador repetido: " + id);
        }
    }

    /** Anade el par {a, b} si no estaba (los arrays no tienen equals por valor, por eso se recorre). */
    private static void anadirPar(List<String[]> lista, String a, String b) {
        for (String[] par : lista) {
            if (par[0].equals(a) && par[1].equals(b)) return;
        }
        lista.add(new String[] {a, b});
    }

    /** Busca por id y lanza un error legible ("No existe el objeto 'X'") si falta. */
    private static <T> T buscar(Map<String, T> mapa, String id, String que) {
        T t = mapa.get(id);
        if (t == null) throw new IllegalArgumentException("No existe el " + que + " '" + id + "'");
        return t;
    }
}

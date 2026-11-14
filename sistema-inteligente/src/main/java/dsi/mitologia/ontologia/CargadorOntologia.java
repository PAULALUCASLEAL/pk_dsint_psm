package dsi.mitologia.ontologia;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import dsi.mitologia.escenario.DominioMitologico;
import dsi.mitologia.modelo.Capacidad;
import dsi.mitologia.modelo.Objeto;
import dsi.mitologia.modelo.Personaje;
import dsi.mitologia.modelo.enums.CategoriaObjeto;
import dsi.mitologia.modelo.enums.EstadoLibertad;
import dsi.mitologia.modelo.enums.EstadoVital;
import dsi.mitologia.modelo.enums.TipoPersonaje;

/**
 * Lee los INDIVIDUOS de {@code ontologia/mitologia.owl} (fase 1.a, RDF/XML) y construye un
 * {@link DominioMitologico}: el conocimiento por defecto del mito del que parte cada escenario.
 *
 * <p>Es procesamiento de datos de entrada, no razonamiento (nota O del enunciado): no aplica dominios,
 * rangos, inversas ni restricciones {@code allValuesFrom} (eso lo haria un razonador como HermiT). Solo
 * traduce los asertos que estan escritos en el fichero. La inferencia del mito queda para Drools.</p>
 *
 * <p>Decision de diseno: se usa el DOM estandar de Java 8 ({@code javax.xml.parsers}) en lugar de la
 * OWL API o Jena para no anadir dependencias al proyecto Maven. Alternativa descartada: OWL API, que
 * resolveria la semantica completa pero arrastra muchas librerias para leer unas pocas decenas de
 * individuos. El fichero se abre solo para lectura y nunca se reescribe: la ontologia sigue siendo
 * editable con Protege sin que el sistema la altere.</p>
 *
 * <p>Admite las serializaciones de Protege 5 ({@code owl:NamedIndividual} / {@code rdf:Description}
 * con {@code rdf:type}) y la de Protege 3 (elemento con el nombre de la clase), porque la ontologia se
 * creo con Protege 3.4.8 y se edito despues con 5.6.9.</p>
 *
 * <p>Traduccion de clases OWL a Java:</p>
 * <ul>
 *   <li>{@code Dios}, {@code Heroe}, {@code Semidios}, {@code Mortal}, {@code Criatura} (subclases de
 *       {@code Personaje}) -&gt; {@link Personaje} con el {@link TipoPersonaje} correspondiente. Un
 *       individuo puede tener varias (Perseo es {@code Heroe} y {@code Semidios}).</li>
 *   <li>{@code Arma}, {@code Proteccion}, {@code ArtefactoMagico}, {@code ParteAnatomica} (subclases de
 *       {@code Objeto}) -&gt; {@link Objeto} con su {@link CategoriaObjeto}.</li>
 *   <li>{@code Capacidad} -&gt; {@link Capacidad}.</li>
 *   <li>DatatypeProperties {@code estado_vital} y {@code estado_libertad} -&gt; {@link EstadoVital} y
 *       {@link EstadoLibertad} del personaje.</li>
 * </ul>
 *
 * <p>Traduccion de ObjectProperties a registros del dominio (cada inversa se normaliza al mismo
 * registro, por eso da igual en que lado del par la escribiera Protege):</p>
 * <ul>
 *   <li>{@code tiene_objeto} / {@code esta_en_posesion_de} -&gt; posesion por defecto.</li>
 *   <li>{@code proporciona_capacidad} -&gt; {@code ProporcionaCapacidad} (conocimiento estatico).</li>
 *   <li>{@code es_hijo_de} / {@code tiene_hijo} -&gt; filiacion.</li>
 *   <li>{@code tiene_favor} -&gt; favor; {@code tiene_enojo} -&gt; enojo.</li>
 *   <li>{@code apresa_a} / {@code esta_preso_por} -&gt; cautiverio.</li>
 *   <li>{@code tiene_capacidad} -&gt; capacidad propia del personaje.</li>
 * </ul>
 * <p>{@code mata_a}, {@code libera_a} y {@code localiza_a} (con sus inversas) no se traducen: describen
 * sucesos que deben deducir las reglas, no estado inicial. Si apareciesen como aserto se avisaria.</p>
 *
 * <p>Relacion con la ontologia: con el {@code mitologia.owl} actual salen 15 personajes, 11 objetos y 4
 * capacidades. Conocimiento por defecto que aporta: Atenea tiene EscudoBronce; Hades tiene CascoHades;
 * Hermes tiene HozAcero; Hefesto tiene EspadaIndestructible; las Ninfas tienen SandaliasAladas y
 * ZurronMagico; las Grayas tienen OjoGrayas y DienteGrayas; Medusa tiene CabezaMedusa; EscudoEspejo y
 * BridaDorada no tienen dueno. Proporciona: CascoHades -&gt; Invisibilidad, SandaliasAladas -&gt; Vuelo,
 * EscudoEspejo -&gt; Reflejo, CabezaMedusa -&gt; Petrificacion. Ceto apresa a Andromeda (Andromeda presa);
 * Casiopea tiene el enojo de Poseidon; Perseo es hijo de Zeus y de Danae y Andromeda de Casiopea. No
 * hay ningun {@code tiene_favor}: los favores llegan como condiciones iniciales del escenario.</p>
 *
 * <p>Lo que no sabe traducir lo anota en {@link #getAvisos()} en lugar de inventarlo o abortar: asi un
 * cambio parcial en Protege no impide cargar el resto del mito, y los tests exigen cero avisos con la
 * ontologia buena, de modo que cualquier desajuste se detecta igualmente.</p>
 */
public class CargadorOntologia {

    /** Espacio de nombres RDF (atributos {@code rdf:about}, {@code rdf:resource}, {@code rdf:type}). */
    private static final String RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    /** Espacio de nombres OWL (declaraciones de clases y propiedades, {@code owl:NamedIndividual}). */
    private static final String OWL = "http://www.w3.org/2002/07/owl#";

    /** Avisos de la ultima carga (clases o propiedades no traducidas, asertos incoherentes). */
    private final List<String> avisos = new ArrayList<String>();

    /**
     * Nombres locales de propiedades que ya se traducen (o que se ignoran a proposito, como
     * {@code type}, {@code comment} y {@code label}). Cualquier otra genera aviso.
     */
    private static final Set<String> PROPIEDADES_CONOCIDAS = new LinkedHashSet<String>();
    static {
        Collections.addAll(PROPIEDADES_CONOCIDAS, "type", "comment", "label", "estado_vital", "estado_libertad",
                "tiene_objeto", "esta_en_posesion_de", "proporciona_capacidad", "es_hijo_de", "tiene_hijo",
                "tiene_favor", "tiene_enojo", "apresa_a", "esta_preso_por", "tiene_capacidad");
    }

    /**
     * Carga el fichero OWL y devuelve un dominio nuevo con sus entidades y relaciones por defecto.
     * Vacia los avisos de cargas anteriores.
     *
     * @throws IllegalArgumentException si el fichero no existe o no es XML valido.
     */
    public DominioMitologico cargar(File owl) {
        avisos.clear();
        // LinkedHashMap para conservar el orden del fichero y que avisos y listados sean reproducibles.
        Map<String, Element> individuos = new LinkedHashMap<String, Element>();
        Map<String, Set<String>> tipos = new LinkedHashMap<String, Set<String>>();
        leerIndividuos(owl, individuos, tipos);

        DominioMitologico d = new DominioMitologico();
        // Dos pasadas porque una relacion puede apuntar a un individuo que aparece mas abajo en el fichero
        // (p. ej. Andromeda esta_preso_por Ceto aparece antes que Ceto).
        // 1.a pasada: entidades
        for (Map.Entry<String, Set<String>> e : tipos.entrySet()) {
            crearEntidad(d, e.getKey(), e.getValue(), individuos.get(e.getKey()));
        }
        // 2.a pasada: relaciones
        for (Map.Entry<String, Element> e : individuos.entrySet()) {
            leerRelaciones(d, e.getKey(), e.getValue());
        }
        return d;
    }

    /** Avisos de la ultima llamada a {@link #cargar}; lista de solo lectura, vacia si todo se tradujo. */
    public List<String> getAvisos() {
        return Collections.unmodifiableList(avisos);
    }

    // ------------------------------------------------------------------

    /**
     * Recorre los nodos identificados de {@code rdf:RDF} y guarda cada individuo (elemento con
     * {@code rdf:about} que no sea una declaracion OWL) junto con el conjunto de clases que declara,
     * tanto por {@code rdf:type} (Protege 5) como por el nombre del elemento (Protege 3).
     */
    private void leerIndividuos(File owl, Map<String, Element> individuos, Map<String, Set<String>> tipos) {
        Element raiz;
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            // Necesario para distinguir rdf:type de owl:Class por espacio de nombres y no por prefijo.
            f.setNamespaceAware(true);
            DocumentBuilder b = f.newDocumentBuilder();
            raiz = b.parse(owl).getDocumentElement();
        } catch (Exception ex) {
            throw new IllegalArgumentException("No se puede leer la ontologia " + owl, ex);
        }
        // Protege 3 puede anidar individuos dentro de relaciones y usar rdf:ID.
        // Reunimos todos sus fragmentos antes de crear entidades y relaciones.
        NodeList nodos = raiz.getElementsByTagNameNS("*", "*");
        Set<String> vocabulario = new LinkedHashSet<String>();
        for (int i = 0; i < nodos.getLength(); i++) {
            Element el = (Element) nodos.item(i);
            if (esDeclaracion(el)) vocabulario.add(identificador(el));
        }
        for (int i = 0; i < nodos.getLength(); i++) {
            Element el = (Element) nodos.item(i);
            String id = identificador(el);
            if (id.isEmpty() || esDeclaracion(el) || vocabulario.contains(id)) continue;
            Set<String> ts = tipos.get(id);
            if (ts == null) {
                ts = new LinkedHashSet<String>();
                tipos.put(id, ts);
                individuos.put(id, raiz.getOwnerDocument().createElement("individuo"));
            }
            if (!esNodoGenerico(el)) ts.add(el.getLocalName());
            for (Element hijo : hijos(el)) {
                if (RDF.equals(hijo.getNamespaceURI()) && "type".equals(hijo.getLocalName())) {
                    String t = destino(hijo);
                    if (!"NamedIndividual".equals(t)) ts.add(t);
                }
                individuos.get(id).appendChild(hijo.cloneNode(true));
            }
        }
    }

    private static String identificador(Element el) {
        String id = el.getAttributeNS(RDF, "ID");
        return id.isEmpty() ? local(el.getAttributeNS(RDF, "about")) : id;
    }

    private static String destino(Element propiedad) {
        String recurso = propiedad.getAttributeNS(RDF, "resource");
        if (!recurso.isEmpty()) return local(recurso);
        for (Element anidado : hijos(propiedad)) {
            String id = identificador(anidado);
            if (!id.isEmpty()) return id;
        }
        return "";
    }

    /**
     * Crea en el dominio la entidad Java que corresponde a las clases OWL del individuo. Exige que
     * caiga en exactamente una de las tres familias disjuntas (Personaje, Objeto, Capacidad); si no,
     * no se carga y se avisa.
     */
    private void crearEntidad(DominioMitologico d, String id, Set<String> clases, Element el) {
        Set<TipoPersonaje> tp = EnumSet.noneOf(TipoPersonaje.class);
        Set<CategoriaObjeto> co = EnumSet.noneOf(CategoriaObjeto.class);
        boolean personaje = false, objeto = false, capacidad = false;
        for (String c : clases) {
            TipoPersonaje t = TipoPersonaje.desdeClaseOwl(c);
            CategoriaObjeto o = CategoriaObjeto.desdeClaseOwl(c);
            // Se aceptan tanto las subclases (Dios, Arma...) como la superclase directa (Personaje, Objeto).
            if (t != null) { tp.add(t); personaje = true; }
            else if ("Personaje".equals(c)) personaje = true;
            else if (o != null) { co.add(o); objeto = true; }
            else if ("Objeto".equals(c)) objeto = true;
            else if ("Capacidad".equals(c)) capacidad = true;
            else avisos.add(id + ": clase no traducida " + c);
        }
        // Relacion con la ontologia: Personaje, Objeto y Capacidad se declaran disjuntas en el OWL.
        int n = (personaje ? 1 : 0) + (objeto ? 1 : 0) + (capacidad ? 1 : 0);
        if (n != 1) {
            avisos.add(id + ": no se carga (tipos " + clases + "; Personaje, Objeto y Capacidad son disjuntas)");
            return;
        }
        if (personaje) {
            String vital = texto(el, "estado_vital");
            String libertad = texto(el, "estado_libertad");
            // La ontologia exige cardinalidad 1 en ambos; si faltan se usa el valor neutro y se avisa.
            if (vital == null || libertad == null) {
                avisos.add(id + ": falta estado_vital o estado_libertad (cardinalidad 1); se asume vivo/libre");
            }
            // El identificador OWL se usa tambien como nombre visible (los IRIs no llevan tildes).
            d.registrarPersonaje(new Personaje(id, id, tp,
                    vital == null ? EstadoVital.VIVO : EstadoVital.desdeOwl(vital),
                    libertad == null ? EstadoLibertad.LIBRE : EstadoLibertad.desdeOwl(libertad)));
        } else if (objeto) {
            d.registrarObjeto(new Objeto(id, id, co));
        } else {
            d.registrarCapacidad(new Capacidad(id, id));
        }
    }

    /**
     * Traduce las ObjectProperties del individuo a registros del dominio. Cada propiedad inversa se
     * invierte aqui para guardar siempre el par en el mismo sentido; el dominio ignora los pares
     * repetidos, asi que si Protege escribe ambos lados (Hades tiene_objeto CascoHades y CascoHades
     * esta_en_posesion_de Hades) solo queda un registro.
     */
    private void leerRelaciones(DominioMitologico d, String id, Element el) {
        boolean esPersonaje = d.existePersonaje(id);
        boolean esObjeto = d.existeObjeto(id);
        // Individuo descartado en la primera pasada: ya tiene su aviso, no se repite por cada propiedad.
        if (!esPersonaje && !esObjeto && !d.existeCapacidad(id)) return;
        for (Element h : hijos(el)) {
            String prop = h.getLocalName();
            String destino = destino(h);
            if (!PROPIEDADES_CONOCIDAS.contains(prop)) {
                avisos.add(id + ": propiedad no traducida " + prop + (destino.isEmpty() ? "" : " " + destino));
                continue;
            }
            // Las DatatypeProperties y rdf:type ya se usaron en crearEntidad: ninguna rama las recoge aqui.
            try {
                if ("tiene_objeto".equals(prop)) d.registrarPosesion(id, destino);
                else if ("esta_en_posesion_de".equals(prop)) d.registrarPosesion(destino, id);
                else if ("proporciona_capacidad".equals(prop)) d.registrarProporciona(id, destino);
                else if ("es_hijo_de".equals(prop)) d.registrarFiliacion(id, destino);
                else if ("tiene_hijo".equals(prop)) d.registrarFiliacion(destino, id);
                else if ("tiene_favor".equals(prop)) d.registrarFavor(id, destino);
                else if ("tiene_enojo".equals(prop)) d.registrarEnojo(id, destino);
                else if ("apresa_a".equals(prop)) d.registrarCautiverio(id, destino);
                else if ("esta_preso_por".equals(prop)) d.registrarCautiverio(destino, id);
                else if ("tiene_capacidad".equals(prop)) d.registrarCapacidadPropia(id, destino);
            } catch (RuntimeException ex) {
                // Destino inexistente o incoherente (p. ej. un objeto con dos duenos por defecto): se avisa
                // y se sigue, para no perder el resto de la ontologia por un aserto erroneo.
                avisos.add(id + ": " + prop + " " + destino + " no se carga: " + ex.getMessage());
            }
        }
    }

    // ------------------------------------------------------------------

    /** Es un elemento del vocabulario OWL (owl:Class, owl:ObjectProperty...) y no un individuo? */
    private static boolean esDeclaracion(Element el) {
        if (!OWL.equals(el.getNamespaceURI())) return false;
        String n = el.getLocalName();
        return !"NamedIndividual".equals(n);
    }

    /** Es un nodo cuyo nombre no indica la clase ({@code owl:NamedIndividual} o {@code rdf:Description})? */
    private static boolean esNodoGenerico(Element el) {
        return (OWL.equals(el.getNamespaceURI()) && "NamedIndividual".equals(el.getLocalName()))
                || (RDF.equals(el.getNamespaceURI()) && "Description".equals(el.getLocalName()));
    }

    /** Hijos de tipo elemento (se descartan textos en blanco y comentarios XML). */
    private static List<Element> hijos(Element el) {
        List<Element> lista = new ArrayList<Element>();
        NodeList nl = el.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i) instanceof Element) lista.add((Element) nl.item(i));
        }
        return lista;
    }

    /** Valor literal de la primera DatatypeProperty con ese nombre, o {@code null} si no aparece. */
    private static String texto(Element el, String propiedad) {
        for (Element h : hijos(el)) {
            if (propiedad.equals(h.getLocalName())) return h.getTextContent().trim();
        }
        return null;
    }

    /** Nombre local de un IRI (lo que va tras {@code #}), que es el identificador usado en todo el modelo. */
    private static String local(String iri) {
        int i = iri.lastIndexOf('#');
        return i >= 0 ? iri.substring(i + 1) : iri;
    }
}

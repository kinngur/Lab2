package aed.multisets;

import es.upm.aedlib.Pair;
import es.upm.aedlib.Position;
import es.upm.aedlib.positionlist.PositionList;
import es.upm.aedlib.positionlist.NodePositionList;
import java.lang.Math;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Una implementacion de un multiset (multiconjunto) a traves de una lista
 * de posiciones.
 */
public class MultiSetList<E> implements MultiSet<E> {

  /**
   * La estructura de datos que guarda los elementos del multiset.
   */
  private PositionList<Pair<E,Integer>> elements;

  /**
   * El tamaño del multiset.
   */
  private int size;

  /**
   * Construye un multiset vacio.
   */
  public MultiSetList() {
    this.elements = new NodePositionList<Pair<E,Integer>>();
    this.size = 0;
  }

  /**
   * Añade n copias de elem al multiset.
   * @throws IllegalArgumentException si n<0.
   */
  @Override
  public void add(E elem, int n) {
    if (n<0) {
      throw new IllegalArgumentException();
    }
    else if (n>0){
      if (isIn(elem)) {
        Position<Pair<E, Integer>> valCursor = cursorAux(elem);
        int sumaVal = valCursor.element().getRight() + n;
        elements.remove(valCursor);
        elements.addLast(new Pair<>(elem, sumaVal));
      } else {
        elements.addLast(new Pair<>(elem, n));
      }
    }
  }

  /**
   * Borra n copias de elem en el multiset.
   * Devuelve el numero de copias borrados, que es 0 o n.
   *
   * @throws IllegalArgumentException si n<0.
   */
  @Override
  public int remove(E elem, int n) {
    if (n<0) {
      throw new IllegalArgumentException();
    }
    if (n==0) {
      return 0;
    }
    if (isIn(elem)) {
      Position<Pair<E, Integer>> valCursor = cursorAux(elem);
      int valActual = multiplicity(elem);

      if (n > valActual) {
        return 0; //if n>valActual ==> Quieres quitar "MÁS" de las que hay ==> Imposible, luego devuelves 0
      }
      else {
        elements.remove(valCursor); //if n<=valActual ==> Puedes quitar
        size -= valActual;
        if (valActual - n > 0) { //if n < valActual ==> No se eliminan todos ==> Tienes que ajustar los que no se quitan
          elements.addLast(new Pair<>(elem, valActual - n));
          size += valActual - n;
        }
        return n; //como n < valActual ==> Se eliminan valActual - n ==> Se devuelve n que son los eliminados
      }
    }
    else {  //if !isIn(elem) ==> No quitas ninguno ==> Quitas 0
      return 0;
    }
  }


  /**
   * Devuelve el numero de copias de elem en el multiset.
   *
   */
  @Override
  public int multiplicity(E elem) {
    int numAux = 0;
    if (isIn(elem)) {
      Position<Pair<E,Integer>> valCursor = cursorAux(elem);
      numAux = valCursor.element().getRight();
    }
    return numAux;
  }

  /**
   * Devuelve el numero total de copias de elementos en el multiset.
   * Por ejemplo, si s = {a,b,a,b,b} entonces s.size() devuelve 5.
   */
  @Override
  public int size() {
    int tamañoLista = 0;
    for (E elem : elements()) {
      tamañoLista += multiplicity(elem);
    }
    return tamañoLista;
  }

  /**
   * Devuelve true si el multiconjunto es vacio, y false si no es vacio.
   */
  @Override
  public boolean isEmpty() {
    return size()==0;
  }

  /**
   * Devuelve una lista con los elementos que tiene una multiplicidad > 0
   * dentro el multiconjunto.
   * El orden de los elementos en el resultado no importa.
   * Por ejemplo, si s = {a,b,a,b,b} entonces devuelve la lista [a,b].
   */
  @Override
  public PositionList<E> elements() {
    PositionList<E> resultado = new NodePositionList<>();
    elements.forEach(x -> {
      if (x.getRight() > 0) {
        resultado.addLast(x.getLeft());
      }
    });
    return resultado;
  }

  /**
   * Devuelve un multiset nuevo que es la suma de this y s.
   * Por ejemplo, si this={a,b,b} y s={a,a,c} entonces devuelve el multiconjunto
   * {a,a,a,b,b,c}.
   */
  @Override
  public MultiSet<E> sum(MultiSet<E> s) {
    MultiSetList<E> lista = new MultiSetList<>();
    this.elements.forEach(e -> lista.add(e.getLeft(),e.getRight()));
    MultiSetList<E> s2 = (MultiSetList<E>) s;
    s2.elements.forEach(e -> lista.add(e.getLeft(), e.getRight()));
    return lista;
  }

  /**
   * Devuelve un multiset nuevo que es "this resta s".
   * Por ejemplo, si this={a,a,b,b} y s={a,c} entonces devuelve el multiconjunto
   * {a,b,b}.
   */
  @Override
  public MultiSet<E> minus(MultiSet<E> s) {
    MultiSetList<E> lista = new MultiSetList<>();
    MultiSetList<E> s2 = (MultiSetList<E>) s;
    this.elements.forEach((e -> {
      if(s2.isIn(e.getLeft())) {
        if ((e.getRight() - s2.multiplicity(e.getLeft())) < 0) // checkeo puesto que la multiplicidad ha de ser siempre >= 0
          lista.add(e.getLeft(), 0);
        else
          lista.add(e.getLeft(), e.getRight() - s2.multiplicity(e.getLeft()));
      } else {
        lista.add(e.getLeft(),e.getRight());
      }
    }));
    return lista;
  }

  /**
   * Devuelve un multiset nuevo que es la interseccion de this y s.
   * Por ejemplo, si s={a,a,b,b} y this={a,c} entonces devuelve el multiconjunto {a}.
   */
  @Override
  public MultiSet<E> intersection(MultiSet<E> s) {
    MultiSetList<E> lista = new MultiSetList<>();
    MultiSetList<E> s2 = (MultiSetList<E>) s;
    this.elements.forEach((e -> {
      if(s2.isIn(e.getLeft())) {
        lista.add(e.getLeft(), Math.min(e.getRight(),s2.multiplicity(e.getLeft())));
      }
    }));
    return lista;
  }

  /**
   * Devuelve true si this es un submultiset de s, y false si no.
   * Por ejemplo, si s={a,b} y this={a} devuelve true,
   * si s={a} y this={a} devuelve true,
   * si s={a} y this={a,b} devuelve false,
   * y si s={a} y this={a,a} devuelve false,
   */
  @Override
  public boolean subsetEqual(MultiSet<E> s) {
    MultiSetList<E> s2 = (MultiSetList<E>) s;
    AtomicBoolean resultado = new AtomicBoolean(true);
    this.elements.forEach(e -> {
      if(!s2.isIn(e.getLeft()) || e.getRight() > s2.multiplicity(e.getLeft())) { // si alguno de los elementos de this no esta en s
        resultado.set(false);
      }
    });
    return resultado.get();
  }


  // codigo auxiliar fuera de la practica
  private boolean isIn (E e) {  //función similar en transparencias
    Position<Pair<E,Integer>> p = elements.first();
    boolean found = false;
    while (p != null && !found) {
      E lE = p.element().getLeft();
      found = e.equals(lE);
      p = elements.next(p);
    }
    return found;
  }

  private Position<Pair<E, Integer>> cursorAux (E e) {
    Position<Pair<E, Integer>> valor = elements.first();
    boolean bucleAux = false;
    while (!bucleAux && valor != null) {
      if (e.equals(valor.element().getLeft())) {
        return valor;
      } else {
        valor = elements.next(valor);
      }
    }
    return valor;
  }
}

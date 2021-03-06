package strawman.collection

import scala.{Boolean, Int, Option, None}

import strawman.collection.mutable.Builder

trait MapView[K, +V]
  extends MapOps[K, V, ({ type l[X, Y] = View[(X, Y)] })#l, View[(K, V)]]
    with View[(K, V)] {

  override def view: MapView[K, V] = this

  def mapFactory: MapFactory[({ type l[X, Y] = View[(X, Y)] })#l] =
    new MapFactory[({ type l[X, Y] = View[(X, Y)] })#l] {
      def newBuilder[X, Y](): Builder[(X, Y), View[(X, Y)]] = View.newBuilder[(X, Y)]()
      def empty[X, Y]: View[(X, Y)] = View.empty
      def from[X, Y](it: IterableOnce[(X, Y)]): View[(X, Y)] = View.from(it)
    }

  def empty: View[(K, V)] = View.Empty

}

object MapView {

  /** An `IterableOps` whose collection type and collection type constructor are unknown */
  type SomeIterableConstr[X, Y] = IterableOps[_, AnyConstr, _]
  /** A `MapOps` whose collection type and collection type constructor are (mostly) unknown */
  type SomeMapOps[K, +V] = MapOps[K, V, SomeIterableConstr, _]

  class Id[K, +V](underlying: SomeMapOps[K, V]) extends MapView[K, V] {
    def get(key: K): Option[V] = underlying.get(key)
    def iterator(): Iterator[(K, V)] = underlying.iterator()
    override def knownSize: Int = underlying.knownSize
  }

  class MapValues[K, +V, +W](underlying: SomeMapOps[K, V], f: V => W) extends MapView[K, W] {
    def iterator(): Iterator[(K, W)] = underlying.iterator().map(kv => (kv._1, f(kv._2)))
    def get(key: K): Option[W] = underlying.get(key).map(f)
    override def knownSize: Int = underlying.knownSize
  }

  class FilterKeys[K, +V](underlying: SomeMapOps[K, V], p: K => Boolean) extends MapView[K, V] {
    def iterator(): Iterator[(K, V)] = underlying.iterator().filter { case (k, _) => p(k) }
    def get(key: K): Option[V] = if (p(key)) underlying.get(key) else None
  }

}
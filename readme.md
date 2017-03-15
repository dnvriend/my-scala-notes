# my-scala-notes
My scala notes, because I need to store them somewhere...

## Case Class Tricks
A case class is a [scala.Product](http://www.scala-lang.org/files/archive/api/current/scala/Product.html), which
is the base trait for all case classes and [tuples](http://www.scala-lang.org/files/archive/api/current/scala/Tuple1.html). Because both case classes and tuples are a Product type, they have access to the methods:

| method | description |
| ------ | ----------- |
| productArity: Int | The size of this product ie. the number of types it contains eg. Person(name: String, age: Int) has an arity of 2 |
| productElement(n: Int): Any | Returns the n-th element of the product, and is zero-based |
| productIterator: Iterator[Any] | An iterator over the elements of the product |
| productPrefix: String | A String used in the toString() methods of derived classes | 

```scala
scala> case class Person(name: String, age: Int)
defined class Person

scala> val person = Person("foo", 42)
person: Person = Person(foo,42)

scala> person.isInstanceOf[Product]
res0: Boolean = true

scala> val values = person.productIterator.toList
values: List[Any] = List(foo, 42)

scala> person.productElement(0)
res1: Any = foo

scala> person.productElement(1)
res2: Any = 42

scala> person.productArity
res3: Int = 2

scala> person.productPrefix
res4: String = Person
```

Getting the fields of the case class:

```scala
import scala.reflect.runtime.universe._

scala> def getMethods[T <: Product : TypeTag] : List[MethodSymbol] = typeOf[T].members.collect {
     |     case m: MethodSymbol if m.isCaseAccessor => m
     |   }.toList
getMethods: [T <: Product](implicit evidence$1: reflect.runtime.universe.TypeTag[T])List[reflect.runtime.universe.MethodSymbol]

scala> getMethods[Person]
res5: List[reflect.runtime.universe.MethodSymbol] = List(value age, value name)

scala> getMethods[Person].map(_.name.toString)
res6: List[String] = List(age, name)
```

Getting the names and values of the case class:

```scala
scala> :paste
// Entering paste mode (ctrl-D to finish)

getMethods[Person]
  .map(_.name.toString)
  .iterator.zip(Person("foo", 42).productIterator)
  .toMap

// Exiting paste mode, now interpreting.

res7: scala.collection.immutable.Map[String,Any] = Map(age -> foo, name -> 42)
```
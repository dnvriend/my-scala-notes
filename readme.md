# my-scala-notes
My scala notes, because I need to store them somewhere...

## How to unify method parameters and tuples
Scala does not unify method parameters and tuples, for example, the following method:

```scala
def addNumbers(x: Int, y: Int): Int = x + y
```

Doesn't work with tuples. It would be nice if it did, but that is not the case.
Also, changing the method API to tuples doesn't work as nice:

```scala
def addNumbers(numbers: (Int, Int)): Int = numbers match {
  case (x, y) => x + y
}
```

What is the alternative? Well, we can 'convert' the method to a function and change its interface to a tuple:

```scala
scala> def addNumbers(x: Int, y: Int): Int = x + y
addNumbers: (x: Int, y: Int)Int

scala> addNumbers _
res0: (Int, Int) => Int = $$Lambda$1495/60466312@3a332eed

scala> res0.tupled
res1: ((Int, Int)) => Int = scala.Function2$$Lambda$229/1860944798@3c0178f

scala> val numbers = (1, 2)
numbers: (Int, Int) = (1,2)

scala> res1(numbers)
res2: Int = 3
```

The pattern of __converting a method to a function and then calling '.tupled'__ can be useful because most operations
we do in practise return pairs or list of pairs:

```scala
scala> val xs = Vector(1, 2, 3, 4)
xs: scala.collection.immutable.Vector[Int] = Vector(1, 2, 3, 4)

scala> val ys = xs.zip(xs.tail)
ys: scala.collection.immutable.Vector[(Int, Int)] = Vector((1,2), (2,3), (3,4))

scala> ys.map(res1)
res3: scala.collection.immutable.Vector[Int] = Vector(3, 5, 7)

scala> ys.map((addNumbers _).tupled)
res4: scala.collection.immutable.Vector[Int] = Vector(3, 5, 7)
```

## Curried functions
Methods can be converted to functions like so:

```scala
scala> def addNumbers(x: Int, y: Int) = x + y
addNumbers: (x: Int, y: Int)Int

scala> val x = addNumbers _
x: (Int, Int) => Int = $$Lambda$1530/265754685@1e63cd7e
```

The function can then be applied by applying the function with all the arguments:

```scala
scala> x(1, 2)
res0: Int = 3
```

We can also curry the function, which means that we've converted the function that takes multiple arguments
into a sequence of functions each taking a single argument:

```scala
scala> x.curried
res1: Int => (Int => Int) = scala.Function2$$Lambda$1529/1670650711@5c7bbf0a

scala> x(1)
scala> val y = x.curried
y: Int => (Int => Int) = scala.Function2$$Lambda$1529/1670650711@60e784de

scala> y(1)
res2: Int => Int = scala.Function2$$Lambda$1531/1050434890@6fae8b4d

scala> res3(2)
res3: Int = 3
```

## Scalaz Validation: Parsing user input
Parsing user input is easy with Scala and is provided by the class [StringLike](http://scala-lang.org/files/archive/api/current/scala/collection/immutable/StringLike.html)
and provides methods like 'toBoolean', 'toInt', 'toLong', 'toDouble', but all of these methods can throw exceptions like NumberFormatException and IllegalArgumentException.

```scala
scala> "foo".toBoolean
java.lang.IllegalArgumentException: For input string: "foo"

scala> "foo".toDouble
java.lang.NumberFormatException: For input string: "foo"
```

Of course, we can wrap the operation in a [scala.util.Try](http://scala-lang.org/files/archive/api/current/scala/util/Try.html), but this is not ideal:

```scala
scala> Try("foo".toBoolean)
res0: scala.util.Try[Boolean] = Failure(java.lang.IllegalArgumentException: For input string: "foo")
```

Scalaz provides [scalaz.syntax.std.StingOps](https://github.com/scalaz/scalaz/blob/v7.2.11/core/src/main/scala/scalaz/syntax/std/StringOps.scala) and
provides the methods:

- def parseBoolean: Validation[IllegalArgumentException, Boolean] = s.parseBoolean(self)
- def parseByte: Validation[NumberFormatException, Byte] = s.parseByte(self)
- def parseShort: Validation[NumberFormatException, Short] = s.parseShort(self)
- def parseInt: Validation[NumberFormatException, Int] = s.parseInt(self)
- def parseLong: Validation[NumberFormatException, Long] = s.parseLong(self)
- def parseFloat: Validation[NumberFormatException, Float] = s.parseFloat(self)
- def parseDouble: Validation[NumberFormatException, Double] = s.parseDouble(self)
- def parseBigInt: Validation[NumberFormatException, BigInt] = s.parseBigInt(self)
- def parseBigDecimal: Validation[NumberFormatException, BigDecimal] = s.parseBigDecimal(self)

```scala
scala> import scalaz._
import scalaz._

scala> import Scalaz._
import Scalaz._

scala> "foo".parseBoolean
res0: scalaz.Validation[IllegalArgumentException,Boolean] = Failure(java.lang.IllegalArgumentException: For input string: "foo")

scala> "true".parseBoolean
res1: scalaz.Validation[IllegalArgumentException,Boolean] = Success(true)
```

All the methods provided by [scalaz.syntax.std.StingOps](https://github.com/scalaz/scalaz/blob/v7.2.11/core/src/main/scala/scalaz/syntax/std/StringOps.scala)
return a [scalaz.Validation](https://github.com/scalaz/scalaz/blob/v7.2.11/core/src/main/scala/scalaz/Validation.scala) with a Throwable on the failure side
and the expected type on the success side.

## Creating Validation instances
The companion object of [scalaz.Validation](https://github.com/scalaz/scalaz/blob/v7.2.11/core/src/main/scala/scalaz/Validation.scala) provides the following
ways to create Validation instances:

```scala
// create a success instance
scala> Validation.success[String, Int](1)
res0: scalaz.Validation[String,Int] = Success(1)

// create a failure instance
scala> Validation.failure[String, Int]("My Error")
res1: scalaz.Validation[String,Int] = Failure(My Error)

// create a FailureNel instance
scala> Validation.failureNel[String, Int]("My Error")
res2: scalaz.ValidationNel[String,Int] = Failure(NonEmpty[My Error])

// create a Validation, based on a predicate, the instance will either be a
// success or a failure
scala> Validation.lift(17)(age => age < 18, "You must be at least 18 to buy beer")
res3: scalaz.Validation[String,Int] = Failure(You must be at least 18 to buy beer)

// create a ValidationNel instance
scala> Validation.liftNel(17)(age => age < 18, "You must be at least 18 to buy beer")
res4: scalaz.ValidationNel[String,Int] = Failure(NonEmpty[You must be at least 18 to buy beer])

// create a validation from a thunk that can throw
scala> Validation.fromTryCatchThrowable[Int, Throwable](1/0)
res5: scalaz.Validation[Throwable,Int] = Failure(java.lang.ArithmeticException: / by zero)

// create a validation from a thunk that throws nonfatal exceptions
scala> Validation.fromTryCatchNonFatal[Int](1/0)
res6: scalaz.Validation[Throwable,Int] = Failure(java.lang.ArithmeticException: / by zero)

// create a validation from an either
scala> val answer = scala.util.Right[String, Int](42)
answer: scala.util.Right[String,Int] = Right(42)

scala> Validation.fromEither(answer)
res7: scalaz.Validation[String,Int] = Success(42)
```

The most interesting constructors are 'lift' and 'liftNel' that take a predicate, and if that predicate resolves to true, it will
return a Failure with the contents of the second argument of the 'lift' method. These two methods are really handy for encoding
business rule validations:

```scala
scala> def validateAge(age: Int): ValidationNel[String, Int] = Validation.liftNel(age)(age => age < 18, "You must be at least 18 to buy beer")
validateAge: (age: Int)scalaz.ValidationNel[String,Int]

scala> def validateName(name: String): ValidationNel[String, String] = Validation.liftNel(name)(name => name.isEmpty, "Name must not be empty")
validateName: (name: String)scalaz.ValidationNel[String,String]

scala> validateAge(17)
res0: scalaz.ValidationNel[String,Int] = Failure(NonEmpty[You must be at least 18 to buy beer])

scala> validateName("")
res1: scalaz.ValidationNel[String,String] = Failure(NonEmpty[Name must not be empty])
```

## Using Validation results in an imperative way
The [scalaz.Validation](https://github.com/scalaz/scalaz/blob/v7.2.11/core/src/main/scala/scalaz/Validation.scala) instances contain two properties
'isSuccess' and 'isFailure' that return a boolean, so we could do the following:

```scala
scala> if(validateAge(17).isSuccess && validateName("Dennis").isSuccess) "yeah success!" else "darn, a failure"
res0: String = darn, a failure
```

Of course there are better ways, lets go functional!

## Using Validation results the functional way
Lets do the following, parse an electronic form that takes the user name and the user age:

```scala
scala> val formName = ""
formName: String = ""

scala> val formAge = "17"
formAge: String = 17

// convert it to a ValidationNel[String, Int]
scala> val parsedAge: ValidationNel[String, Int] =
     |   formAge.parseInt.leftMap(_.toString).toValidationNel
parsedAge: scalaz.ValidationNel[String,Int] = Success(17)

// define some validation methods
scala> def validateName(name: String): ValidationNel[String, String] =
     |   Validation.liftNel(name)(name => name.isEmpty, "Name must not be empty")
validateName: (name: String)scalaz.ValidationNel[String,String]

scala> def validateAge(age: Int): ValidationNel[String, Int] =
     |   Validation.liftNel(age)(age => age < 18, "You must be at least 18 to buy beer")
validateAge: (age: Int)scalaz.ValidationNel[String,Int]

// create a case class to capture a validated person
scala> case class Person(name: String, age: Int)
defined class Person

// define a validation to validate name and age, accrue the failures and create
// a person that captures a valid person
scala> def validateNameAndAge(name: String, age: Int): ValidationNel[String, Person] =
     |   (validateName(name) |@| validateAge(age))(Person.apply)
validateNameAndAge: (name: String, age: Int)scalaz.ValidationNel[String,Person]

// Validation from a form has two steps:
// 1. converting from the String types to the expected types so String => Int
// 2. Validating the business rules and accrue the results

// we can use Validation is a for-expression by importing the following:
scala> import scalaz.Validation.FlatMap.ValidationFlatMapRequested
import scalaz.Validation.FlatMap.ValidationFlatMapRequested

scala> for {
     |   age <- parsedAge
     |   person <- validateNameAndAge(formName, age)
     | } yield person
res0: scalaz.Validation[scalaz.NonEmptyList[String],Person] =
  Failure(NonEmpty[Name must not be empty,You must be at least 18 to buy beer])
```

## Validating a list of business rules
We can use the 'traverse' method on a List of business rules to validate the person eg:

```scala
scala> val formName = "Dennis42"
formName: String = Dennis42

scala> val formAge = "17"
formAge: String = 17

scala> val parsedAge: ValidationNel[String, Int] =
     |   formAge.parseInt.leftMap(_.toString).toValidationNel
parsedAge: scalaz.ValidationNel[String,Int] = Success(17)

// lets create some validations
scala> def validateNonEmpty(name: String): ValidationNel[String, String] =
     |   Validation.liftNel(name)(name => name.isEmpty, "Name must not be empty")
validateNonEmpty: (name: String)scalaz.ValidationNel[String,String]

scala> def validateLength(name: String): ValidationNel[String, String] =
     |   Validation.liftNel(name)(name => name.length > 7, "Maximum length of 7 chars exceeded")
validateLength: (name: String)scalaz.ValidationNel[String,String]

scala> def validateOnlyLetters(name: String): ValidationNel[String, String] =
     |   Validation.liftNel(name)(name => name.exists(char => !char.isLetter), "name must contain only letters")
validateOnlyLetters: (name: String)scalaz.ValidationNel[String,String]

scala> def validateAge(age: Int): ValidationNel[String, Int] =
     |   Validation.liftNel(age)(age => age < 18, "You must be at least 18 to buy beer")
validateAge: (age: Int)scalaz.ValidationNel[String,Int]

scala> def validateName(name: String): ValidationNel[String, String] = {
     |   NonEmptyList(
     |     validateNonEmpty _,
     |     validateLength _,
     |     validateOnlyLetters _)
     |     .traverseU(_.apply(name))
     |     .rightMap(_.head)
     | }
validateName: (name: String)scalaz.ValidationNel[String,String]

scala> case class Person(name: String, age: Int)
defined class Person

scala> def validateNameAndAge(name: String, age: Int): ValidationNel[String, Person] =
     |   (validateName(formName) |@| validateAge(age)) (Person.apply)
validateNameAndAge: (name: String, age: Int)scalaz.ValidationNel[String,Person]

// we can use Validation is a for-expression by importing the following:
scala> import scalaz.Validation.FlatMap.ValidationFlatMapRequested
import scalaz.Validation.FlatMap.ValidationFlatMapRequested

scala> for {
     |   age <- parsedAge
     |   person <- validateNameAndAge(formName, age)
     | } yield person
res1: scalaz.Validation[scalaz.NonEmptyList[String],Person] =
  Failure(NonEmpty[Maximum length of 7 chars exceeded,name must contain only letters,You must be at least 18 to buy beer])
```

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

## Scalaz ReaderT usage
A [monad transformer](http://eed3si9n.com/learning-scalaz/Monad+transformers.html) for the Reader Monad

```scala
import scalaz._
import Scalaz._
import scala.language.higherKinds
import scala.language.implicitConversions

case class StringStats(length: Int, palindrome: Boolean)

def stringStats(calcLength: String => Int, isPalindrome: String => Boolean): String => StringStats = for {
  length <- calcLength
  palindrome <- isPalindrome
} yield StringStats(length, palindrome)

def stringStatsCtx[F[_]: Monad]
  (calcLength: ReaderT[F, String, Int],
   isPalindrome: ReaderT[F, String, Boolean]): ReaderT[F, String, StringStats] = for {
  length <- calcLength
  palindrome <- isPalindrome
} yield StringStats(length, palindrome)

def compose[F[_]: Monad, A: Monoid](fx: F[A], fy: F[A]): F[A] = for {
  x <- fx
  y <- fy
} yield x |+| y

compose[Id, Int](1, 2) == 3

val statsFunction: (String) => StringStats = 
  stringStats(_ => 4, _ => true)

statsFunction("abba") == StringStats(4, true)

// actually use the function and define the effect
val statsFunctionCtx: ReaderT[Option, String, StringStats] =
  stringStatsCtx[Option](
    ReaderT[Option, String, Int](_ => Option(4)),
    ReaderT[Option, String, Boolean](_ => Option(true))
  )

statsFunctionCtx("abba") == Some(StringStats(4, true))

// there must be an implicit execution context
// available at the call site
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
def callStrLengthService(str: String): Future[Int] =
  Future.successful(4)

def callStrPalindromeService(str: String): Future[Boolean] =
  Future.successful(str.reverse == str)

import scala.concurrent.ExecutionContext.Implicits.global
val statsFunctionAsync: ReaderT[Future, String, StringStats] =
  stringStatsCtx[Future](
    ReaderT[Future, String, Int](callStrLengthService),
    ReaderT[Future, String, Boolean](callStrPalindromeService)
  )

Await.result(statsFunctionAsync("abba"), 1.second) == 
  StringStats(4, true)
```

## Creating and Parsing a binary String
Whats really fun is converting a text String to a binary String and back again:

```scala
"Hello World".getBytes.map(Integer.toBinaryString(_)).mkString(" ")

"1001000 1100101 1101100 1101100 1101111 100000 1010111 1101111 1110010 1101100 1100100"
.split("\\s").map(Integer.parseInt(_, 2)).map(_.toChar).mkString
```

## Scala object equality
In Scala we check for object equality using '==' operator. The '==' operator
delegates to the '.equals' method:

```scala
scala> 1 == 1
res0: Boolean = true

scala> new String("foo") == new String("foo")
res1: Boolean = true
```

When creating our own value objects manually, we should override the equals method, but there is
a better way, using case classes. Case classes create a valid '.equals' and 'hashcode' methods:

```scala
scala> case class Person(name: String, age: Int)
defined class Person

scala> Person("foo", 42) == Person("foo", 42)
res2: Boolean = true
```

## Checking for reference equality
In Scala we check for reference equality using the 'eq' and 'ne' operators:

```scala
scala> new String("foo") eq new String("foo")
res0: Boolean = false

scala> new String("foo") ne new String("foo")
res1: Boolean = true
```

## Simple FizzBuzz example
FizzBuzz can be solved in many ways, you could do the following:

```scala
def evaluate(x: Int): String = {
  if(x % 15 == 0)
    "FizzBuzz"
  else if(x % 3 == 0) 
    "Fizz"
  else if(x % 5 == 0)
    "Buzz"
  else x.toString
}

(1 to 100).map(evaluate)
```

## FizzBuzz with pattern matching
If you like pattern matching approach: 

Using guards:

```scala
(1 to 100)
  .map {
    case x if x % 15 == 0 => "FizzBuzz"
    case x if x % 3 == 0 => "Fizz"
    case x if x % 5 == 0 => "Buzz"
    case x => x.toString
  }
```

Using extractors:

```scala
object FizzBuzz {
  def unapply(arg: Int): Option[String] =
    Option(arg).find(_ % 15 == 0).map(_ => "FizzBuzz")
}

object Fizz {
  def unapply(arg: Int): Option[String] =
    Option(arg).find(_ % 3 == 0).map(_ => "Fizz")
}

object Buzz {
  def unapply(arg: Int): Option[String] =
    Option(arg).find(_ % 5 == 0).map(_ => "Buzz")
}

(1 to 100)
  .map {
    case FizzBuzz(x) => x
    case Fizz(x) => x
    case Buzz(x) => x
    case x => x.toString
  }
```

Using tuples:

```scala
(1 to 100)
  .map(x => (x, x % 3, x % 5))
  .map {
    case (_, 0, 0) => "FizzBuzz"
    case (_, 0, _) => "Fizz"
    case (_, _, 0) => "Buzz"
    case (nr,_, _) => nr
  }
```

Using a Stream:

```scala
Stream.from(1)
  .map(x => (x, x % 3, x % 5))
  .map {
    case (_, 0, 0) => "FizzBuzz"
    case (_, 0, _) => "Fizz"
    case (_, _, 0) => "Buzz"
    case (nr,_, _) => nr
  }
.take(35)
.toList
```

## A FizzBuzz example
Of course, the FizzBuzz can be solved in many ways, this is just one of many
that uses an aggregator approach:

```scala
val fizzPred: Int => Boolean = (_: Int) % 3 == 0
val buzzPred: Int => Boolean = (_: Int) % 5 == 0
val woofPred: Int => Boolean = (_: Int) % 7 == 0

val fizzTxt = (x: Boolean) => if(x) Option("Fizz") else None
val buzzTxt = (x: Boolean) => if(x) Option("Buzz") else None
val woofTxt = (x: Boolean) => if(x) Option("Woof") else None

val fizz = fizzPred andThen fizzTxt
val buzz = buzzPred andThen buzzTxt
val woof= woofPred andThen woofTxt

val rules = List(fizz, buzz, woof)

val ys = (1 to 35).map(x => (x, rules))
.map {
  case (x, xs) => (x, xs.flatMap(_(x)))
}
.map {
  case (x, Nil) => x.toString
  case (x, xs) => xs.mkString
}

ys.foreach(println)

1
2
Fizz
4
Buzz
Fizz
Woof
8
Fizz
Buzz
11
Fizz
13
Woof
FizzBuzz
16
17
Fizz
19
Buzz
FizzWoof
22
23
Fizz
Buzz
26
Fizz
Woof
29
FizzBuzz
31
32
Fizz
34
BuzzWoof
```

## FizzBuzz with Scalaz
Also using an aggregator:

```scala
import scalaz._
import Scalaz._

val fizzPred = (_: Int) % 3 == 0
val buzzPred = (_: Int) % 5 == 0
val woofPred = (_: Int) % 7 == 0
  
val fizzTxt = (x: Boolean) => if(x) Option("Fizz") else None
val buzzTxt = (x: Boolean) => if(x) Option("Buzz") else None
val woofTxt = (x: Boolean) => if(x) Option("Woof") else None

val fizz = fizzPred andThen fizzTxt
val buzz = buzzPred andThen buzzTxt
val woof= woofPred andThen woofTxt

val rules = List(fizz, buzz, woof)

val ys = (1 to 35).map { x =>
  // apply the value to the list of rules
  (List(x) <*> rules)
    // the resulting List[Option] will be flatten'd
    // and converted to an Option[NonEmptyList]
    .flatten.toNel
    // if its a Some[NonEmptyList], convert that NEL to a String
    .map(_.fold)
    // get the value, or put the x' value here as a String
    .getOrElse(x.toString)
}

ys.foreach(println)

1
2
Fizz
4
Buzz
Fizz
Woof
8
Fizz
Buzz
11
Fizz
13
Woof
FizzBuzz
16
17
Fizz
19
Buzz
FizzWoof
22
23
Fizz
Buzz
26
Fizz
Woof
29
FizzBuzz
31
32
Fizz
34
BuzzWoof
```

## FizzBuzz using Scalaz Validation
A solution (of many) that involves using Scalaz Validation:

```scala
import scalaz._
import Scalaz._

val fizz = (x: Int) =>
  Option(x).filterNot(_ % 3 == 0).map(_.toString).toSuccessNel("Fizz")
val buzz = (x: Int) =>
  Option(x).filterNot(_ % 5 == 0).map(_.toString).toSuccessNel("Buzz")
val woof = (x: Int) =>
  Option(x).filterNot(_ % 7 == 0).map(_.toString).toSuccessNel("Woof")

val rules = List(fizz, buzz, woof)

val ys = (1 to 35).toList.map { x =>
  val ys = List(x) <*> rules
  ys.sequenceU.rightMap(_.head).valueOr(_.fold)
}

ys.foreach(println)

1
2
Fizz
4
Buzz
Fizz
Woof
8
Fizz
Buzz
11
Fizz
13
Woof
FizzBuzz
16
17
Fizz
19
Buzz
FizzWoof
22
23
Fizz
Buzz
26
Fizz
Woof
29
FizzBuzz
31
32
Fizz
34
BuzzWoof
```

## Finding a line in a file that is double
Say, we want to find a line in a file that is double, so the line must be somewhere in the file but is double.
Because the solution revolves around aggregating results, or 'appending', Monoids play a big role. Using a library
like Scalaz for example, you can use the Monoids from these libraries and saves a lot of boilerplate code so the
code now just specifies what to do, and not how to do it:

```scala
import java.security.MessageDigest

val lines =
  """
    |Now look at them yo-yos thats the way you do it
    |You play the guitar on the mtv
    |That aint workin thats the way you do it
    |Money for nothin and chicks for free
    |Now that aint workin thats the way you do it
    |Lemme tell ya them guys aint dumb
    |Maybe get a blister on your little finger
    |Maybe get a blister on your thumb
    |
    |We gotta install microwave ovens
    |Custom kitchen deliveries
    |We gotta move these refrigerators
    |We gotta move these colour tvs
    |
    |See the little faggot with the earring and the makeup
    |Yeah buddy thats his own hair
    |That little faggot got his own jet airplane
    |That little faggot hes a millionaire
    |
    |We gotta install microwave ovesns
    |Custom kitchens deliveries
    |We gotta move these refrigerators
    |We gotta move these colour tvs
    |
    |I shoulda learned to play the guitar
    |I shoulda learned to play them drums
    |Look at that mama, she got it stickin in the camera
    |Man we could have some fun
    |And hes up there, whats that? hawaiian noises?
    |Bangin on the bongoes like a chimpanzee
    |That aint workin thats the way you do it
    |Get your money for nothin get your chicks for free
    |
    |We gotta install microwave ovens
    |Custom kitchen deliveries
    |We gotta move these refrigerators
    |We gotta move these colour tvs, lord
    |
    |Now that aint workin thats the way you do it
    |You play the guitar on the mtv
    |That aint workin thats the way you do it
    |Money for nothin and your chicks for free
    |Money for nothin and chicks for free
  """.stripMargin.trim.replace(",", "").replace("'", "").replace("?", "").split("\n")

def hasher(str: String, algo: String = "MD5"): String = {
  val HexChars = "0123456789abcdef".toCharArray
  val digest = MessageDigest.getInstance(algo)
  val bytes: Array[Byte] = digest.digest(str.getBytes)
  val buffer = new StringBuilder(bytes.length * 2)
  bytes.foreach { byte =>
    buffer.append(HexChars((byte & 0xF0) >> 4))
    buffer.append(HexChars(byte & 0x0F))
  }
  buffer.toString
}


import scalaz._
import Scalaz._

// if we append two maps using then we get the following result
(Map("a" -> 1) |+| Map("a" -> 1)) == Map("a" -> 2)

// if we have a List[Map[String, Int]] and we fold the list using suml
// using the available monoids we get the following
List(Map("a" -> 1), Map("a" -> 1)).suml == Map("a" -> 2)

// so if we can just digest the text and create a map then we're all good
// if the key is unique like say a hash, and the value is a '1' then we
// can use the monoid strategy out of the box of scalaz

// what we can do is the following
// 1. the key is still a hash
// 2. The value is a pair
// 3. The pair contains the '1' as the key and a List of rownumbers as the value
List(Map("a" -> (1 -> List(1))), Map("a" -> (1 -> List(2))), Map("a" -> (1 -> List(3))), Map("b" -> (1 -> List(1)))).suml

// so what we must encode is the following:
// 1. A List[Map[String, (Int, List[Int])]
// 2. Each element is a Map
// 3. The map uses the hash as the key
// 4. The value points to a pair of (1, List[Int])
// 5. The value of the pair is a list of rownumber

val hashedLines: Map[String, (Int, List[Int])] = lines.zipWithIndex.map {
  case (str, index) => Map(hasher(str) -> (1 -> List(index)))
}.toList.suml

hashedLines.filter(_._2._1 == 2).flatMap(_._2._2).headOption.map(row => lines(row))
```

The result is:

```bash
res0: Option[String] = Some(Now that aint workin thats the way you do it)
```
b
## What is the difference between a var, a val, lazy val and def?
A __var__ is a variable. It’s a mutable reference to a value. Since it’s mutable, its value may change through the program lifetime,
making it a magnet for errors. Keep in mind that the variable type cannot change in Scala. You may say that a var behaves similarly
to Java variables.

```scala
scala> var x = 1
x: Int = 1

scala> x = 2
x: Int = 2

scala> x = "foo"
<console>:12: error: type mismatch;
 found   : String("foo")
 required: Int
       x = "foo"
           ^
```

A __val__ is a value. It’s an immutable reference, meaning that its value never changes. Once assigned it will always keep the same value.
It’s similar to constants in another languages.

```scala
scala> val x = 10
x: Int = 10

scala> x = 42
<console>:12: error: reassignment to val
       x = 42
```

A __def__ creates a method. It is evaluated on call.

```scala
scala> def x: Int = { println("evaluating..."); 42 }
x: Int

scala> x
evaluating...
res0: Int = 42

scala> x
evaluating...
res1: Int = 42
```

A __lazy val__ is like a val, but its value is only computed when needed. It’s specially useful to avoid heavy computations at the point of the expression,
but delaying it until the value is actually needed. It is mostly used when doing dependency injection like with the cake pattern (lite). Lazy vals aren't
cheap so they must be used sparingly in your code base:

```scala
scala> lazy val x: Int = { println("Evaluating..."); 42 }
x: Int = <lazy>

scala> x
Evaluating...
res2: Int = 42

scala> x
res3: Int = 42
```

## Lazy val deadlock problem
The lazy val deadlock problem is introduced when we have a cyclic dependency so a dependency that goes both ways when using lazy vals.
For example, say we have a Foo that depends on Bar and we have a Bar that depends on Foo so Foo <-> Bar:

```scala
case class Foo(b: Bar)
case class Bar(f: Foo)

lazy val foo: Foo = Foo(bar)
lazy val bar: Bar = Bar(foo)

defined class Foo
defined class Bar
foo: Foo = <lazy>
bar: Bar = <lazy>
```

Everything seems fine, until we access a member of either of them:

```scala
scala> foo.b
java.lang.StackOverflowError
 at ....
```

or

```scala
scala> bar.f
java.lang.StackOverflowError
  at ...
```

The best ways to fix this is by breaking the cyclic dependency, the Foo <-> Bar dependency. We can do that by introducing a third type,
lets call it Quz and put it between the two so Foo <- Quz -> Bar which means:

```scala
case class Foo()
case class Bar()
case class Quz(b: Bar, f: Foo)

lazy val quz: Quz = Quz(bar, foo)
lazy val foo: Foo = Foo()
lazy val bar: Bar = Bar()
```

Of course that means that we also have refactored the code in Foo and in Bar so that the logic that caused the cyclic dependency is now in Quz.
This of course means some refactoring and a change to our model. This goes to show how important it is to not start coding immediately and first
create a model...

Now its safe to call quz.b

```scala
scala> quz.b
res0: Bar = Bar()
```

Of course, cyclic dependencies are sometimes bad, like with object graphs as we saw here, and sometimes a good thing like when creating a virtual object graph
when referencing eg. entities by UUID when we want to reference an ID in another DDD bounded context for example.

## Methods aren't functions
Methods or 'def' aren't functions because methods aren't values. You can however construct a function that delegates to a method via 'eta-expansion' by
appending an underscore after a method name or by coersing the expression by using the type system (just stating that the value must be a function) eg:

```scala
// define a method
scala> def add(x: Int): Int = x + 1
add: (x: Int)Int

// using coersion
scala> val f: Int => Int = add
f: Int => Int = $$Lambda$1121/1506648430@5dd903be

// using eta-expansion
scala> val g = add _
g: Int => Int = $$Lambda$1122/1966787205@2e645fbd
```

Methods can also be automatically promoted to a function by just referencing the name of the method eg. in a processing pipeline like the following:

```scala
scala> List(1, 2, 3).map(add)
res0: List[Int] = List(2, 3, 4)
```

## What is the difference between a trait and an abstract class?
The first difference is that a class can only extend one other class, but an unlimited number of traits:

```scala
scala> class Foo
defined class Foo

scala> class Bar
defined class Bar

scala> class Baz extends Foo with Bar
<console>:13: error: class Bar needs to be a trait to be mixed in
       class Baz extends Foo with Bar
```

While traits only support type parameters, abstract classes can support both type parameters and constructor parameters:

```scala
scala> trait Box[A]
defined trait Box

scala> trait Box[A](a: A)
<console>:1: error: traits or objects may not have parameters
trait Box[A](a: A)

scala> abstract class Box[A](a: A)
defined class Box
```

Also, abstract classes are interoperable with Java, while traits are only interoperable with Java if they do not contain any implementation.
IMHO, for most projects this is a non-issue as the dependency mostly goes from Scala -> Java, ie. we are using Java libraries; not the other
way around. If you do, please move from Java to Scala and start having more fun!

## What is the difference between an object and a class?
An object is a singleton instance of a class. It cannot be instantiated by the developer. The Scala runtime handles
all the necessary initialization issues of the singleton issue of the class.

If an object has the same name that a class, and is in the same scala file, then the object is called a companion object.

```scala
scala> :paste
// Entering paste mode (ctrl-D to finish)

class Foo
object Foo {
def apply(): Foo = new Foo
}

// Exiting paste mode, now interpreting.

defined class Foo
defined object Foo

scala> Foo()
res5: Foo = Foo@b3fc6d8
```

## What is a case class?
A case class is syntactic sugar for a class that is immutable and decomposable through pattern matching.
This is because the companion object of a case class has an apply and unapply method. Being decomposable
means it is possible to extract its constructors parameters in the pattern matching.

Case classes contain a companion object which holds the apply method. This fact makes possible to instantiate
a case class without the new keyword. They also come with some helper methods like the .copy method, that eases
the creation of a slightly changed copy from the original.

Finally, case classes are compared by structural equality instead of being compared by reference, i.e., they come with
a method which compares two case classes by their values/fields, instead of comparing just the references.

Case classes are specially useful to be used as Value Objects, Data Transfer Object definitions and so on.

```scala
scala> import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._

scala> Person("dennis", 42).asJson.noSpaces
res0: String = {"name":"dennis","age":42}
```

Or a more generic representation of the case class:

```scala
scala> import shapeless._

scala> Generic[Person].to(Person("dennis", 42))
res1: String :: Int :: shapeless.HNil = dennis :: 42 :: HNil
```

Or just do some pattern matching:

```scala
scala> Person("dennis", 42) match {
     | case Person(fn, age) => s"Hi $fn, you are $age years old!"
     | }
res2: String = Hi dennis, you are 42 years old!
```

## What is the difference between a Java future and a Scala future?
The Scala implementation is asynchronous without blocking, while in Java you can’t get the future value without blocking.
Scala provides an API to manipulate the future as a monad or by attaching callbacks for completion. Unless you decide to use the Await,
you won’t block your program using Scala futures.

```scala
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent._

val result: Future[Int] = for {
  x <- Future(1)
  y <- Future(1 + x)
  z <- Future(1 + x + y)
} yield x + y + z

Await.result(result, 1.second)
```

## What is the difference between unapply and apply, when would you use them?
The method 'unapply' is a method that needs to be implemented by an object in order for it to be an extractor.
Extractors are used in pattern matching to access an object constructor parameters. It’s the opposite of a constructor.

For example:

```scala
object Even {
  def unapply(arg: Int): Option[Int] =
    Option(arg).find(_ % 2 == 0)
}

object Odd {
  def unapply(arg: Int): Option[Int] =
    Option(arg).find(_ % 2 != 0)
}

(1 to 10).map {
  case Even(x) => s"$x is even"
  case Odd(x) => s"$x is odd"
}.foreach(println)
```

The apply method is a special method that allows you to write someObject(params) instead of someObject.apply(params). This usage is common in case classes,
which contain a companion object with the apply method that allows the nice syntax to instantiate a new object without the new keyword:

```scala
case class Person(id: Long, name: String, age: Int)

object PersonRepository {
  def apply(): PersonRepository = new InMemoryPersonRepository(Map.empty)
}
trait PersonRepository {
  def getById(id: Long): Option[Person]
}
class InMemoryPersonRepository(repo: Map[Long, Person]) extends PersonRepository {
  override def getById(id: Long): Option[Person] = repo.get(id)
}
PersonRepository().getById(1L)
```

## What is a companion object?
If an object has the same name that a class, and is in the same scala file then the object is called a companion object.
A companion object has access to methods of private visibility of the class, and the class also has access to private methods
of the object. Doing the comparison with Java, companion objects hold the “static methods” of a class.

Note that the companion object has to be defined in the same source file that the class.

## What is the difference between the following terms and types in Scala: Nil, Null, None, Nothing?
The __None__ is the empty representation of the Option Monad which is represented as an Algebraic Data Type (ADT) Option[A] is either a Some[A] or a None.

Null is a Scala trait, where null is its only instance. The null value comes from Java and it’s an instance of any object, i.e., it is a subtype of all
reference types, but not of value types. It exists so that reference types can be assigned null and value types (like Int or Long) can’t.

Nothing is another Scala trait. It’s a subtype of __any other type__, and it has no subtypes. It exists due to the complex type system Scala has.
It has __zero__ instances. It’s the return type of a method that never returns normally, for instance, a method that always throws an exception.
The reason Scala has a bottom type is tied to its ability to express variance in type parameters. For example, the None is of type Option[Nothing] and the
'.get' method of None always throws a 'NoSuchElementException'.

Finally, __Nil__ represents an empty List of anything of size zero and is an object, so there is a single instance. Nil is of type List[Nothing].
If you call '.head' or '.tail' on the Nil object, the methods throw an exception.

## What is Unit?
Unit is a __type__ which represents the absence of value, just like Java void. It is a subtype of 'scala.AnyVal'. There is only one value of type Unit,
represented by '()', and it is not represented by any object in the underlying runtime system.

## Resources
- [Scala Interview Questions](http://pedrorijo.com/blog/scala-interview-questions/)

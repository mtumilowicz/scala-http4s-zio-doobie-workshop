# scala-http4s-zio-fs2-workshop

* https://medium.com/@ghostdogpr/wrapping-impure-code-with-zio-9265c219e2e
* https://scalac.io/blog/introduction-to-programming-with-zio-functional-effects/
* https://degoes.net/
* https://scalac.io/wp-content/uploads/2021/04/Scalac_Ebook_Mastering_modularity_in_ZIO_updated..pdf
* [A Tour of ZIO](https://www.youtube.com/watch?v=TWdC7DhvD8M)
* [I Can Has? (And So Can You!) — Exploring ZIO's Has Type](https://www.youtube.com/watch?v=1e0C0jUzup4)
* [Structuring Services in Scala with ZIO and ZLayer](https://www.youtube.com/watch?v=PaogLRrYo64)
* [ZIO WORLD - ZLayer by Kit Langton](https://www.youtube.com/watch?v=B3bAcU2-TGI)
* [Getting started with ZIO](https://www.youtube.com/watch?v=6A1SA5Be9qw)
* [ZIO WORLD - ZIO Runtime System by John A. De Goes](https://www.youtube.com/watch?v=OFFrw5aJzG4)
* [1st Zymposium - Building an App with ZIO](https://www.youtube.com/watch?v=XUwynbWUlhg)
* [The ZIO of the Future by John De Goes](https://www.youtube.com/watch?v=u3pgyEiu9eU)
* [ZIO WORLD - ZHub by Adam Fraser](https://www.youtube.com/watch?v=v7Ontn7jZt8)
* [Zymposium - Optics](https://www.youtube.com/watch?v=-km5ohYhJa4)
* [Zymposium — Type-Level Sets](https://www.youtube.com/watch?v=N_it0qwk7_U)
* [Zymposium — Path Dependent Types](https://www.youtube.com/watch?v=w2rcHCqdn-o)
* [ZIO from Scratch — Part 1](https://www.youtube.com/watch?v=wsTIcHxJMeQ)
* [ZIO from Scratch — Part 2](https://www.youtube.com/watch?v=g8Tuqldu2AE)
* [ZIO from Scratch — Part 3](https://www.youtube.com/watch?v=0IU9mGO_9Rw)
* [ZIO from Scratch — Part 4](https://www.youtube.com/watch?v=95Vk-vVgnOg)
* [ZIO from Scratch — Part 5](https://www.youtube.com/watch?v=uzDs4X42w2k)
* https://blog.rockthejvm.com/structuring-services-with-zio-zlayer/
* https://blog.softwaremill.com/zio-environment-meets-constructor-based-dependency-injection-6a13de6e000
* https://alvinalexander.com/scala/what-effects-effectful-mean-in-functional-programming/

* If you’re using ZIO, you should not throw exceptions within IO.succeed or map/flatMap, because it will result in the fiber being killed. Instead, you should use IO.effect: this effect constructor will catch exceptions for you and return an IO[Throwable, A] (aka Task)
    * You can then use mapError, catchAll or other combinators to deal with this exception.
* blocks the current thread until completion? If you run it within a regular IO, you will block a thread from your application’s main thread pool and potentially cause thread starvation
    * ZIO has a solution for that, which is to wrap your code within effectBlocking
    * The return type is ZIO[Blocking, Throwable, A], which means that it requires a “blocking environment” (= the thread pool to use) and that it catches exceptions
    * By the way, never wrap Thread.sleep, use non-blocking IO.sleep instead.
* You can also use effectAsync in combination with a Scala Future, but there’s even an easier way for that: IO.fromFuture
* ZIO has a data type that encapsulate initialization and closing logic: Managed
    * Its constructor make takes 2 functions: one to create the object, and one to release it
* instead of writing functions that interact with the outside world, we write functions that describe interactions with the outside world, which are executed only at a specific point in our application, (usually called the end of the world) for example the main function
    * What is this aforementioned end of the world? Well, the end of the world is simply a specific point in our application where the functional world ends, and where the descriptions of interactions with the outside world are being run, usually as late as possible, preferably at the very edge of our program which is its main function
## zio
* just by looking at the signature of a function, we can tell:
  If it has external dependencies.
  If it can fail or not, and also with what type of errors it can fail.
  If it can finish successfully or not, and also what the type of data is that returns when finishing.
* ZIO gives us the superpowers to work with asynchronous and concurrent programming, using a fiber-based model, which is much more efficient than a thread-based model.
* ZIO [-R, +E, +A]
    * R => Either[E, A]
    * Needs a context of type R to run (this context can be anything: a connection to a database, a REST client , a configuration object, etc.)
    * It may fail with an error of type E or it may complete successfully, returning a value of type A.
    * Common aliases for the ZIO data type
        * Task[+A] = ZIO[Any, Throwable, A]
            * Doesn’t require an environment to run
            * Can fail with a Throwable
            * Can succeed with an A
        * UIO[+A] = ZIO[Any, Nothing, A]
        * RIO[-R, +A] = ZIO[R, Throwable, A]
        * IO[+E, +A] = ZIO[Any, E, A]
        * URIO[-R, +A] = ZIO[R, Nothing, A]
* The zio.App trait requires that we implement a run method, which is the application’s entry point, as you can see this method receives a list of arguments like any normal Scala application, and returns a ZIO functional effect, which we already know is only a description of what our application should do
* another operator to combine two effects sequentially, and it is the <*> operator (which by the way is equivalent to the ZIO#zip method)
    * (putStrLn(message) <*> getStrLn).map(_._2)
    * This operator, like ZIO#flatMap, combines the results of two effects, with the difference that the second effect does not need the result of the first to be executed
    * The *> operator (equivalent to the ZIO#zipRight method) does exactly what we did in the previous version, but in a much more condensed way.
* ZIO.fromOption method, which returns an effect that ends successfully if the given Option is a Some, and an effect that fails if the given Option is None
* Then we are using a new <> operator (which is equivalent to the ZIO#orElse method), which also allows us to combine two effects sequentially, but in a somewhat different way than <*>
    * If the first effect is successful (in this case: if the player’s name is valid), the second effect is not executed.
    * If the first effect fails (in this case: if the player’s name is invalid), the second effect is executed (in this case: an error message is displayed and then getName is called again).
* .orDieWith(_ => new Error("Boom!"))
    * using the ZIO#orDieWith method, which returns a new effect that cannot fail and, if it does fail, that would mean that there is some serious defect in our application (for example that some of our predefined words are empty when they should not be) and therefore it should fail immediately with the provided exception
    * ZIO#orDie method, which returns an effect that never fails (we already know that if there were any failure, it would actually be a defect and our application should fail immediately).
    * By the way, ZIO#orDie is very similar to ZIO#orDieWith, but it can only be used with effects that fail with a Throwable.
* why we need to call the ZIO#exitCode method, which returns an effect of the type ZIO[Console With Random, Nothing, ExitCode]as follows:

  If the original effect ends successfully, ExitCode.success is returned.
  If the original effect fails, the error is printed by console and ExitCode.failure is returned
* type ZEnv = Clock with Console with System with Random with Blocking
    * We can see that ZEnv is just an alias that encompasses all of the standard modules provided by ZIO
* Has[A] represents a dependency on a service A.
* Has[A] and a Has[B] can be combined horizontally with the ++ operator for obtaining
  a Has[A] with Has[B], representing a dependency on two services (if you are
  wondering what combined horizontally means, don’t worry too much because the idea
  will become clearer when we reimplement the Tic-Tac-Toe application)
    * The true power of the Has data type is that it is backed by an heterogeneous map from
      service type to service implementation, so when you combine Has[A] with Has[B],
      you can easily get access to the A and B services implementations.
* ZLayer[-RIn, +E, +ROut <: Has[_]]
    *  contains a description to build an
      environment of type ROut, starting from a value RIn, possibly producing an error E during
      creation
    * Moreover, two layers can be combined in two fundamental ways:
      ● Horizontally: To build a layer that has the requirements and provides the capabilities of
      both layers, we use the ++ operator.
      ● Vertically: In this case, the output of one layer is used as input for the subsequent layer,
      resulting in a layer with the requirement of the first and the output of the second layer.
      We use the >>> operator for this.
   * aliases
    * TaskLayer[+ROut]
    * ULayer[+ROut]
    * RLayer[-RIn, +ROut]
    * Layer[+E, +ROut]
    * URLayer[-RIn, +ROut]
* ZLayer.fromEffect: Allows to lift a ZIO effect to a ZLayer. This is especially
  handy when you want to define a ZLayer whose creation depends on an environment
  and/or can fail. You can also use the equivalent operator in the ZIO data type:
  ZIO#toLayer.

## testing
* https://zio.dev/version-1.x/howto/mock-services



* that run loop maybe could be refactored using GADT (generalized algebraic data types)
* ZIO[-R, +E, +A] is something function: R => Either[E, A]
  * lazy
  * like R => Either[E, A]
    * def either: ZIO[R, Nothing, Either[E, A]]
    * def absolve(zio: ZIO[R, E, Either[E, A]]): ZIO[R, E, A]
  * doesn't produce effects - it describe effects
    * side effect: function doing something other than just returning a value
      * val sayHello: Unit = println("Hello!")
        * from value perspective, any different than val unit = ()
        * all it does is side effect
    * effect system: is how we manage this side effect by describing them not doing them
    * effects are good, side-effects are bugs
  * effects are descriptions so we can run them again
    * but if that function returns F[A] instead, that result has not already been fully evaluated, the A is still inside F[A] waiting to be evaluated
  * a better future (referentially transparent)
  * type aliases
    * Task[+A] = ZIO[Any, Throwable, A] // need anything
    * UIO[+A] = ZIO[Any, Nothing, A] // cannot fail
    * RIO[-R, +A] = ZIO[R, Throwable, A]
    * IO[+E, +A] = ZIO[Any, E, A]
      * in ZIO: def provider(r: R): IO[E, A]
        * give dependency, any you don't need one anymore (therefore IO)
    * URIO[-R, +A] = ZIO[R, Nothing, A]
  * use ZIO for dependency injection
  * use IO and UIO for typed errors

## error handling
* zio.catchAllCause()
* Cause
* sealed trait ConfigError
  * final case class IOError(message: String) extends ConfigError
  * final case class ParseError(message: String) extends ConfigError
* die error channel

* Ref - something similar to java atomic, better than var
* STM
  * object StmDiningPhilosophers extends App
    * https://gist.github.com/calvinlfer/09fea316613c6ace32ae79d62b1acc71#file-workshop-scala-L962
* program example
  * val program: URIO[Int, Int] = ZIO.environment[Int].map(_ * 2)
  * program.provide(12).flatMap(r => UIO(println(result))).exitCode
* Has // implementation detail
  * i = Has[Int] = Has(12)
  * s = Has[String] = Has("abc")
  * combined: Has[Int] with Has[String] = i ++ s
  * we can use it as a map
    * like a map where the keys are types and values are implementations of those types
  * val int: Int = i.get
  * val str: String = s.get
  * combined.get[String] // like a map but with compile type check if we have something there
  * motivation
    * program1: ZIO[Int, Nothing, Int]
    * program2: ZIO[String, Nothing, String]
    * program3: ZIO[String with Int, Nothing, (Int, String)] = program1 zip program2
    * program3.provide(12).flatMap(r => UIO(println(result))).exitCode // not works
    * replace types with Has[X]
      * replace val program: URIO[Int, Int] = ZIO.environment[Int].map(_ * 2)
        * with URIO[Has[Int], Int] = ZIO.service[Int].map(_ * 2)
      * program3: ZIO[Has[String] with Has[Int], Nothing, (Int, String)] = program1 zip program2
    * and here where layers come in
      * val stringLayer: ULayer[Has[String]] = ZLayer.succeed("STR")
      * val intLayer = ZLayer.succeed(1)
      * val combinedLayer = stringLayer ++ intLayer
    * and there program3.provideLayer(combinedLayer)
* ZLayer
  * horizontal composition
    * ZL[I1, E1, O1] ++ ZL[I2, E2, O2] => ZL[I1 with I2, super(E1, E2), O1 with O2]
  * vertical composition
  object UserSubscription {
  // service definition as a class
  class Service(notifier: UserEmailer.Service, userModel: UserDb.Service) {
    def subscribe(u: User): Task[User] = {
      for {
        _ <- userModel.insert(u)
        _ <- notifier.notify(u, s"Welcome, ${u.name}! Here are some ZIO articles for you here at Rock the JVM.")
      } yield u
    }
  }
}

  val live: ZLayer[UserEmailerEnv with UserDbEnv, Nothing, UserSubscriptionEnv] =
  ZLayer.fromServices[UserEmailer.Service, UserDb.Service, UserSubscription.Service]( emailer, db =>
  new Service(emailer, db)
  )
  * and then
  val userBackendLayer: ZLayer[Any, Nothing, UserDbEnv with UserEmailerEnv] =
  UserDb.live ++ UserEmailer.live
  * val userSubscriptionLayer: ZLayer[Any, Throwable, UserSubscriptionEnv] =
    userBackendLayer >>> UserSubscription.live
* ZLayer
  * composing internal dependency graph (services, repositories)
  * horizontal composition (++)
    * get in parallel
  * vertical composition (>>>)
    * released in reverse order
  * service pattern 1.0
    * object Analytics
      * type Analytics = Has[Service]
      * trait Service { def track(event: String): UIO[Unit] }
      * def track(event: String): URIO[Analytics, Unit] = ZIO.accessM(_.get.track(event))
      * val live: ULayer[Analytics] = ???
    * contains
      * an object
      * a nested service trait
      * a type alias
      * accessor methods
      * layers
  * service pattern 2.0
    * trait Analytics { def track(event: String): UIO[Unit] }
    * object Analytics
      * def track(event: ...): URIO[Has[Analytics], Unit] = ZIO.serviceWith(_.track(event))
      * val live: ULayer[Has[Analytics]] = ???
  * defining ZLayers 2.0
    * val live: URLayer[Has[Console], Has[Analytics]] =
      * for
        * console <- ZIO.service[Console]
      * yield AnalyticsLive(console)
      * }.toLayer
    * case class AnalyticsLive(console: Console) extends Analytics {
      * def track(event: String): UIO[Unit] = console.putStrLn(s"EVENT: $event")
* effect value is actually a tree
  ```
  val effect =
    ZIO(callApi(url)).flatMap { result =>
      saveCache(result)
    }.eventually // if fail retry
  ```
  translated into tree (blueprint)
  ```
  lazy val effect =
    Fold(
      FlatMap( // common operators as a case classes
        EffectPartial(() => callApi(url)),
         result => saveCache(res lt)
        ),
        error => effect,
        success => EffectTotal(() => success)
      )
    )
  ```
  and FlatMap is something like
  ```
  case class FlatMap[A, B](zio: ZIO[A], f: A => ZIO[B]) extends ZIO[B]
    override def fun(callback: B => Unit): Unit =
      zio.run { a =>
        f(a).run { b // or f(a).run(callback) directly
          callback(b)
        }
      }
  ```
  that FlatMap implementation will blow up the stack, so we have to convert java
  stack into local stack (scala Stack)
  * ZIO.FlatMap(zio, cont) => stack.push(cont); currentZIO = zio
* zio runtime system
  * unsafeRun(effect)
  * (R, ZIO[R, E, A]) -> zio runtime system -> Either[E,A]
  * responsibilities
    * execute every step of the blueprint
    * handle unexpected errors
    * spawn concurrent fibers
    * cooperatively yield to other fibers
    * capture execution & stack traces
    * ensure finalizers are run appropriately
    * handle asynchronous callbacks

## concurrency
* https://blog.rockthejvm.com/zio-fibers/
* https://blog.rockthejvm.com/cats-effect-fibers/
* in ZIO world, Fiber is the closest analogy to Future
  * if we see fiber it is probably doing something or already evaluated
  * two core methods are: join and interrupt
    * no start method, as soon as fiber is created it is started as well
* in ZIO: def fork: ZIO[R, Nothing, Fiber[E, A]]
* in Fiber: def join: ZIO[Any, E, A]
  * fork means run in the background; join means wait for a result
* semantically block but never block underlying threads
* ZIO.foreachPar(ids)(getUserById)
  * automatically interrupt others if one fails
* getDataFromEastCoast.race(getDataFromWestCoast)
  * returns first
  * automatically interrupt the loser
* provided primitives
  * Ref - functional equivalent of atomic ref
  * Promise - single value communication
  * Queue - multiple value communication
  * Semaphore - control level of concurrency
  * Schedule - manage repeats and retries

* fibers
  * if it is not doing active work and can't do active work - will be garbage collected
  * you don't have to take care of explicitly shutting them down
  * it’s up to the ZIO runtime to schedule these fibers for execution (on the internal JVM thread pool)
  * Moreover, ZIO executes fibers using an Executor, which is a sort of abstraction over a thread pool
  * ZIO fibers don’t block any thread during the waiting associated with the call of the join method
  * If the fiber already succeeded with its value when interrupted, then ZIO returns an instance of Exit.Success[A], an Exit.Failure[Cause.Interrupt] otherwise
    * Unlike interrupting a thread, interrupting a fiber is an easy operation
    * Interrupting a fiber simply tells the Executor that the fiber must not be scheduled anymore
    * As the name suggests, an uninterruptible fiber will execute till the end even if it receives an interrupt signal.
  * Notice that we’re measuring threads versus CPU cores and fibers versus GB of heap
  * But since creating the fiber itself — and running the IO on a separate thread — is an effect, the returned fiber is wrapped in another IO instance
* json
  * https://github.com/zio/zio-json
* it's composable
  ```
  val managedData = Managed.make(open(url))(close(_))

  managedData.use { data =>
    searchBreadth(data)
  }
  ```
  support multiple urls
  ```
  ZIO.foreach(urls) { url => // parallel: ZIO.foreachPar(urls); with limit: ZIO.foreachParN(10)(urls)
    val managedData = Managed.make(open(url))(close(_))

    managedData.use { data =>
      searchBreadth(data)
    }
  }
  ```
  retry: at most 100 times
  ```
  val policy = Schedule.recurs(100) // exponential backoff: && Schedule.exponential(10.millis)

  ZIO.foreach(urls) { url => // parallel: ZIO.foreachPar(urls); with limit: ZIO.foreachParN(10)(urls)
    val managedData = Managed.make(open(url))(close(_))

    val robustData = managedData.retry(policy) // timeout: .timeoutFail(30.seconds)

    robustData.use { data =>
      searchBreadth(data) // use other search and take faster: .race(searchDepth(data))
    }
  } // timeout all: .timeout(10.seconds)
  ```

* Queue
  * naive solution
    * immutable queue in atomic reference
      * supports only one operation - all other have to retry
  * ring buffer
    * granularity per index: one producer could write to index idx1 and other to idx1 + 1
  * queue doesn't solve the problem of broadcasting
    * naive solution: maintain queue for each consumer
    * ring buffer: for each value keep track of how many consumers have to read that value
      * consumers just go in their speed
      * if the last consumer reads value value is cleared and give producers space for producing the next value

* managed resources
  * connection: ZManaged[Any, Throwable, Connection]

## optics
* ref: Ref[Map[String, Either[String, Chunk[Int]]]]
* ref.key("key").right.at(0).update(_ + 1)
* introduction
  ```
  case class Developer(name: String, manager: Manager)
  case class Lens[Whole, Piece](get: Whole => Piece, set: Piece => Whole => Whole) {

    def update(whole: Whole)(f: Piece => Piece): Whole = { self =>
      val oldPiece = get(whole)
      set(f(oldPiece))(whole)
    }

    def >>>[SubPiece](that: Lens[Piece, SubPiece]): Lens[Whole, SubPiece] =
      Lens(
        whole => that.get(self.get(whole)),
        subpiece => whole => self.set(that.set(subpiece)(self.get(whole)))(whole)
  }

  val managerLens =
  Lens[Developer, Manager](
    developer => developer.manager,
    manager => developer => developer.copy(manager = manager))
  ```
* prisms (type MyType = Either[Int, String])
  * try to modify right side of either
  * case class Prism[Whole, Piece](get: Whole => Option[Piece], set: Piece => Whole)
* case class Traversal[Whole, Piece](get: Whole => List[Price], set: List[Price] => Whole)
* seems conceptually very similar
  * case class Optic[GetWhole, SetWholeBefore, SetPiece, GetError, SetError, GetPiece, SetWholeAfter](
    get: GetWhole => Either[GetError, GetPiece]
    set: SetPiece => SetWholeBefore => SetWholeAfter
  )
  * type Lens[Whole, Piece] = Optic[Whole, Whole, Piece, Nothing, Nothing, Piece, Whole]

* type level sets
  * https://jto.github.io/articles/getting-started-with-shapeless/
  * val demo = 42 :: "Hello" :: User("Julien") :: HNil
  * val s = demo.select[String] // returns "Hello"
  * demo.select[List[Int]] // Compilation error. demo does not contain a List[Int]
  * https://gist.github.com/adamgfraser/512a640dcda970e2fffb597b756c744d#file-hset-scala

* path dependent types
  ```
  val zio1: ZIO[Any, Nothing, Int] = ZIO.succeed(1)
  val zio2: ZIO[Any, Nothing, Int] = ZIO.succeed(2)
  val zio3: ZIO[Any, Nothing, Int] = ZIO.succeed(3)

  val zio4: ZIO[Any, Nothing, (Int, Int, Int)] = zio1 <*> zio2 <*> zio3 // no tuples nesting
  ```
  * def <*>...(that: ZIO[R1, E1, B])(implicit zippable: Zippable[A, b]): ZIO[R, E, zippable.Out]
    * zippable.Out // output type is not always a tuple, it depends on the value
    * we can return different types based on the types that coming in
    * so if left: ZIO[R E (A, B)] and right ZIO[R E C] don't give me ((A, B), C) give me (A, B, C)
  * def zip(left, right): ZIO[R, E, (A, B)] - it doesn't depend on anything it's always tuple
  * example
    ```
    trait Box[+A] { def value: A }
    trait PathDependentBox { type A; def value: A } // A depend on particular box, not a box in general

    object PathDependentBox { // val box1: PathDependentBox = PathDependentBox(1) // there is no type
      def apply[A0](a: A0): Box =
        new Box {
          type A = A0
          val value = a // box1.value + box1.value - compiler error
        }
    }

    trait CountDownLatch {
      type S // you want to keep type from consumer (S - state)
      protected def updateState: S => S
      def countDown: UIO[Unit]
      def onDone: UIO[Unit]
    }
    ```
    * https://medium.com/virtuslab/path-dependent-types-9f2d7927c1fa

* map(_ => 5) is a method: .as(5)

* effectAsync
  * if you’re using ZIO, you should not throw exceptions within IO.succeed or map/flatMap, because it will result in the fiber being killed
  * Instead, you should use IO.effect: this effect constructor will catch exceptions for you and return an IO[Throwable, A]
  * You can then use mapError, catchAll or other combinators to deal with this exception.
  * It is a good rule of thumb to use this whenever you’re not sure if the code you’re calling might throw exceptions (or if you’re sure it will, of course).
  * Now, how about some legacy code which not only throws exceptions, but also blocks the current thread until completion
    * If you run it within a regular IO, you will block a thread from your application’s main thread pool and potentially cause thread starvation
    * Instead, it is better to run such task inside another thread pool dedicated to blocking tasks
    * ZIO has a solution for that, which is to wrap your code within effectBlocking
  * CompletableFuture, which itself has a handle method taking a callback that will be executed once the API calls returns
    * How to deal with such a function with ZIO? By wrapping it with effectAsync
    * It gives you a function that you can call when the callback is triggered, and that will complete the effect with either a failure or a value
  * def effectAsync[R, E, A]( register: (ZIO[R, E, A] => Unit) => Any): ZIO[R, E, A]
    * def succeed[A](a: => A): ZIO[Any, Nothing, A]

## errors
* Cause has several variations which encode all the cases:
  * Fail[+E](value: E) contains the cause of expected failure of type E.
    * expected errors
    * errors you would potentially want to recover from
  * Die(value: Throwable) contains the cause of a defect or in other words, an unexpected failure of type Throwable. If we have a bug in our code and something throws an unexpected exception, that information would be described inside a Die.
    * unexpected errors
    * errors you can't recover from in a sensible way (because you didn't expect them)

## environment
* suppose we don't have Has
  * zio = ZIO[Int with String, ...]
  * zio.provide(12) // compile type error
  * if we define trait HasInt { value: Int }
    * we could do ZIO[HasInt with HasString]
    * and then zio.provide( new HasInt with HasString { ... } )

## zio kafka

## dependency injection
In a type-safe, resource-safe, potentially concurrent way, with principled error handling, without reflection or classpath scanning.
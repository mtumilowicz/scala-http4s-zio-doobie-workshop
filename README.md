# scala-http4s-zio-fs2-workshop

* references
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
    * https://zio.dev/version-1.x/overview/
    * https://aleksandarskrbic.github.io/functional-effects-with-zio/

## preface

## introduction
* functional programs do not interact with the external world directly
    * it involves partiality, non-determinism and side-effects
    * instead, describe (or model) interaction with the real world
        * by introducing appropriate data structures
    * functions that describe interactions with the outside world are executed only at a specific point in our application
        * called the end of the (pure functional) world
            * example: the main function
        * rest of the application can be purely functional
* effects
    * immutable data structures that model procedural effects
    * used to model some common operations or sequence of operations, like database interaction, RPC calls,
    WebSocket connections, etc.
    * side effect context: function doing something other than just returning a value
        * effects are good, side-effects are bugs
        * example: `val sayHello: Unit = println("Hello!")`
            * from value perspective, any different than `val unit = ()`
                * all it does is side effect
                * not referentially transparent
    * effect system: is how we manage side effects - by describing them not doing them
    * effects are descriptions so we can run them again

## zio
* is a library for asynchronous and concurrent programming that is based on pure functional programming
* provides everything that is necessary for building any modern, highly-concurrent application
### general
* basic building block: `ZIO[-R, +E, +A]`
    * something like `R => Either[E, A]`
        * isomorphism
            * `def either: ZIO[R, Nothing, Either[E, A]]`
            * `def absolve(zio: ZIO[R, E, Either[E, A]]): ZIO[R, E, A]`
    * `R` stands for context needed to run
        * example
            * a connection to a database
            * a REST client
            * a configuration object
            * other service
    * note that just by looking at the signature of a function, we can tell:
        * if it has external dependencies
        * if it can fail or not
            * what type of errors it can fail
        * if it can finish successfully or not
            * what the type of data is that returns when finishing
    * a better `Future`
        * referentially transparent
            * doesn't produce effects - it describe effects
        * lazy
            * when created - `Future` is already running
                * example: `Future { Thread.sleep(2_000); 1 }`
                * therefore it needs execution context when creating
            * effect value is just a tree
                ```
                val effect =
                  ZIO(callApi(url)).flatMap { result =>
                    saveCache(result)
                  }.eventually // if fail retry
                ```
                is translated into tree (blueprint)
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
    * resource-safe equivalent: `ZManaged[-R, +E, +A]`
        * its constructor `make` takes 2 functions: one to create the object, and one to release it
    * is composable
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
    * type aliases
        * `Task[+A] = ZIO[Any, Throwable, A]` // need anything
        * `UIO[+A] = ZIO[Any, Nothing, A]` // cannot fail
        * `RIO[-R, +A] = ZIO[R, Throwable, A]`
        * `IO[+E, +A] = ZIO[Any, E, A]`
        * `URIO[-R, +A] = ZIO[R, Nothing, A]`

* `zio.App`
    * entry point for a purely-functional application on the JVM
    * zio runtime system
        * `def unsafeRun[E, A](zio: => ZIO[R, E, A]): A`
        * responsibilities
            * execute every step of the blueprint
            * handle unexpected errors
            * spawn concurrent fibers
            * cooperatively yield to other fibers
            * capture execution & stack traces
            * ensure finalizers are run appropriately
            * handle asynchronous callbacks
    * method to override: `def run(args: List[String]): URIO[ZEnv, ExitCode]`
        * errors have to be fully handled
        * use `ZIO.exitCode` to map ZIO into `URIO[ZEnv, ExitCode]`
            * if the original effect ends successfully: `ExitCode.success` is returned
            * if the original effect fails
                * the error is printed by console
                * and `ExitCode.failure` is returned
    * example
        ```
         object MyApp extends App {

           final def run(args: List[String]) =
             program.exitCode

           def program =
             for {
               _ <- putStrLn("Hello! What is your name?")
               n <- getStrLn
               _ <- putStrLn("Hello, " + n + ", good to meet you!")
             } yield ()
         }
        ```
* useful alias operators
    * `.as(...)` is equivalent of `map(_ => ...)`
    * `<*>` is equivalent to the `zip`
    * `*>` is equivalent of `flatMap(_ => ...)`
    * `>>=` is equivalent of `flatMap`
    * `<>` is equivalent of `orElse`
        * if the first effect is successful - the second effect is not executed
        * If the first effect fails (in this case: if the player’s name is invalid), the second effect is executed (in this case: an error message is displayed and then getName is called again).
* monad transformers equivalent
    * for `Option`
        *
    * for `Either`
    * example
        ```
        val maybeId: IO[Option[Nothing], String] = ZIO.fromOption(Some("abc123"))
        def getUser(userId: String): IO[Throwable, Option[User]] = ???
        def getTeam(teamId: String): IO[Throwable, Team] = ???


        val result: IO[Throwable, Option[(User, Team)]] = (for {
          id   <- maybeId
          user <- getUser(id).some // ZIO[R, E, Option[B]] -> ZIO[R, Option[E], B]
          team <- getTeam(user.teamId).asSomeError // ZIO[R, E, B] -> ZIO[R, Option[E], B]
        } yield (user, team)).optional // ZIO[R, Option[E], B] -> ZIO[R, E, Option[B]]
        ```

### errors
* `IO[E, A]` is polymorphic in values of type E and we can work with any error type
    * there is a lot of information that is not inside an arbitrary `E` value
        * unexpected exceptions or defects
        * stack and execution traces
        * cause of fiber interruptions
        * etc
* `Cause[E]` is a description of a full story of failure, which is included in an `Exit.Failure`
    * has several variations
        * `Fail[+E](value: E)`
            * expected errors
            * errors you would potentially want to recover from
        * `Die(value: Throwable)`
            * unexpected errors (defects)
            * errors you can't recover from in a sensible way (because you didn't expect them)
        * others
* handling
    * `catchAll`
    * `catchSome`
* you should not throw exceptions within `IO.succeed` or `map/flatMap` or any other non-effectful method
    * it will result in the fiber being killed
    * instead
        * `def effect[A](effect: => A): Task[A]`
            * imports a synchronous side-effect into a pure ZIO value
            * translates any thrown exceptions into typed failed effects (`ZIO.fail`)
        * `def mapEffect[B](f: A => B)(implicit ev: E <:< Throwable): RIO[R, B]`
            * translates any thrown exceptions into typed failed effects (`ZIO.fail`)
* `.orDie`
    * returns an effect that never fails
        * if there were any failure, it would actually be a defect and our application should fail immediately
    * `.orDieWith(_ => new Error("Boom!"))`
        * returns a new effect that cannot fail and, if it does fail, that would mean that there is some serious defect

### concurrency
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
* blocks the current thread until completion? If you run it within a regular IO, you will block a thread from your application’s main thread pool and potentially cause thread starvation
    * ZIO has a solution for that, which is to wrap your code within effectBlocking
    * The return type is ZIO[Blocking, Throwable, A], which means that it requires a “blocking environment” (= the thread pool to use) and that it catches exceptions
    * By the way, never wrap Thread.sleep, use non-blocking IO.sleep instead.
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

### dependency injection
* In a type-safe, resource-safe, potentially concurrent way, with principled error handling, without reflection or classpath scanning.
* suppose we don't have Has
  * zio = ZIO[Int with String, ...]
  * zio.provide(12) // compile type error
  * if we define trait HasInt { value: Int }
    * we could do ZIO[HasInt with HasString]
    * and then zio.provide( new HasInt with HasString { ... } )
* environment
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
### testing
* https://zio.dev/version-1.x/howto/mock-services

## http4s

## fs2

## doobie
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
    * https://blog.rockthejvm.com/zio-fibers/
    * https://blog.rockthejvm.com/cats-effect-fibers/

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
* fiber
    * using fibers directly can be tricky
        * use higher-level methods, such as `raceWith`, `zipPar`, and so forth
    * usage
        * example
            ```
             for { // fork means run in the background; join means wait for a result
               fiber <- subtask.fork
               // Do stuff...
               a <- fiber.join
             } yield a
            ```
    * in ZIO: `def fork: URIO[R, Fiber.Runtime[E, A]]`
        * forks this effect into its own separate fiber
            * returning the fiber immediately
        * new fiber is attached to the parent fiber's scope
            * when the parent fiber terminates, the child fiber will be terminated as well ("auto supervision")
        * creating the fiber itself — and running the IO on a separate thread — is an effect (therefore the return type)
    * in `Fiber`: `def join: IO[E, A]`
        * semantically block but never block underlying threads
    * in `Fiber`: `def interrupt: UIO[Exit[E, A]]`
        * if the fiber already succeeded: `Exit.Success[A]` otherwise `Exit.Failure[Cause.Interrupt]`
        * unlike interrupting a thread, interrupting a fiber is an easy operation
            * it simply tells the `Executor` that the fiber must not be scheduled anymore
    * is the closest analogy to `Future`
        * if we see fiber it is probably doing something or already evaluated
             * if it is not doing active work and can't do active work - will be garbage collected
                   * you don't have to take care of explicitly shutting them down
        * no start method, as soon as fiber is created it is started as well
    * it’s up to the ZIO runtime to schedule fibers for execution (on the internal JVM thread pool)
        * ZIO executes fibers using an Executor (abstraction over a thread pool)
* blocks the current thread until completion? If you run it within a regular IO, you will block a thread from your application’s main thread pool and potentially cause thread starvation
    * ZIO has a solution for that, which is to wrap your code within effectBlocking
    * The return type is ZIO[Blocking, Throwable, A], which means that it requires a “blocking environment” (= the thread pool to use) and that it catches exceptions
    * By the way, never wrap Thread.sleep, use non-blocking IO.sleep instead.

* useful methods
    * def effectAsync[R, E, A](register: (ZIO[R, E, A] => Unit) => Any, blockingOn: List[Fiber.Id] = Nil): ZIO[R, E, A]
        * used for asynchronous side-effect with a callback-based API
        * more specialized equivalents: `fromCompletableFuture`, `fromFuture`
        * example
            ```
            def send(client: AsyncClient): Task[Unit] =
              IO.effectAsync[Any, Throwable, Unit] { cb =>
                client
                  .sendMessage(msg)
                  .handle[Unit]((_, err) => {
                    err match {
                      case null => cb(IO.unit)
                      case ex   => cb(IO.fail(ex))
                    }
                  })
                ()
              }
            ```
    * `zio.blocking.effectBlocking[A](effect: => A): RIO[Blocking, A]`
        * used for legacy code which not only throws exceptions, but also blocks the current thread
        * example: synchronous socket/file reads
        * `Blocking`: thread pool that can be used for performing blocking operations
            * by default, ZIO is asynchronous and all effects will be executed on a default primary thread pool
            which is optimized for asynchronous operations
    * `ZIO.foreachPar(ids)(getUserById)`
        * automatically interrupt others if one fails
    * `getDataFromEastCoast.race(getDataFromWestCoast)`
        * returns first
        * automatically interrupt the loser
    * use `IO.sleep` (never wrap `Thread.sleep`)
* provided primitives
  * Ref - functional equivalent of atomic ref
  * Promise - single value communication
  * Queue - multiple value communication
  * Hub - broadcast values to many subscribers
  * Semaphore - control level of concurrency
  * Schedule - manage repeats and retries

### dependency injection
* features
    * type-safe
    * resource-safe
    * potentially concurrent way
    * principled error handling
    * without reflection or classpath scanning
* example
    ```
    type InternalServiceEnv = Has[Service1] with Has[Service2] with ...
    type ApiRepositoryEnv = Has[ApiRepository1] with Has[ApiRepository2] with ...

    val service: URLayer[InternalServiceEnv with ApiRepositoryEnv, CustomerServiceEnv] = {
      for {
        service <- ZIO.service[IdService] // get service from environment
        repository <- ZIO.service[CustomerRepository] // get service from environment
      } yield CustomerService(service, repository)
    }.toLayer // lift a ZIO effect to a ZLayer
    ```
* `Has[A]` represents a dependency on a service `A`
    * like a map but with compile type check if we have something there
    * suppose we don't have `Has`
        ```
        zio = ZIO[Int with String, ...]
        zio.provide(12) // compile type error
        // however, if we define trait HasInt { value: Int } and its HasString counterpart
        // we could do
        zio = ZIO[HasInt with HasString, ...]
        zio.provide( new HasInt with HasString { ... } )
        ```
    * suppose we need a dependency on a service `A` that is itself dependent on some service `B`
        * dependencies are like a graph with layers
        * we need a type representing some kind of `B => A`
* `ZLayer[-RIn, +E, +ROut <: Has[_]]`
    * description to build an environment of type `ROut`, starting from a value `RIn`, possibly
    producing an error `E` during creation
    * horizontal composition: `ZLayer[RIn, E1, ROut] ++ ZLayer[RIn2, E1, ROut2] = ZLayer[RIn with RIn2, E1, ROut1 with ROut2]`
        * layer that has the requirements and provides the capabilities of both layers
        * get in parallel
    * vertical composition: `ZLayer[RIn, E1, ROut] >>> ZLayer[RIn2, E1, ROut2] = ZLayer[ROut, E1, ROut2]`
        * layer with the requirement of the first and the output of the second layer
        * released in reverse order
* useful aliases
    * `TaskLayer[+ROut]`
    * `ULayer[+ROut]`
    * `RLayer[-RIn, +ROut]`
    * `Layer[+E, +ROut]`
    * `URLayer[-RIn, +ROut]`
* `type ZEnv = Clock with Console with System with Random with Blocking`
    * is just an alias for all standard modules provided by ZIO
* providing dependencies
    * `provideLayer[E1 >: E, R0, R1](layer: ZLayer[R0, E1, R1])`
    * `def provideSomeLayer[R0 <: Has[_]]: ZIO.ProvideSomeLayer[R0, R, E, A]`
        * example: `prog.provideSomeLayer[ZEnv](...)`
        * it's like provideLayer(ZLayer.identity[R0] ++ layer)
    * example
        ```
        val effect1: URIO[Has[Int], Int] = ZIO.service[Int].map(_ * 2)
        val effect2: ZIO[Has[String], IOException, String] = ZIO.service[String].map(_ + "!")

        val effect: ZIO[Has[String] with Has[Int], IOException, (Int, String)] = effect1 <*> effect2

        val program: IO[IOException, (Int, String)] = effect.provide(Has(66) ++ Has("hello world"))

        override def run(args: List[String]): URIO[zio.ZEnv, ZExitCode] = (for {
          p <- program
          _ <- putStrLn(p.toString())
        } yield ()).exitCode
        ```
        notice, that we operate on bare `Has[...]`, but we could go through layers:
        ```
        val stringLayer: ULayer[Has[String]] = ZLayer.succeed("hello world")
        val intLayer: ULayer[Has[Int]] = ZLayer.succeed(66)
        val composedLayer: ULayer[Has[String] with Has[Int]] = stringLayer ++ intLayer

        val program: IO[IOException, (Int, String)] = effect.provideLayer(composedLayer)
        ```
### testing
* https://zio.dev/version-1.x/howto/mock-services

## http4s

## fs2

## doobie
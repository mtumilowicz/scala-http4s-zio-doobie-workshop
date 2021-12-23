# scala-http4s-zio-fs2-workshop

* https://medium.com/@ghostdogpr/wrapping-impure-code-with-zio-9265c219e2e
* https://scalac.io/blog/introduction-to-programming-with-zio-functional-effects/
* https://degoes.net/
* https://scalac.io/wp-content/uploads/2021/04/Scalac_Ebook_Mastering_modularity_in_ZIO_updated..pdf

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

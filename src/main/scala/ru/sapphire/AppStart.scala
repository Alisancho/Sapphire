package ru.sapphire

import cats.effect.ExitCode
import monix.eval.{Task, TaskApp}
import monix.execution.Scheduler

object AppStart extends TaskApp {
  override val scheduler: Scheduler =
    Scheduler.fixedPool(name = "sapphire", poolSize = 4)

  override def run(args: List[String]): Task[ExitCode] =
    for {
      _ <- Task.unit
    } yield ExitCode(0)
}

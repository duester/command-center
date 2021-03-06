package commandcenter

import java.util.Locale
import commandcenter.TestRuntime.TestEnv
import commandcenter.command.cache.InMemoryCache
import commandcenter.shortcuts.Shortcuts
import commandcenter.tools.Tools
import sttp.client.httpclient.zio.HttpClientZioBackend
import zio.{ Layer, ZLayer }
import zio.duration._
import zio.test.environment.testEnvironment
import zio.test.{ RunnableSpec, TestAspect, TestExecutor, TestRunner }

trait CommandBaseSpec extends RunnableSpec[TestEnv, Any] {
  val testEnv: Layer[Throwable, TestEnv] = {
    import zio.magic._

    ZLayer.fromMagic[TestEnv](
      testEnvironment,
      CCLogging.make(TerminalType.Test),
      Tools.live,
      Shortcuts.unsupported,
      HttpClientZioBackend.layer(),
      InMemoryCache.make(5.minutes)
    )
  }

  val defaultCommandContext: CommandContext =
    CommandContext(Locale.ENGLISH, TestTerminal, 1.0)

  override def aspects: List[TestAspect[Nothing, TestEnv, Nothing, Any]] =
    List(TestAspect.timeoutWarning(60.seconds))

  override def runner: TestRunner[TestEnv, Any] =
    TestRunner(TestExecutor.default(testEnv.orDie))
}

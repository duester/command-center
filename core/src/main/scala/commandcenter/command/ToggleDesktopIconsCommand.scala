package commandcenter.command

import com.typesafe.config.Config
import commandcenter.CCRuntime.Env
import commandcenter.util.OS
import zio.process.{ Command => PCommand }
import zio.{ TaskManaged, ZIO, ZManaged }

final case class ToggleDesktopIconsCommand(commandNames: List[String]) extends Command[Unit] {
  val commandType: CommandType = CommandType.ToggleDesktopIconsCommand
  val title: String            = "Toggle Desktop Icons"

  override val supportedOS: Set[OS] = Set(OS.MacOS)

  def preview(searchInput: SearchInput): ZIO[Env, CommandError, PreviewResults[Unit]] =
    for {
      input <- ZIO.fromOption(searchInput.asKeyword).orElseFail(CommandError.NotApplicable)
    } yield {
      val run = for {
        showingIcons <- PCommand("defaults", "read", "com.apple.finder", "CreateDesktop").string.map(_.trim == "1")
        _            <-
          PCommand("defaults", "write", "com.apple.finder", "CreateDesktop", "-bool", (!showingIcons).toString).exitCode
        _            <- PCommand("killall", "Finder").exitCode
      } yield ()

      PreviewResults.one(Preview.unit.onRun(run).score(Scores.high(input.context)))
    }
}

object ToggleDesktopIconsCommand extends CommandPlugin[ToggleDesktopIconsCommand] {
  def make(config: Config): TaskManaged[ToggleDesktopIconsCommand] =
    ZManaged.fromEither(
      for {
        commandNames <- config.get[Option[List[String]]]("commandNames")
      } yield ToggleDesktopIconsCommand(commandNames.getOrElse(List("icons")))
    )
}

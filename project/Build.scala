import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "signalbox"
    val appVersion      = "1.0"

    val appDependencies = Seq(
      "org.eclipse.jgit" % "org.eclipse.jgit" % "1.3.0.201202151440-r"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
      // Add your own project settings here      
    )

}

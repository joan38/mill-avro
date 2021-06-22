import $ivy.`com.goyeau::mill-git:0.2.2`
import $ivy.`com.goyeau::mill-scalafix:0.2.2`
import $ivy.`de.tototec::de.tobiasroeser.mill.integrationtest_mill0.9:0.4.0`
import $ivy.`io.github.davidgregory084::mill-tpolecat:0.2.0`
import com.goyeau.mill.git.{GitVersionModule, GitVersionedPublishModule}
import com.goyeau.mill.scalafix.StyleModule
import de.tobiasroeser.mill.integrationtest._
import io.github.davidgregory084.TpolecatModule
import mill._
import mill.scalalib._
import mill.scalalib.publish.{Developer, License, PomSettings, VersionControl}

object `mill-avro` extends ScalaModule with TpolecatModule with StyleModule with GitVersionedPublishModule {
  override def scalaVersion = "2.13.5"

  lazy val millVersion = "0.9.8"
  override def compileIvyDeps = super.compileIvyDeps() ++ Agg(
    ivy"com.lihaoyi::mill-main:$millVersion",
    ivy"com.lihaoyi::mill-scalalib:$millVersion"
  )
  override def ivyDeps = super.ivyDeps() ++ {
    val version = "1.0.0-RC24"
    Agg(
      ivy"com.julianpeeters::avrohugger-core:$version",
      ivy"com.julianpeeters::avrohugger-filesorter:$version"
    )
  }

  override def publishVersion = GitVersionModule.version(withSnapshotSuffix = true)()
  def pomSettings = PomSettings(
    description =
      "A Mill plugin for generating Scala case classes and ADTs from Apache Avro schemas, datafiles, and protocols",
    organization = "com.goyeau",
    url = "https://github.com/joan38/mill-avro",
    licenses = Seq(License.MIT),
    versionControl = VersionControl.github("joan38", "mill-avro"),
    developers = Seq(Developer("joan38", "Joan Goyeau", "https://github.com/joan38"))
  )
}

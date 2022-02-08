import $ivy.`com.goyeau::mill-git::0.2.3`
import $ivy.`com.goyeau::mill-scalafix::0.2.8`
import $ivy.`de.tototec::de.tobiasroeser.mill.integrationtest::0.4.1-30-f29f55`
import $ivy.`io.github.davidgregory084::mill-tpolecat::0.3.0`
import com.goyeau.mill.git.{GitVersionModule, GitVersionedPublishModule}
import com.goyeau.mill.scalafix.StyleModule
import de.tobiasroeser.mill.integrationtest._
import io.github.davidgregory084.TpolecatModule
import mill._
import mill.scalalib._
import mill.scalalib.api.Util.scalaNativeBinaryVersion
import mill.scalalib.publish.{Developer, License, PomSettings, VersionControl}

val millVersions                           = Seq("0.10.0", "0.9.12")
def millBinaryVersion(millVersion: String) = scalaNativeBinaryVersion(millVersion)

object `mill-avro` extends Cross[MillAvroCross](millVersions: _*)
class MillAvroCross(millVersion: String)
    extends CrossModuleBase
    with TpolecatModule
    with StyleModule
    with GitVersionedPublishModule {
  override def crossScalaVersion = "2.13.7"
  override def artifactSuffix    = s"_mill${millBinaryVersion(millVersion)}" + super.artifactSuffix()

  override def compileIvyDeps = super.compileIvyDeps() ++ Agg(
    ivy"com.lihaoyi::mill-main:$millVersion",
    ivy"com.lihaoyi::mill-scalalib:$millVersion"
  )
  override def ivyDeps = super.ivyDeps() ++ {
    val version = "1.0.0-RC30"
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

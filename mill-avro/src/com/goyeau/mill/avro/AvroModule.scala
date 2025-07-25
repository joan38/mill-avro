package com.goyeau.mill.avro

import avrohugger.Generator
import avrohugger.filesorter.AvdlFileSorter
import avrohugger.filesorter.AvscFileSorter
import avrohugger.format.Standard
import avrohugger.format.abstractions.SourceFormat
import avrohugger.types.AvroScalaTypes
import mill.*
import mill.api.PathRef
import mill.javalib.api.JvmWorkerUtil.scalaBinaryVersion
import mill.scalalib.ScalaModule
import os.Path
import upickle.default.ReadWriter

import java.io.File
import java.util.UUID

trait AvroModule extends ScalaModule {
  implicit def sourceFormatRW[F <: SourceFormat]: ReadWriter[F] = upickle.default
    .readwriter[String]
    .bimap[F](_ => UUID.randomUUID().toString, _ => ???)
  implicit val avroScalaTypesRW: ReadWriter[AvroScalaTypes] = upickle.default
    .readwriter[String]
    .bimap[AvroScalaTypes](_ => UUID.randomUUID().toString, _ => ???)

  def avroSources: T[Seq[PathRef]]                     = Task.Sources(moduleDir / "avro")
  def avroScalaCustomNamespace: T[Map[String, String]] = Map.empty[String, String]
  def avroScalaFormat: T[SourceFormat]                 = Standard
  def avroScalaCustomTypes: T[AvroScalaTypes]          = avroScalaFormat().defaultTypes

  override def generatedSources: T[Seq[PathRef]] = super.generatedSources() :+ generateScalaFromAvro(
    Generator(
      format = avroScalaFormat(),
      avroScalaCustomTypes = Some(avroScalaCustomTypes()),
      avroScalaCustomNamespace = avroScalaCustomNamespace(),
      targetScalaPartialVersion = scalaBinaryVersion(scalaVersion())
    ),
    avroSources(),
    Task.dest / "avro"
  )

  private def generateScalaFromAvro(generator: Generator, avroSources: Seq[PathRef], out: Path) = {
    AvscFileSorter.sortSchemaFiles(filesFor(avroSources, "avsc")).foreach { avroFile =>
      println(s"Generating case classes from AVSC $avroFile")
      generator.fileToFile(avroFile, out.toString)
    }

    AvdlFileSorter.sortSchemaFiles(filesFor(avroSources, "avdl")).foreach { avroFile =>
      println(s"Generating case classes from Avro IDL $avroFile")
      generator.fileToFile(avroFile, out.toString)
    }

    filesFor(avroSources, "avro").foreach { avroFile =>
      println(s"Compiling case classes from Avro datafile $avroFile")
      generator.fileToFile(avroFile, out.toString)
    }

    filesFor(avroSources, "avpr").foreach { avroFile =>
      println(s"Compiling case classes from Avro protocol $avroFile")
      generator.fileToFile(avroFile, out.toString)
    }

    PathRef(out)
  }

  private def filesFor(sources: Seq[PathRef], extension: String): Seq[File] =
    for {
      path <- sources.map(_.path)
      if os.exists(path)
      file <-
        if (os.isDir(path)) os.walk(path).filter(file => os.isFile(file) && (file.ext == extension))
        else Seq(path)
    } yield file.toIO
}

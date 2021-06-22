package com.goyeau.mill.avro

import avrohugger.Generator
import avrohugger.filesorter.{AvdlFileSorter, AvscFileSorter}
import avrohugger.format.Standard
import mill._
import mill.define.Sources
import mill.scalalib.JavaModule
import os.Path
import java.io.File

trait AvroModule extends JavaModule {
  def avroSources: Sources                             = T.sources(millSourcePath / "avro")
  def avroScalaCustomNamespace: T[Map[String, String]] = Map.empty[String, String]

  override def generatedSources: T[Seq[PathRef]] = super.generatedSources() :+ generateScalaFromAvro(
    Generator(format = Standard, avroScalaCustomNamespace = avroScalaCustomNamespace()),
    avroSources(),
    T.dest / "avro"
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

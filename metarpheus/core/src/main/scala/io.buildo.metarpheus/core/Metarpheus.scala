package io.buildo.metarpheus
package core

import scala.meta._
import scala.meta.inputs.Input.File
import scala.meta.io.AbsolutePath
import scala.meta.internal.io.PlatformFileIO

object Metarpheus {

  def run(paths: List[String], config: Config): intermediate.API = {
    val parsed = parseFiles(paths)
    extractors
      .extractFullAPI(parsed = parsed)
      .stripUnusedModels(config.modelsForciblyInUse, config.discardRouteErrorModels)
  }

  def parseFiles(paths: List[String]) : List[Source] = {
    val files = paths
      .flatMap(path => PlatformFileIO.listAllFilesRecursively(AbsolutePath(path)))
      .filter(_.toString.endsWith(".scala"))
    files.map(File(_).parse[Source].get)
}

}

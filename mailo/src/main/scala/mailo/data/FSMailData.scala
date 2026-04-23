package mailo.data
import java.io.File

import mailo.{MailError, MailRawContent}

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/*file system based MailData, that assumes you have a folder with templates inside, and tries to match the requested template name and the file name,
it will also search for a subfolder named partials, and, if found, it will try to load all it's files as mailo partials
 * */
class FSMailData(directory: File, loadPartials: Boolean) extends MailData {
  import FSMailDataError._

  lazy val partials = if (loadPartials) internalLoadPartials() else Map.empty[String, String]

  private[this] def internalLoadPartials(): Map[String, String] = {
    Try(
      directory.listFiles(f => f.isDirectory && f.getName.equalsIgnoreCase("partials")).headOption,
    ).map(
      _.map(partials =>
        partials
          .listFiles(f => f.isFile)
          .map(file => {
            val buffer = scala.io.Source.fromFile(file)
            val template = Try(buffer.mkString)
            buffer.close()
            template.map(content => (file.getName, content))
          })
          .flatMap(_.toOption)
          .toMap, // convert all tries to an option, remove none and create a map with the remainings
      ),
    ).toOption
      .flatten
      .getOrElse(Map.empty[String, String])
  }

  private[this] def parseTemplateFile(f: File): Either[MailError, MailRawContent] = {
    val buffer = scala.io.Source.fromFile(f)
    val template = Try(buffer.mkString)
    buffer.close()
    template match {
      case Success(templateText) => Right(MailRawContent(templateText, partials))
      case Failure(exception)    => Left(IOError(exception.getMessage))
    }
  }

  private[this] def internalGet(name: String) = {
    if (!directory.isDirectory) Left(NotADirectory(directory.getAbsolutePath))
    else
      directory
        .listFiles()
        .find(f => f.getName == name)
        .map(templateFile => parseTemplateFile(templateFile))
        .getOrElse(Left(TemplateNotFound(directory.getAbsolutePath.concat(s"/$name"))))
  }

  override def get(name: String): Future[Either[MailError, MailRawContent]] =
    Future.successful(internalGet(name))
}

object FSMailDataError {
  case class NotADirectory(path: String) extends MailError(s"expected a directory but got: $path")
  case class TemplateNotFound(path: String) extends MailError(s"can't find template file: $path")
  case class IOError(msg: String) extends MailError(s"error while reading template file: $msg")
}

package mailo

import mailo.data.FSMailData
import java.nio.file.Files
import java.io.{File, PrintWriter}

import mailo.data.FSMailDataError.{NotADirectory, TemplateNotFound}

class FSMailDataSpec extends munit.FunSuite {
  implicit val ec = munitExecutionContext

  val templateTest1 = "<html><h1>Ciao Mr. {{name}}</h1></html>"
  val partialName = "footer"
  val templateTest2 = s"<html><h1>Ciao, ecco un parziale</h1>[[$partialName]]</html>"
  val partial1 = "<p>Hi, I am an english partial</p>"

  test("FSMailData should retrieve a simple template") {
    val templateName = "test-template-1"
    val dir = Files.createTempDirectory("mailo-test")
    new PrintWriter(dir.toString + s"/${templateName}") { write(templateTest1); close }
    val dirFile = new File(dir.toString)

    val mailData = new FSMailData(dirFile, false)
    mailData
      .get(templateName)
      .map { value =>
        assertEquals(value, Right(MailRawContent(templateTest1, Map.empty[String, String])))
        dirFile.listFiles().foreach(f => f.delete())
        dirFile.delete()
      }

  }

  test("FSMailData should return the correct error if given directory was not found") {
    val mailData = new FSMailData(new File("asd"), false)
    mailData.get("asd").map {
      case Left(NotADirectory(_)) => ()
      case _                      => fail("expected NotADirectory error")
    }
  }

  test("FSMailData should return the correct error if template was not found") {
    val dir = Files.createTempDirectory("mailo-test")
    val dirFile = new File(dir.toString)
    val mailData = new FSMailData(dirFile, false)
    mailData.get("asd").map {
      case Left(TemplateNotFound(_)) => ()
      case _                         => fail("expected TemplateNotFound error")
    }
  }

  test("FSMailData should load and use partials") {
    val templateName = "test-template-1"
    val dir = Files.createTempDirectory("mailo-test")
    new PrintWriter(dir.toString + s"/$templateName") { write(templateTest2); close }
    val partialDir = new File(dir + "/partials")
    partialDir.mkdir()
    new PrintWriter(partialDir.getAbsolutePath + s"/$partialName") { write(partial1); close }
    val dirFile = new File(dir.toString)
    val mailData = new FSMailData(dirFile, true)

    mailData.get(templateName).map { value =>
      assertEquals(value, Right(MailRawContent(templateTest2, Map(partialName -> partial1))))
      partialDir.listFiles().foreach(f => f.delete())
      dirFile.listFiles().foreach(f => f.delete())
      dirFile.delete()
    }
  }
}

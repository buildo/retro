package mailo

import org.scalatest._
import java.io.{File, PrintWriter}

import mailo.data.{FSMailData}
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import java.nio.file.Files

import mailo.data.FSMailDataError.{NotADirectory, TemplateNotFound}

class FSMailDataSpec extends FlatSpec with Matchers {

  val templateTest1 = "<html><h1>Ciao Mr. {{name}}</h1></html>"
  val partialName = "footer"
  val templateTest2 = s"<html><h1>Ciao, ecco un parziale</h1>[[$partialName]]</html>"
  val partial1 = "<p>Hi, I am an english partial</p>"

  "FSMailData" should "retrieve a simple template" in {
    val templateName = "test-template-1"
    val dir = Files.createTempDirectory("mailo-test")
    new PrintWriter(dir.toString+s"/${templateName}") { write(templateTest1); close }
    val dirFile = new File(dir.toString)

    val mailData = new FSMailData(dirFile, false)
    Await.result(mailData.get(templateName), Duration.Inf) should be (Right(MailRawContent(templateTest1, Map.empty[String, String])))

    dirFile.listFiles().foreach(f => f.delete())
    dirFile.delete()
  }

  it should "return the correct error if given directory was not found" in {
    val mailData = new FSMailData(new File("asd"), false)
    Await.result(mailData.get("asd"), Duration.Inf) match {
      case Left(NotADirectory(_)) => succeed
      case _ => fail("expected NotADirectory error")
    }
  }

  it should "return the correct error if template was not found" in {
    val dir = Files.createTempDirectory("mailo-test")
    val dirFile = new File(dir.toString)
    val mailData = new FSMailData(dirFile, false)
    Await.result(mailData.get("asd"), Duration.Inf) match {
      case Left(TemplateNotFound(_)) => succeed
      case _ => fail("expected TemplateNotFound error")
    }
  }

  it should "load and use partials" in {
    val templateName = "test-template-1"
    val dir = Files.createTempDirectory("mailo-test")
    new PrintWriter(dir.toString+s"/$templateName") { write(templateTest2); close }
    val partialDir = new File(dir+"/partials")
    partialDir.mkdir()
    new PrintWriter(partialDir.getAbsolutePath+s"/$partialName") { write(partial1); close }
    val dirFile = new File(dir.toString)
    val mailData = new FSMailData(dirFile, true)
    Await.result(mailData.get(templateName), Duration.Inf) should be (Right(MailRawContent(templateTest2, Map(partialName -> partial1))))

    partialDir.listFiles().foreach(f => f.delete())
    dirFile.listFiles().foreach(f => f.delete())
    dirFile.delete()
  }
}

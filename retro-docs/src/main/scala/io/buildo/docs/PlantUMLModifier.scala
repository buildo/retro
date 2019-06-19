package io.buildo.docs

import mdoc.StringModifier
import mdoc.Reporter
import scala.meta.Input
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import net.sourceforge.plantuml.SourceStringReader
import net.sourceforge.plantuml.FileFormatOption
import net.sourceforge.plantuml.FileFormat

class PlantUMLModifier extends StringModifier {
  override val name = "plantuml"

  override def process(info: String, code: Input, reporter: Reporter): String = {
    val source = s"@startuml\n${code.text}\n@enduml\n"
    val reader = new SourceStringReader(source)
    val os = new ByteArrayOutputStream
    reader.generateImage(os, new FileFormatOption(FileFormat.SVG))
    os.close()
    val svg = new String(os.toByteArray, Charset.forName("UTF-8"))
    s"<div align='center'>$svg</div>"
  }
}

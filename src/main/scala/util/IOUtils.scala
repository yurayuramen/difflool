package util

import java.io.{File, FileOutputStream, StringWriter}
import java.nio.charset.Charset

import scala.io.Source

object IOUtils {

  val CharsetUTF8 = Charset.forName("utf-8")

  implicit class FileExt(file:File){
    def writeALL(source:String,charset:Charset=CharsetUTF8): Unit = {
      using(new FileOutputStream(file)) { os =>
        os.write(source.getBytes(charset))
      }
    }

    def readALLString(charset:Charset=CharsetUTF8) ={
      val writer=new StringWriter()
      using(Source.fromFile(file,charset.displayName())){source=>
        val iterator = source.iter
        while(iterator.hasNext){
          writer.write(iterator.next())
        }
        writer.toString
      }
      /*
      val fileSize = file.length().toInt
      using(new FileInputStream(file)) { is =>
        val array = Array.fill[Byte](fileSize)(0)
        is.read(array)
        new String(array,charset)
      }
      */
    }

  }

  implicit class StringExt(file:String){
    def writeALL(source:String,charset:Charset=CharsetUTF8): Unit = new File(file).writeALL(source,charset)
    def readALLString(charset:Charset=CharsetUTF8) =new File(file).readALLString(charset)
  }

}

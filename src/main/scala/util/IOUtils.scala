package util

import java.io.{File, FileInputStream, FileOutputStream, StringWriter}
import java.nio.charset.Charset
import java.nio.file.Files
import java.security.MessageDigest

import scala.io.{Codec, Source}

object IOUtils {

  val CharsetUTF8 = Charset.forName("utf-8")

  case class FileTooLargeException(msg:String) extends RuntimeException(msg)

  implicit class FileExt(underlying:File){

    def writeALL(source:String,charset:Charset=CharsetUTF8): Unit = {
      writeALL(source.getBytes(charset))
    }

    def writeALL(source:Array[Byte]): Unit = {
      using(new FileOutputStream(underlying)) { os =>
        os.write(source)
      }
    }

    def readALLString(charset:Charset=CharsetUTF8) ={
      new String(readALLBytes(),charset)
    }

    def readALLString()(implicit codec:Codec) ={
      new String(readALLBytes(),codec.charSet)
    }

    def readALLBytes():Array[Byte]={
      Files.readAllBytes(underlying.toPath)
    }

  }

  implicit class StringExt(underlying:String){
    def writeALL(source:Array[Byte]): Unit = new File(underlying).writeALL(source)
    def writeALL(source:String,charset:Charset=CharsetUTF8): Unit = new File(underlying).writeALL(source,charset)
    def readALLString(charset:Charset=CharsetUTF8) =new File(underlying).readALLString(charset)
  }

  /*
  object CheckSumTypes{


    trait Checksum{
      def build(source:Array[Byte])
    }
    case object MD5Sum extends Checksum{
      def build(source:Array[Byte])={
      }
    }

  }

  def checksum(): Unit ={

  }
  */
}

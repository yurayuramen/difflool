package util

import java.io.{ByteArrayOutputStream, File, FileInputStream}
import java.security.MessageDigest
import java.util.zip.{GZIPInputStream, ZipInputStream}

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream

import scala.annotation.tailrec

object ArchiveTypes
{
  def defaultExtensions:Seq[(String,ArchiveTypes.ArchiveType)] =
    Seq("zip" -> ArchiveTypes.Zip,"jar" -> ArchiveTypes.Zip,"tgz" -> ArchiveTypes.TarGz,"tar.gz" -> ArchiveTypes.TarGz,"tar" -> ArchiveTypes.Tar)

  type Extensions = Seq[(String,ArchiveTypes.ArchiveType)]
  trait ArchiveType{
    def createInputStream(flle:File):ArchiveInputStream
  }
  object Zip extends ArchiveType{
    def createInputStream(file:File)= new ZipInputStreamWrapper(new FileInputStream(file))
  }
  object Tar extends ArchiveType{
    def createInputStream(file:File)=new TarArchiveInputStreamWrapper(new FileInputStream(file))
  }
  object TarGz extends ArchiveType{
    def createInputStream(file:File)=new TarArchiveInputStreamWrapper(new GZIPInputStream(new FileInputStream(file)))
  }
}

object ArchiveUtils {

  case class ArchiveEntry(name:String,size:Long,isDirectory:Boolean,md5sum:Option[String],body:Option[Array[Byte]])






}
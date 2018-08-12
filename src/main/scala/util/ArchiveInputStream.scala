package util

import java.io.{ByteArrayOutputStream, File, FileInputStream, InputStream}
import java.util.zip.ZipInputStream

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream

import scala.annotation.tailrec
import scala.collection.mutable.ArrayBuffer
import scala.util.Try
import util.ArchiveTypes._

object ArchiveInputStream {

  class NotMatchExtensionException(file:File) extends RuntimeException(s"${file.getAbsolutePath}はアーカイブ形式をみつけられませんでした")

  def apply(file:File,extensions:Extensions=defaultExtensions):util.ArchiveInputStream={
    val fileName = file.getName

    if(file.isDirectory)
      new DirectoryInputStream(file)
    else
      extensions.find { case (exten, _) => fileName.endsWith(s".${exten}") }.map { case (_, archiveType) =>
        archiveType.createInputStream(file)
      }.getOrElse(throw new NotMatchExtensionException(file))
  }

}

case class ArchiveItem(name:String,size:Long,directory:Boolean)
trait ArchiveInputStream extends AutoCloseable {
  def getNextItem() : Option[ArchiveItem]
  def read(array:Array[Byte]):Int
  def closeEntry():Unit

  def getNextItemWithBody():Option[(ArchiveItem,Option[Array[Byte]])]={
    getNextItem().map{item=>

      val BuffSize = 1024 * 1024
      val buff = Array.fill[Byte](BuffSize)(0)


      val array =
      if(item.directory)
        None
      else{
        Some apply using(new ByteArrayOutputStream()){bos=>

          @tailrec
          def read(): Unit ={
            val size = this.read(buff)
            if(size != -1){
              bos.write(buff, 0, size)
              read()
            }
          }
          read()
          this.closeEntry()
          bos.toByteArray
        }
      }
      item -> array
    }
  }

  def read[T](readAction:(ArchiveInputStream,ArchiveItem,Option[Array[Byte]])=>T):Seq[T]={


    @tailrec
    def _read(list:Seq[T]): Seq[T]={
      val itemWithBody = getNextItemWithBody()
      if(itemWithBody.isDefined){
        val (item,body) = itemWithBody.get

        val item2 = body.map{body=> item.copy(size=body.length)}.getOrElse(item)
        val result = readAction(this,item2,body)
        _read(list :+ result)
      }
      else
        list
    }
    _read(Nil)
  }

  def listWithMd5Checksum()={
    val md5SumBuilder = new Md5SumBuilder()
    read({(inputStream,item,body)=> item -> body.map( md5SumBuilder.build )})
  }

  def listWithSha1Checksum()={
    val sha1SumBuilder =new Sha1SumBuilder()
    read({(inputStream,item,body)=> item -> body.map( sha1SumBuilder.build )})
  }

  def list()={
    read({(_,item,_)=>item})
  }

  def extract(extractDir:File,emptyDirCreate:Boolean=false):Seq[File]={

    val extractRootPath = extractDir.getCanonicalPath.replace('\\','/')

    val list=
      read({(_,item,body)=>

        if(!item.directory){

          val itemOutputFilePath = s"${extractRootPath}/${item.name}"
          val itemOutputFile=new File(itemOutputFilePath)
          val itemOutputDir = itemOutputFile.getParentFile
          //val itemOutputDirPath  = itemOutputFilePath.substring(0,itemOutputFilePath.lastIndexOf("/"))

          if(!itemOutputDir.exists()) itemOutputDir.mkdirs()
          //val itemOutputFile=new File(itemOutputFilePath)
          import IOUtils._
          body.map(itemOutputFile.writeALL)
          Some(itemOutputFile)
        }
        else if(emptyDirCreate){
          val itemOutputDir = new File(s"${extractRootPath}/${item.name}")
          if(!itemOutputDir.exists()) itemOutputDir.mkdirs()
          Some(itemOutputDir)
        }
        else
          None

      })
    list.flatten
  }


}

class DirectoryInputStream(file:File) extends ArchiveInputStream{

  private[this] val dirPathLength = file.getCanonicalPath.length

  private[this] var inputStream:Option[FileInputStream] = None
  private[this] val iterator:Iterator[File] = {

    val fileList = ArrayBuffer.empty[File]
    //@tailrec
    def read1(file:File): Unit = {
      val iterator = file.listFiles().iterator
      while (iterator.hasNext) {
        val file = iterator.next()
        fileList += file
        if (file.isDirectory)
          read1(file)
      }
    }
    read1(file)
    fileList.iterator
  }

  def close(): Unit =
    Try {
      inputStream.map(_.close())
      inputStream = None
    }


  override def closeEntry(): Unit = close()

  override def read(array: Array[Byte]): Int = inputStream.map(_.read(array) ).getOrElse(-1)

  override def getNextItem(): Option[ArchiveItem] = {
    if(iterator.hasNext){
      val file = iterator.next()
      val directory = file.isDirectory
      val name = file.getCanonicalPath.substring(dirPathLength + 1).replace('\\','/')
      if(!directory){
        this.inputStream = Some(new FileInputStream(file))
      }
      Some(ArchiveItem(name,file.length(),directory))
    }
    else
      None
  }
}

class ZipInputStreamWrapper(underlying:InputStream) extends ZipInputStream(underlying) with ArchiveInputStream{

  if(underlying.isInstanceOf[ZipInputStream]){
    throw new IllegalArgumentException("コンストラクタ引数にZipInputStreamを渡さないでください")
  }


  override def getNextItem(): Option[ArchiveItem] = {
    val entry = super.getNextEntry
    if(entry!=null){
      Some(ArchiveItem(entry.getName,entry.getSize,entry.isDirectory))
    }
    else
      None
  }

}

class TarArchiveInputStreamWrapper(underlying:InputStream) extends TarArchiveInputStream(underlying) with ArchiveInputStream{

  if(underlying.isInstanceOf[TarArchiveInputStream]){
    throw new IllegalArgumentException("コンストラクタ引数にTarArchiveInputStreamを渡さないでください")
  }

  override def getNextItem(): Option[ArchiveItem] = {
    val entry = super.getNextEntry
    if(entry!=null){
      Some(ArchiveItem(entry.getName,entry.getSize,entry.isDirectory))
    }
    else
      None
  }

  override def closeEntry(): Unit ={

  }
}

package util

import java.io.File

import difflib.DiffUtils
import util.DiffUtilsWrapper.format

object DiffUtilsWrapper {


  def format(file:File): Seq[String]={
    ArchiveInputStream(file).listWithMd5Checksum().sortBy(_._1.name).filterNot(_._1.directory).map{case(item,checksum)=> s"name:${item.name},size:${item.size}md5:${checksum.getOrElse("")}"}
  }



  def diff(leftFile:File,rightFile:File)={


    import scala.collection.JavaConverters._
    val leftFileList = format(leftFile).asJava
    val rightFileList = format(rightFile).asJava

    val patch=DiffUtils.diff(leftFileList,rightFileList)
    val list3 = DiffUtils.patch(leftFileList,patch)

    patch.getDeltas.asScala.map{delta=>
      val leftList = delta.getOriginal.getLines.asScala
      val rightList = delta.getRevised.getLines.asScala

      val leftPos1 = delta.getOriginal.getPosition + 1
      val leftPos2 = leftPos1 + delta.getOriginal.size()

      val rightPos1 = delta.getRevised.getPosition + 1
      val rightPos2 = rightPos1 + delta.getRevised.size()
      if(leftList.isEmpty)
      {
        s"a${rightPos1},${rightPos2}\n${rightList.map(line=> s"> ${line}").mkString("\n")}"
      }
      else if(rightList.isEmpty)
      {
        s"${leftPos1},${leftPos2}d${leftPos1},${leftPos2}\n${leftList.map(line=> s"< ${line}").mkString("\n")}"
      }
      else
      {
        s"${leftPos1},${leftPos2}c${rightPos1},${rightPos2}\n${leftList.map(line=> s"< ${line}").mkString("\n")}" ++
          "\n---\n" ++
          s"${rightList.map(line=> s"> ${line}").mkString("\n")}"
      }

    }.mkString("\n")
  }
}

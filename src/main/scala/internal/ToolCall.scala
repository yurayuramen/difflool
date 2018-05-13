package internal

import java.io.File
import scala.sys.process.Process

object ToolCall{
  val FileNameLeft = "left.txt"
  val FileNameRight = "right.txt"
  def apply(toolPath:String,tmpDir:File):ToolCall=new ToolCallDefault(toolPath,tmpDir)
}


trait ToolCall{
  def toolPath:String
  def tmpDir:File
  def filenameLeft:String = ToolCall.FileNameLeft
  def filenameRight:String = ToolCall.FileNameRight

  def async():Process
  def sync():String

}

class ToolCallDefault(val toolPath:String,val tmpDir:File) extends ToolCall{

  def async() ={
    val fileLeft = new File(s"${tmpDir.getAbsolutePath}/${filenameLeft}")
    val fileRight = new File(s"${tmpDir.getAbsolutePath}/${filenameRight}")


    //println(s"exec:$toolPath ${fileLeft.getCanonicalPath} ${fileRight.getCanonicalPath} ")

    Process(Seq(toolPath,fileLeft.getCanonicalPath,fileRight.getCanonicalPath)).run()
    //Runtime.getRuntime.exec(Array(toolPath,fileLeft.getCanonicalPath,fileRight.getCanonicalPath))
  }

  def sync()={
    val fileLeft = new File(s"${tmpDir.getAbsolutePath}/${filenameLeft}")
    val fileRight = new File(s"${tmpDir.getAbsolutePath}/${filenameRight}")


    import sys.process._
    import java.io.{ BufferedReader, InputStreamReader }
    val buff=new StringBuilder()

    // ProcessIOの用意
    val pio = new ProcessIO(
      in => {},
      out => {
        //charset がハードコード・・・
        val reader = new BufferedReader(new InputStreamReader(out,"windows-31j"))
        def readLine(): Unit = {
          val line = reader.readLine()
          println(line) // ここに1行ずつで結果が来るから適当に処理する
          buff ++= line
          buff += '\n'
          if(line != null) readLine()
        }
        readLine()
      },
      err => {})

    // runする時に引数に渡す
    val process = s""""${toolPath}" "${fileLeft.getCanonicalPath}" "${fileRight.getCanonicalPath}"""".run(pio)

    //Thread.sleep(3000)

    process.exitValue()

    buff.toString()

    //println(s"exec:$toolPath ${fileLeft.getCanonicalPath} ${fileRight.getCanonicalPath} ")

    //Process(Seq(toolPath,fileLeft.getCanonicalPath,fileRight.getCanonicalPath))!!

  }




}

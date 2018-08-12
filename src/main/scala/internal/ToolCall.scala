package internal

import java.io.File

import conf.ArgsDef

import scala.sys.process.Process

object ToolCall{
  def apply(toolPath:String,left:File,right:File):ToolCall=new ToolCallDefault(toolPath,left,right)
}


trait ToolCall{
  //def toolPath:String
  //def tmpDir:File

  def async():Process
  def sync():(String,String)

}

class ToolCallDefault(val toolPath:String,val tmpLeftFile:File,val tmpRightFile:File) extends ToolCall{

  def async() ={

    //println(s"exec:$toolPath ${fileLeft.getCanonicalPath} ${fileRight.getCanonicalPath} ")
    Process(Seq(toolPath,tmpLeftFile.getCanonicalPath,tmpRightFile.getCanonicalPath)).run()
    //Runtime.getRuntime.exec(Array(toolPath,fileLeft.getCanonicalPath,fileRight.getCanonicalPath))
  }

  def sync()={


    import sys.process._
    import java.io.{ BufferedReader, InputStreamReader }
    val stdout=StringBuilder.newBuilder
    val stderr=StringBuilder.newBuilder

    // ProcessIOの用意
    val pio = new ProcessIO(
      in => {},
      out => {
        val reader = new BufferedReader(new InputStreamReader(out,"utf-8")) //ArgsDef.osDefaultChatset))
        def readLine(): Unit = {
          val line = reader.readLine()
          println(line) // ここに1行ずつで結果が来るから適当に処理する
          stdout ++= line
          stdout += '\n'
          if(line != null) readLine()
        }
        readLine()
      },
      err => {
        val reader = new BufferedReader(new InputStreamReader(err,"utf-8")) //ArgsDef.osDefaultChatset))
        def readLine(): Unit = {
          val line = reader.readLine()
          println(line) // ここに1行ずつで結果が来るから適当に処理する
          stderr ++= line
          stderr += '\n'
          if(line != null) readLine()
        }
        readLine()


      })

    // runする時に引数に渡す

    val toolPath2=
    if(toolPath.contains(" "))
      s""""${toolPath}""""
    else
      toolPath


    val process = s"${toolPath2} ${tmpRightFile.getCanonicalPath} ${tmpLeftFile.getCanonicalPath}".run(pio)

    //Thread.sleep(3000)

    process.exitValue()

    stdout.toString() -> stderr.toString

    //println(s"exec:$toolPath ${fileLeft.getCanonicalPath} ${fileRight.getCanonicalPath} ")

    //Process(Seq(toolPath,fileLeft.getCanonicalPath,fileRight.getCanonicalPath))!!

  }




}

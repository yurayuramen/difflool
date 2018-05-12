package internal

import java.io.File

object ToolCall{
  val FileNameLeft = "left.txt"
  val FileNameRight = "right.txt"
  def apply(toolPath:String,tmpDir:File)=new ToolCallDefault(toolPath,tmpDir)
}


trait ToolCall{
  //TODO 変数の構成がいびつ
  def toolPath:String
  def tmpDir:File
  def filenameLeft:String = ToolCall.FileNameLeft
  def filenameRight:String = ToolCall.FileNameRight


}

class ToolCallDefault(val toolPath:String,val tmpDir:File) extends ToolCall{

  def call() ={
    val pathLeft = s"${tmpDir.getAbsolutePath}/${filenameLeft}"
    val pathRight = s"${tmpDir.getAbsolutePath}/${filenameRight}"
    Runtime.getRuntime.exec(Array(toolPath,pathLeft,pathRight))
  }





}

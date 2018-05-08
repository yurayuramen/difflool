package internal

import java.io.File

object ToolCall{
  def apply(toolPath:String,tmpDir:File)=new ToolCallDefault(toolPath,tmpDir)
}


trait ToolCall{
  def toolPath:String
  def tmpDir:File
  def target1:String=s"${tmpDir.getCanonicalPath}/left"
  def target2:String=s"${tmpDir.getCanonicalPath}/right"



}

class ToolCallDefault(val toolPath:String,val tmpDir:File) extends ToolCall{

  def call() ={
    Runtime.getRuntime.exec(Array(toolPath,target1,target2))
  }





}

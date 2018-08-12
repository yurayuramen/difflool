package conf

import util.ArgParser
import util.ArgParser.{ArgNameItem, Args}

object ArgsDef {


  import ArgParser._

  val argNameTempDir = ArgNameItem("--temp-dir","-t")()
  val argNameTempRightFile = ArgNameItem("--temp-right","-tr")()
  val argNameTempLeftFile = ArgNameItem("--temp-left","-tl")()
  val argNameExecute = ArgNameItem("--exec","-x")()
  val argNameEncoding = ArgNameItem("--source-encoding","-e")()
  //val argNameWinFc = ArgNameItem("--win-fc","-f")(hasValue = false)
  //val argNameDiff = ArgNameItem("--diff","-d")(hasValue = false)

  //val osDefaultChatset = ""

  private[this] var _args:Args = null

  def args = this._args

  def init(params:Array[String]): Unit =
  {
    this._args = new ArgParser(Seq(
      argNameTempDir
      ,argNameTempLeftFile
      ,argNameTempRightFile
      ,argNameExecute
      ,argNameEncoding
      //,argNameWindiff
      //,argNameWinFc
      //,argNameDiff
    )).parse(params)

  }







}

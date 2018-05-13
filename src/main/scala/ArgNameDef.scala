package conf

import util.ArgParser
import util.ArgParser.{ArgNameItem, Args}

object ArgsDef {

  sealed trait OS
  case object Linux extends OS
  case object Windows extends OS
  case object Mac extends OS
  case object Unknown extends OS


  import ArgParser._

  val argNameTempDir = ArgNameItem("--temp-dir","-t")()
  val argNameWindiff = ArgNameItem("--win-merge","-w")()
  val argNameEncoding = ArgNameItem("--source-encoding","-e")()
  val argNameWinFc = ArgNameItem("--win-fc","-f")(hasValue = false)
  val argNameDiff = ArgNameItem("--diff","-d")(hasValue = false)

  private[this] var _args:Args = null

  def args = this._args

  def init(params:Array[String]): Unit =
  {
    this._args = new ArgParser(Seq(
      argNameTempDir
      ,argNameEncoding
      ,argNameWindiff
      ,argNameWinFc
      ,argNameDiff
    )).parse(params)

  }


  lazy val os:OS = {
    val osName = System.getProperty("os.name").toLowerCase

    if(osName.startsWith("linux"))
      Linux
    else if(osName.startsWith("mac"))
      Mac
    else if(osName.startsWith("windows"))
      Windows
    else
      Unknown



  }

  lazy val osDefaultChatset:String={
    this.os match{
      case Windows=> //TODO 日本語版以外でこのコードは間違いだが、一旦ここで
        "windows-31j"
      case _=>
        "utf-8"
    }


  }



}

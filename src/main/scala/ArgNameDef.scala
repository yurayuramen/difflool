package conf

import util.ArgParser
import util.ArgParser.{ArgNameItem, Args}

object ArgsDef {


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







}

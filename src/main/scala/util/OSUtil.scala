package util


object OSUtil {
  sealed trait OS
  case object Linux extends OS
  case object Windows extends OS
  case object Mac extends OS
  case object Unknown extends OS

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

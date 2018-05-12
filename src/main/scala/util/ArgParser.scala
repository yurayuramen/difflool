package util


object ArgParser{

  object ArgNameItem{

    def apply(fullname:String,shortname:String,desc:String="",hasValue:Boolean=true)={
      new ArgNameItem(Seq(fullname,shortname),desc,hasValue)
    }

  }

  case class ArgNameItem(val names:Seq[String],val desc:String,val hasValue:Boolean)
  {
    def name=names(0)

  }

  class Args(source:Map[ArgNameItem,Option[String]]){

    def value(argName:ArgNameItem)=getValue(argName).get

    def getValue(argName:ArgNameItem)={
      source.get(argName) match{
        case None=>
          None
        case Some(value)=>
          value.fold(None:Option[String]){value=>Some(value)}
      }
    }
    def apply(argName:ArgNameItem)={
      source.get(argName) match{
        case None=>
          throw new IllegalArgumentException(s"パラメータ：${argName.name}が見つかりませんでした")
        case Some(value)=>
          value match{
            case None=>
              throw new IllegalArgumentException(s"パラメータ：${argName.name}はパラメータ値未設定のパラメータなので、取得ができません")
            case Some(value)=>
              value
          }
          //value.fold(None:Option[String]){value=>Some(value)}
      }
    }


    def getValueOrElse(argName:ArgNameItem,default:String)={
      getValue(argName).getOrElse(default)
    }

    def exists(argName:ArgNameItem)=
      source.contains(argName)

    def keys=source.keys





  }

}

class ArgParser(argNames:Seq[ArgParser.ArgNameItem]) {

  import ArgParser._

  def parse(args:Array[String]): Args ={
    val iterator = args.iterator
    def next(argMap:Map[ArgNameItem,Option[String]]): Map[ArgNameItem,Option[String]] =
      if(iterator.hasNext){
        val value = iterator.next()
        argNames.find{_.names.contains(value)} match{
          case None=>
            next(argMap)
          case Some(argName)=>
            val name = argName.name
            if(argName.hasValue){
              if(!iterator.hasNext) throw new IllegalArgumentException(s"引数[${argName.name}]に引数値が見つかりませんでした")
              val argV = iterator.next()
              next(argMap + (argName -> Some(argV)))
            }
            else
              next(argMap + (argName -> None))
        }
      }
    else
        argMap
    val map = next(Map.empty)
    new Args(map)
  }

  def help()=
    argNames.map{argName=>
      s"${argName.names.mkString(" ")}\t${argName.desc}"
    }.mkString("\n")








}

package util

object using{

  def apply[T1 <: { def close():Any},T2](hasClose:T1)(func:T1=>T2)=
    applyBase(hasClose)(func){()=>hasClose.close()}

  def disconnect[T1 <: { def disconnect():Any},T2](hasDisconnect:T1)(func:T1=>T2)=
    applyBase(hasDisconnect)(func){()=>hasDisconnect.disconnect()}

  def applyBase[T1,T2](hasClose:T1)(func:T1=>T2)(funcClose:()=>Unit)=
    try func(hasClose) finally
      try funcClose()
      catch{
        case _:Throwable=>
      }


}

object Utils {

  /*
  def using[T1 <: { def close():Any},T2](hasClose:T1)(func:T1=>T2)={

    try func(hasClose) finally
      try hasClose.close()
      catch{
        case _:Throwable=>
      }


  }
  */
}


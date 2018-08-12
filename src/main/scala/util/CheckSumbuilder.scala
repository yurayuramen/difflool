package util

import java.io.File
import java.security.MessageDigest

object CheckSumBuilder{


  def makeByMessageDigest(algorithm:String,array:Array[Byte]):String =
    MessageDigest.getInstance(algorithm).digest(array).map( "%02x" format _).mkString.toUpperCase



}


trait CheckSumBuilder {

  def build(file:File):String={

    import IOUtils._
    build(file.readALLBytes())
  }

  def build(array: Array[Byte]):String
}

class Md5SumBuilder{
  def build(array: Array[Byte]):String=CheckSumBuilder.makeByMessageDigest("md5",array)
}

class Sha1SumBuilder
{
  def build(array: Array[Byte]):String=CheckSumBuilder.makeByMessageDigest("sha1",array)
}
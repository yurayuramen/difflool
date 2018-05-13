package ui


import java.awt.datatransfer.DataFlavor
import java.io.File

import internal.ToolCall
import javax.swing.TransferHandler.TransferSupport
import javax.swing.{JTextPane, TransferHandler}
import util.{ArgParser, IOUtils, using}

import scala.io.Source
import scala.swing.event._
import scala.swing.{BoxPanel, Component, Dimension, EditorPane, Frame, MainFrame, Menu, MenuBar, Orientation, ScrollPane, SimpleSwingApplication, SplitPane, TextArea}

object MainUI extends SimpleSwingApplication{

  import conf.ArgsDef
  import conf.ArgsDef._

  override def startup(params: Array[String]): Unit = {

    ArgsDef.init(params)
    super.startup(params)
  }

  class FrameWithEditorPaneOnly(textData:String,override val title:String="undefined") extends scala.swing.Frame {

    /*
    import scala.swing.BorderPanel
    import BorderPanel.Position
    contents = new BorderPanel {

      //val textPane = new EditorPane("text/plain",textData)
      val textArea = new TextArea(textData)

      add(textArea, Position.Center)
    }
    */
    contents = new BoxPanel(Orientation.Vertical){
      contents += new ScrollPane(new TextArea(textData))
    }
    override def closeOperation() = dispose()
  }


  class MyTransferHandler extends TransferHandler{

    import javax.swing.JComponent

    override def getSourceActions(c:JComponent)=
      // return NONE;
      // return MOVE;
      // return COPY;
      TransferHandler.COPY_OR_MOVE
      // return COPY_OR_MOVE | LINK;


    override def canImport(support:TransferSupport)={
      val dfs = support.getDataFlavors();
      dfs.exists{df=>
        import java.awt.datatransfer.DataFlavor
        DataFlavor.javaFileListFlavor.equals(df)

      }
    }

    override def importData(support:TransferSupport)={
      if (support.isDrop()) {
        // ドロップ処理
        val action = support.getDropAction();
        import TransferHandler._
        if (action == COPY || action == MOVE) {
          val t = support.getTransferable();
          val any = t.getTransferData(DataFlavor.javaFileListFlavor)
          import scala.collection.JavaConverters._

          val textPane = support.getComponent().asInstanceOf[JTextPane]

          val file = any.asInstanceOf[java.util.List[File]].asScala(0)
          if(file.isFile)
          {
            val enc = args.getValueOrElse(argNameEncoding,"utf-8")
            textPane.setText(Source.fromFile(file,enc).getLines().mkString("\n"))
          }
          else{
            textPane.setText(file.getCanonicalPath)
          }

          //println(s"${any.getClass.getName}/${any}")
          true
        }
        else
          false
      }
      else
        false
    }

  }

  class MyJTextPane extends JTextPane{
    this.setTransferHandler(new MyTransferHandler)
  }







  override def top: Frame = new MainFrame{

    title = "ファイル比較ツール"
    minimumSize = new Dimension(400,200)

    def buildMenu(title:String,cmd: String,stdoutEncoding:Option[String]=None)={
        new Menu(title){
          listenTo(mouse.clicks)
          reactions += {
            case e: MouseClicked =>

              val leftData = leftTextPane.getText
              val rightData = rightTextPane.getText

              val tmpDir = args(argNameTempDir)

              import IOUtils._

              s"${tmpDir}/${ToolCall.FileNameLeft}".writeALL(leftData)
              s"${tmpDir}/${ToolCall.FileNameRight}".writeALL(rightData)


              stdoutEncoding match{
                case Some(encoding)=>

                  val (stdout,stderr) = ToolCall(cmd,new File(tmpDir)).sync()
                  println(s"stdout*****\n${stdout}\n***************")

                  val subWindow = new FrameWithEditorPaneOnly(stdout)
                  subWindow.open()

                case _=>
                  val proc = ToolCall(cmd,new File(tmpDir)).async()
              }
          }//reactions
        }
        //contents += menu
    }

    menuBar = new MenuBar{

      val menuWinMerge=
      args.getValue(argNameWindiff).map{cmd=>
        buildMenu("winmerge",cmd)
      }

      val menuWinFc:Option[Menu]=
      if(args.exists(argNameWinFc))
        Some(buildMenu("win-fc","fc",Some("windows-31j")))
      else
        None

      val menuDiff:Option[Menu]=
        if(args.exists(argNameDiff))
          Some(buildMenu("diff","diff",Some("utf8")))
        else
          None

      val menuDirDiff = new Menu("dir-diff"){
        listenTo(mouse.clicks)
        reactions += {
          case e: MouseClicked =>

            def listFiles(current:File,list:Seq[File]=Nil):Seq[File]={
              if(current.isFile)
                list :+ current
              else{
                list ++ current.listFiles().flatMap{file=>
                  listFiles(file)
                }
              }
            }

            val left = leftTextPane.getText.split("\r\n|\n")(0).trim
            val right = rightTextPane.getText.split("\r\n|\n")(0).trim

            val leftRootFile = new File(left)
            val rightRootFile = new File(right)

            val leftRootPath = leftRootFile.getCanonicalPath
            val rightRootPath = rightRootFile.getCanonicalPath

            val leftData = listFiles(leftRootFile).map{_.getCanonicalPath.substring(leftRootPath.length)}.sortBy{v=>v}.mkString("\n")
            val rightData = listFiles(rightRootFile).map(_.getCanonicalPath.substring(rightRootPath.length)).sortBy{v=>v}.mkString("\n")

            import IOUtils._
            val tmpDir = args(argNameTempDir)

            s"${tmpDir}/${ToolCall.FileNameLeft}".writeALL(leftData)
            s"${tmpDir}/${ToolCall.FileNameRight}".writeALL(rightData)


            args.getValue(argNameWindiff).fold{
              //TODO winmergeが見つからない場合の挙動がやや雑

              val cmd =
              ArgsDef.os match{
                case Windows=>
                  "fc"
                case _=>
                  "diff"
              }

              val (stdout,stderr) = ToolCall(cmd,new File(tmpDir)).sync()
              println(s"stdout*****\n${stdout}\n***************")
              val subWindow = new FrameWithEditorPaneOnly(stdout)
              subWindow.open()

            }{winMerge=>
              ToolCall(winMerge,new File(tmpDir)).async()
            }


        }
      }

      contents ++= Seq(menuWinMerge , menuWinFc, menuDiff).flatten :+ menuDirDiff

    }//MenuBar

    /*
    val leftTextPane = new MyTextPane()
    val rightTextPane = new MyTextPane()
    contents = new SplitPane(Orientation.Vertical,leftTextPane,rightTextPane)
    */
    val leftTextPane = new MyJTextPane()
    val rightTextPane = new MyJTextPane()


    leftTextPane.setMinimumSize(new Dimension(150, 200))


    val splitPane =new SplitPane(Orientation.Vertical,Component wrap leftTextPane,Component wrap rightTextPane)
    //splitPane.
    contents = splitPane

  }

}

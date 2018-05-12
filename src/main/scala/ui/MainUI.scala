package ui


import java.awt.datatransfer.DataFlavor
import java.io.{File, FileOutputStream}

import internal.ToolCall
import javax.swing.TransferHandler.TransferSupport
import javax.swing.{JTextPane, TransferHandler}
import util.ArgParser.ArgNameItem
import util.{ArgParser, IOUtils}

import scala.io.Source
import scala.swing.event._
import scala.swing.{Component, Dimension, Frame, MainFrame, Menu, MenuBar, Orientation, SimpleSwingApplication, SplitPane, TextPane}

object MainUI extends SimpleSwingApplication{
  import ArgParser._

  val argNameTempDir = ArgNameItem("--temp-dir","-t")()
  val argNameWindiff = ArgNameItem("--win-merge","-w")()
  val argNameEncoding = ArgNameItem("--source-encoding","-e")()

  var args:Args = null

  override def startup(params: Array[String]): Unit = {

    this.args = new ArgParser(Seq(
      argNameTempDir
      ,argNameEncoding
      ,argNameWindiff
    )).parse(params)

    super.startup(params)
    /*
    val t = top
    if (t.size == new Dimension(0,0)) t.pack()
    t.visible = true
    */
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

          val list = any.asInstanceOf[java.util.List[File]].asScala

          val enc = args.getValueOrElse(argNameEncoding,"utf-8")

          support.getComponent().asInstanceOf[JTextPane].setText(Source.fromFile(list(0),enc).getLines().mkString("\n"))


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

    menuBar = new MenuBar{
      args.getValue(argNameWindiff).foreach{windiffPath=>
        val menu = new Menu("winmerge"){
          listenTo(mouse.clicks)
          reactions += {
            case e: MouseClicked =>

              val leftData = leftTextPane.getText
              val rightData = rightTextPane.getText

              val tmpDir = args(argNameTempDir)

              import IOUtils._

              s"${tmpDir}/${ToolCall.FileNameLeft}".writeALL(leftData)
              s"${tmpDir}/${ToolCall.FileNameRight}".writeALL(rightData)

              val proc = ToolCall(windiffPath,new File(tmpDir)).call()
          }//reactions
        }//val menu
        contents += menu
      }//foreach
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

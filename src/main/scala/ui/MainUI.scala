package ui


import java.awt.datatransfer.DataFlavor
import java.io.{File, FileOutputStream}

import internal.ToolCall
import javax.swing.TransferHandler.TransferSupport
import javax.swing.{JTextPane, TransferHandler}

import scala.io.Source
import scala.swing.event._
import scala.swing.{Component, Dimension, Frame, MainFrame, Menu, MenuBar, Orientation, SimpleSwingApplication, SplitPane, TextPane}

object MainUI extends SimpleSwingApplication{

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



          support.getComponent().asInstanceOf[JTextPane].setText(Source.fromFile(list(0),"utf8").getLines().mkString("\n"))


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
    minimumSize = new Dimension(300,200)

    menuBar = new MenuBar{
      val menu = new Menu("windiff"){
        listenTo(mouse.clicks)
        reactions += {
          case e: MouseClicked =>

            val leftData = leftTextPane.getText
            val rightData = rightTextPane.getText

            val tmp = "./tmp"
            val leftFile = "left"
            val rightFile = "right"

            val f1 = new FileOutputStream(s"${tmp}/${leftFile}")
            f1.write(leftData.getBytes("utf-8"))
            val f2 = new FileOutputStream(s"${tmp}/${rightFile}")
            f2.write(rightData.getBytes("utf-8"))
            f1.close()
            f2.close()


            val proc = ToolCall("G:\\appli\\manual\\WinMerge\\winmergeu.exe",new File(tmp)).call()
        }
      }
      contents += menu
      //override val menus = collection.mutable.Seq( menu )
    }

    /*
    val leftTextPane = new MyTextPane()
    val rightTextPane = new MyTextPane()
    contents = new SplitPane(Orientation.Vertical,leftTextPane,rightTextPane)
    */
    val leftTextPane = new MyJTextPane()
    val rightTextPane = new MyJTextPane()


    leftTextPane.setMinimumSize(new Dimension(200, 0))


    val splitPane =new SplitPane(Orientation.Vertical,Component wrap leftTextPane,Component wrap rightTextPane)
    //splitPane.
    contents = splitPane

  }

}

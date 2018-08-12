package ui


import java.awt.datatransfer.DataFlavor
import java.io.File
import java.nio.charset.Charset

import internal.ToolCall
import javax.swing.TransferHandler.TransferSupport
import javax.swing.{JTextArea, JTextPane, TransferHandler}
import util._

import scala.io.{Codec, Source}
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
    import java.awt.Font
    contents = new BoxPanel(Orientation.Vertical){
      val textArea = new TextArea(textData)
      textArea.peer.setFont(new Font("Arial", Font.PLAIN, 12))
      contents += new ScrollPane(textArea)
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



          if(file.isFile && !ArchiveTypes.defaultExtensions.exists{case(exten,_)=> file.getName.endsWith(s".${exten}") })
          {
            val enc = args.getValueOrElse(argNameEncoding,"utf-8")
            import IOUtils._
            textPane.setText(file.readALLString(Charset.forName(enc)))//Source.fromFile(file,enc).getLines().mkString("\n"))
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

    menuBar = new MenuBar{

      val menuFileDiff = new Menu("file-diff") {
        listenTo(mouse.clicks)
        reactions += {
          case e:MouseClicked =>
            val leftData = leftTextPane.getText
            val rightData = rightTextPane.getText

            val tmpDir = args(argNameTempDir)

            val leftFile = new File(s"${tmpDir}/${args.getValueOrElse(argNameTempLeftFile,"left.txt")}")
            val rightFile = new File(s"${tmpDir}/${args.getValueOrElse(argNameTempRightFile,"right.txt")}")

            import IOUtils._
            leftFile.writeALL(leftData)
            rightFile.writeALL(rightData)

            args.getValue(argNameTempDir).foreach{tmpDir=>

              args.getValue(argNameExecute).foreach{execute=>
                ToolCall(execute,leftFile,rightFile).async()
              }

            }

        }
      }

      val menuDirDiff = new Menu("dir-diff"){
        listenTo(mouse.clicks)
        reactions += {
          case e: MouseClicked =>
            val left = leftTextPane.getText.split("\r\n|\n")(0).trim
            val right = rightTextPane.getText.split("\r\n|\n")(0).trim
            val leftRootFile = new File(left)
            val rightRootFile = new File(right)

            val text = DiffUtilsWrapper.diff(leftRootFile,rightRootFile)
            new FrameWithEditorPaneOnly(textData=text).open()
            args.getValue(argNameTempDir).foreach{tmpDir=>

              val listLeft = DiffUtilsWrapper.format(leftRootFile)
              val listRight = DiffUtilsWrapper.format(rightRootFile)
              val leftFile = new File(s"${tmpDir}/${args.getValueOrElse(argNameTempLeftFile,"left.txt")}")
              val rightFile = new File(s"${tmpDir}/${args.getValueOrElse(argNameTempRightFile,"right.txt")}")

              import IOUtils._
              leftFile.writeALL(listLeft.mkString("\n"))
              rightFile.writeALL(listRight.mkString("\n"))

              args.getValue(argNameExecute).foreach{execute=>

                ToolCall(execute,leftFile,rightFile).async()
              }
            }
        }
      }

      contents += menuFileDiff
      contents += menuDirDiff

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

package sim.cloth

import javax.media.opengl.{GLCapabilities, GLProfile}
import com.jogamp.opengl.util.FPSAnimator
import javax.media.opengl.awt.GLCanvas
import java.awt.{event, Frame}
import java.awt.event.WindowAdapter

object VerletClothSim extends WindowAdapter {

  val animator: FPSAnimator = new FPSAnimator(60)

  def main(args: Array[String]): Unit = {
    val glProfile = GLProfile.getDefault
    val glCapabilities = new GLCapabilities(glProfile)
    val glCanvas = new GLCanvas(glCapabilities)

    glCanvas.addGLEventListener(new VerletClothScene())

    val frame = new Frame("Testing... 1, 2, 3")
    frame.setSize(600, 600)
    frame.addWindowListener(this)
    frame.add(glCanvas)
    frame.setVisible(true)

    animator.add(glCanvas)
    animator.start()
  }

  override def windowClosing(e: event.WindowEvent) {
    animator.stop()
    sys.exit(0)
  }
}

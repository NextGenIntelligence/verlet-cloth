package sim.glutil

import javax.media.opengl.GL
import com.jogamp.opengl.util.glsl.ShaderProgram

object GLUtil {

  implicit class RichGL(gl: GL) {

    val glx = gl.getGL2ES2

    def getAttributeData(program: ShaderProgram, attributeName: String): GLAttributeData = {
      val attributeLocation = gl.getGL2ES2.glGetAttribLocation(program.program(), attributeName)
      new GLAttributeData(gl, program, attributeLocation)
    }

  }

}

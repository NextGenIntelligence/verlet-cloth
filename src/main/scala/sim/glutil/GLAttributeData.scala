package sim.glutil

import javax.media.opengl.GL
import com.jogamp.opengl.util.glsl.ShaderProgram

class GLAttributeData(gl: GL, program: ShaderProgram, attributeLocation: Int) {

  private val glx = gl.getGL2ES2

  def setVerticesFromBuffer(vertices: GLFloatBuffer) = {
    glx.glEnableVertexAttribArray(attributeLocation)

    vertices.bind()
    glx.glVertexAttribPointer(attributeLocation, 3, GL.GL_FLOAT, false, 0, 0)
    glx.glDrawArrays(GL.GL_TRIANGLES, 0, vertices.size / 3)
    vertices.unbind()

    glx.glDisableVertexAttribArray(attributeLocation)
  }

}
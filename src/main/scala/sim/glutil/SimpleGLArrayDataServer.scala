package sim.glutil

import com.jogamp.opengl.util.GLArrayDataServer

object SimpleGLArrayDataServer {

  def apply(name: String, bufferable: GLBufferable): GLArrayDataServer = {
    GLArrayDataServer.createGLSL(name,
      bufferable.comps,
      bufferable.dataType,
      bufferable.normalized,
      bufferable.sizeRequired,
      bufferable.vboBufferUsage)
  }

}

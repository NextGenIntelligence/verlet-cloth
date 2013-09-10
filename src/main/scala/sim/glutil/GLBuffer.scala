package sim.glutil

import javax.media.opengl.GL
import java.nio.FloatBuffer
import sim.glutil.Vector3DUtil._

abstract class GLBuffer(gl: GL, buffer: Int, bufferType: Int) {

  protected val glx = gl.getGL2GL3

  protected var numElements = 0

  protected var deleted = false

  def delete() = {
    glx.glDeleteBuffers(1, Array(buffer), 0)
    deleted = true
  }

  def isDeleted: Boolean = deleted

  def size: Int = numElements

  def bind() = glx.glBindBuffer(bufferType, buffer)

  def unbind() = glx.glBindBuffer(bufferType, 0)

}

object GLBuffer {

  def createFloatBuffer(gl: GL, bufferType: Int): GLFloatBuffer = {
    val out = Array(-1)
    gl.getGL2ES2.glGenBuffers(1, out, 0)
    new GLFloatBuffer(gl, out(0), bufferType)
  }

}

class GLFloatBuffer(gl: GL, buffer: Int, bufferType: Int) extends GLBuffer(gl, buffer, bufferType) {

  private val BYTES_PER_FLOAT = 4

  def bufferVertices(data: Traversable[Vector3D], dynamic: Boolean) = {
    if (isDeleted) throw new IllegalStateException("Buffer is deleted")

    val flatData = data.flatMap(v => List(v.x, v.y, v.z)).toArray
    val dataBuffer = FloatBuffer.wrap(flatData)

    bind()
    glx.glBufferData(bufferType,
      flatData.size * BYTES_PER_FLOAT,
      dataBuffer,
      if (dynamic) GL.GL_DYNAMIC_DRAW else GL.GL_STATIC_DRAW)
    unbind()

    numElements = flatData.size
  }

}
package sim.glutil

import com.jogamp.opengl.util.GLArrayDataServer
import javax.media.opengl.GL


trait GLBufferable {

  def comps: Int

  def dataType: Int

  def normalized: Boolean

  def sizeRequired: Int

  def vboBufferUsage: Int

  def bufferData(gl: GL, array: GLArrayDataServer)

}

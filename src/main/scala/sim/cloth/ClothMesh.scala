package sim.cloth

import sim.glutil.Vector3DUtil._
import com.jogamp.opengl.util.GLArrayDataServer
import javax.media.opengl.GL

class ClothMesh(width: Float, length: Float) {

  val widthFaces = 48

  val lengthFaces = 48

  val widthVertices = widthFaces + 1

  val lengthVertices = lengthFaces + 1

  val particles =
    (0 until widthVertices).map(x => {
      (0 until lengthVertices).map(y => {
        val position = ((x.toFloat / widthFaces) * width - width / 2.0f,
          100.0f,
          (y.toFloat / lengthFaces) * length - length / 2.0f)
        new Particle(position, (x == widthFaces || x == 0) && (y == lengthFaces || y == 0))
      }).toArray
    }).toArray

  val verticalSprings = particles.flatMap(column => {
    column.sliding(2).map(p => new Spring(p(0), p(1)))
  })

  val horizontalSprings = particles.view.transpose.flatMap(row => {
    row.sliding(2).map(p => new Spring(p(0), p(1)))
  })

  val allSprings = verticalSprings ++ horizontalSprings

  def step(dt: Float) = {
    allSprings.foreach(_.apply())
    particles.foreach(_.foreach(_.verletIntegrate(dt)))
  }

  def createBuffer(name: String): GLArrayDataServer = {
    GLArrayDataServer.createGLSL(name,
      3, // 3 vector components per vertex.
      GL.GL_FLOAT,
      false, // Not normalized.
      widthVertices * lengthVertices * 6, // 3 vtx per tri, 2 tris per quad.
      GL.GL_DYNAMIC_DRAW)
  }

  def bufferData(gl: GL, array: GLArrayDataServer) {
    array.seal(gl, false)
    array.rewind()

    (0 until widthFaces).foreach(x => {
      (0 until lengthFaces).foreach(y => {
        val p1 = particles(x)(y).getPosition
        val p2 = particles(x + 1)(y).getPosition
        val p3 = particles(x + 1)(y + 1).getPosition
        val p4 = particles(x)(y + 1).getPosition

        // First tri.
        array.putf(p1.x)
        array.putf(p1.y)
        array.putf(p1.z)

        array.putf(p2.x)
        array.putf(p2.y)
        array.putf(p2.z)

        array.putf(p3.x)
        array.putf(p3.y)
        array.putf(p3.z)

        // Second tri.
        array.putf(p3.x)
        array.putf(p3.y)
        array.putf(p3.z)

        array.putf(p4.x)
        array.putf(p4.y)
        array.putf(p4.z)

        array.putf(p1.x)
        array.putf(p1.y)
        array.putf(p1.z)
      })
    })

    array.seal(true)
  }

}

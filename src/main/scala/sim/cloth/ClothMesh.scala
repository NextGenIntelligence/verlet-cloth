package sim.cloth

import sim.glutil.Vector3DUtil._
import com.jogamp.opengl.util.GLArrayDataServer
import javax.media.opengl.GL

class ClothMesh(width: Float, length: Float, collisionObjects: Array[Sphere]) {

  val GRAVITY = -980.7f

  val STRUCTURAL_STIFFNESS = 1.0f

  val STRUCTURAL_DAMPING = 0.5f

  val SHEAR_STIFFNESS = 1.5f

  val SHEAR_DAMPING = 0.75f

  val BEND_STIFFNESS = 2.0f

  val BEND_DAMPING = 1.0f

  val widthFaces = 48

  val lengthFaces = 48

  val widthVertices = widthFaces + 1

  val lengthVertices = lengthFaces + 1

  val particles =
    (0 until widthVertices).map(x => {
      (0 until lengthVertices).map(y => {
        val position = ((x.toFloat / widthFaces) * width - width / 2.0f,
          15.0f,
          (y.toFloat / lengthFaces) * length - length / 2.0f)
        new Particle(position, y == 0)
      }).toArray
    }).toArray

  val verticalSprings = particles.flatMap(column => {
    column.sliding(2).map(p => new SpringDamper(p(0), p(1), STRUCTURAL_STIFFNESS, STRUCTURAL_DAMPING))
  })

  val verticalBendSprings = particles.flatMap(column => {
    column.sliding(3).map(p => new SpringDamper(p(0), p(2), BEND_STIFFNESS, BEND_DAMPING))
  })

  val horizontalSprings = particles.view.transpose.flatMap(row => {
    row.sliding(2).map(p => new SpringDamper(p(0), p(1), STRUCTURAL_STIFFNESS, STRUCTURAL_DAMPING))
  })

  val horizontalBendSprings = particles.view.transpose.flatMap(row => {
    row.sliding(3).map(p => new SpringDamper(p(0), p(2), BEND_STIFFNESS, BEND_DAMPING))
  })

  val diagonalSprings = particles.sliding(2).flatMap(columns => {
    columns(0).zip(columns(1)).sliding(2).flatMap(particles => {
      Array(new SpringDamper(particles(0)._1, particles(1)._2, SHEAR_STIFFNESS, SHEAR_DAMPING),
        new SpringDamper(particles(0)._2, particles(1)._1, SHEAR_STIFFNESS, SHEAR_DAMPING))
    })
  })

  val allSprings = (verticalSprings ++ verticalBendSprings ++
    horizontalSprings ++ horizontalBendSprings ++ diagonalSprings).toVector

  def step(dt: Float) = {
    allSprings.foreach(_(dt))

    val particlesFlat = particles.flatten
    particlesFlat.foreach(_.applyGravity(GRAVITY * dt * dt))
    particlesFlat.foreach(_.verletIntegrate(dt))
    collisionObjects.foreach(obj => particlesFlat.foreach(_.solveCollision(obj)))
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

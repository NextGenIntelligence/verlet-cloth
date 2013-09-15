package sim.cloth

import sim.glutil.Vector3DUtil._
import com.jogamp.opengl.util.GLArrayDataServer
import javax.media.opengl.GL
import com.jogamp.common.nio.Buffers
import com.jogamp.opengl.util.glsl.ShaderProgram

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

  var buffer: Option[ClothMeshBuffer] = None

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

  def createBuffer(gl: GL, attributeName: String, shaderProgram: ShaderProgram) = {
    buffer = Some(ClothMeshBuffer(gl, attributeName, shaderProgram))
  }

  def destroyBuffer(gl: GL) = {
    buffer match {
      case Some(buf) => {
        buf.destroy(gl)
        buffer = None
      }
      case _ => // Do nothing!
    }
  }

  def bufferData(gl: GL) {
    val array = (0 until widthFaces).flatMap(x => {
      val y = (0 until lengthFaces).flatMap(y => {
        val p1 = particles(x)(y).getPosition
        val p2 = particles(x + 1)(y).getPosition
        val p3 = particles(x + 1)(y + 1).getPosition
        val p4 = particles(x)(y + 1).getPosition

        Array(p1.x, p1.y, p1.z,
          p2.x, p2.y, p2.z,
          p3.x, p3.y, p3.z,
          p3.x, p3.y, p3.z,
          p4.x, p4.y, p4.z,
          p1.x, p1.y, p1.z)
      }).toArray
      y
    }).toArray

    buffer match {
      case Some(buf) => {
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, buf.vertexBufferName)
        gl.glBufferData(GL.GL_ARRAY_BUFFER,
          array.size * Buffers.SIZEOF_FLOAT,
          Buffers.newDirectFloatBuffer(array),
          GL.GL_DYNAMIC_DRAW)
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0)
      }
      case _ => // Do nothing!
    }
  }

  def drawBuffer(gl: GL) = {
    val glx = gl.getGL2ES2

    buffer match {
      case Some(buf) => {
        glx.glEnableVertexAttribArray(buf.attributeLocation)
        glx.glBindBuffer(GL.GL_ARRAY_BUFFER, buf.vertexBufferName)
        glx.glVertexAttribPointer(buf.attributeLocation,
          3,
          GL.GL_FLOAT,
          false,
          0,
          0)
        glx.glDrawArrays(GL.GL_TRIANGLES, 0, widthFaces * lengthFaces * 2 /* 2 tris per quad */ * 3 /* 3 vertices per tri */)
        glx.glBindBuffer(GL.GL_ARRAY_BUFFER, 0)
        glx.glDisableVertexAttribArray(buf.attributeLocation)
      }
      case _ => // Do nothing!
    }
  }

  class ClothMeshBuffer(val attributeLocation: Int, val vertexBufferName: Int) {

    def destroy(gl: GL) = {
      gl.glDeleteBuffers(1, Array(vertexBufferName), 0)
    }

  }

  object ClothMeshBuffer {

    def apply(gl: GL, attributeName: String, shaderProgram: ShaderProgram) = {
      val attributeLocation = gl.getGL2ES2.glGetAttribLocation(shaderProgram.program, "vertex")
      val out = Array(-1)
      gl.getGL2ES2.glGenBuffers(1, out, 0)

      new ClothMeshBuffer(attributeLocation, out(0))
    }

  }
}

package sim.cloth

import sim.glutil.Vector3DUtil._
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

  private var buffer: Option[ClothMeshBuffer] = None

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

  private def index(x: Int, y: Int): Int = {
    x * widthVertices + y
  }

  /**
   * Grossly inefficient but readable way of calculating vertex normals.
   * @param x x-coordinate of particle
   * @param y y-coordinate of particle
   * @return the vertex normal for the particle
   */
  private def normal(x: Int, y: Int): Vector3D = {
    val pt = particles(x)(y)

    val (n, s) = y match {
      case y if y == 0 => {
        val a = particles(x)(y + 1).getPosition - pt.getPosition
        (a, -a)
      }
      case y if y == lengthFaces => {
        val b = particles(x)(y - 1).getPosition - pt.getPosition
        (-b, b)
      }
      case _ => (particles(x)(y + 1).getPosition - pt.getPosition, particles(x)(y - 1).getPosition - pt.getPosition)
    }

    val (e, w) = x match {
      case x if x == 0 => {
        val a = particles(x + 1)(y).getPosition - pt.getPosition
        (a, -a)
      }
      case x if x == widthFaces => {
        val b = particles(x - 1)(y).getPosition - pt.getPosition
        (-b, b)
      }
      case _ => (particles(x + 1)(y).getPosition - pt.getPosition, particles(x - 1)(y).getPosition - pt.getPosition)
    }

    ((s cross e) + (n cross w)).normalize
  }

  def createBuffer(gl: GL, attributeName: String, shaderProgram: ShaderProgram) = {
    val buf = ClothMeshBuffer(gl, attributeName, shaderProgram)

    (0 until widthFaces).foreach(x => {
      (0 until lengthFaces).foreach(y => {
        val p1 = index(x, y)
        val p2 = index(x + 1, y)
        val p3 = index(x + 1, y + 1)
        val p4 = index(x, y + 1)

        buf.elementBuffer.put(p1)
        buf.elementBuffer.put(p2)
        buf.elementBuffer.put(p3)
        buf.elementBuffer.put(p3)
        buf.elementBuffer.put(p4)
        buf.elementBuffer.put(p1)
      })
    })

    buf.elementBuffer.rewind()

    // Go ahead and buffer the elements array ahead of time.
    gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, buf.elementBufferName)
    gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER,
      buf.elementBufferSize * Buffers.SIZEOF_INT,
      buf.elementBuffer,
      GL.GL_STATIC_DRAW)
    gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0)

    buffer = Some(buf)
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
    buffer match {
      case Some(buf) => {
        (0 until widthVertices).foreach(x => {
          (0 until lengthVertices).foreach(y => {
            val p = particles(x)(y).getPosition

            buf.vertexBuffer.put(p.x)
            buf.vertexBuffer.put(p.y)
            buf.vertexBuffer.put(p.z)
          })
        })

        buf.vertexBuffer.rewind()

        (0 until widthVertices).foreach(x => {
          (0 until lengthVertices).foreach(y => {
            val n = normal(x, y)

            buf.normalBuffer.put(n.x)
            buf.normalBuffer.put(n.y)
            buf.normalBuffer.put(n.z)
          })
        })

        buf.normalBuffer.rewind()

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, buf.vertexBufferName)
        gl.glBufferData(GL.GL_ARRAY_BUFFER,
          buf.vertexBufferSize * Buffers.SIZEOF_FLOAT,
          buf.vertexBuffer,
          GL.GL_DYNAMIC_DRAW)

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, buf.normalBufferName)
        gl.glBufferData(GL.GL_ARRAY_BUFFER,
          buf.vertexBufferSize * Buffers.SIZEOF_FLOAT,
          buf.normalBuffer,
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
        glx.glEnableVertexAttribArray(buf.vertexAttributeLocation)
        glx.glEnableVertexAttribArray(buf.normalAttributeLocation)

        glx.glBindBuffer(GL.GL_ARRAY_BUFFER, buf.vertexBufferName)
        glx.glVertexAttribPointer(buf.vertexAttributeLocation,
          3,
          GL.GL_FLOAT,
          false,
          0,
          0)

        glx.glBindBuffer(GL.GL_ARRAY_BUFFER, buf.normalBufferName)
        glx.glVertexAttribPointer(buf.normalAttributeLocation,
          3,
          GL.GL_FLOAT,
          false,
          0,
          0)

        glx.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, buf.elementBufferName)
        glx.glDrawElements(GL.GL_TRIANGLES,
          buf.elementBufferSize,
          GL.GL_UNSIGNED_INT,
          0)

        glx.glBindBuffer(GL.GL_ARRAY_BUFFER, 0)
        glx.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0)
        glx.glDisableVertexAttribArray(buf.vertexAttributeLocation)
        glx.glDisableVertexAttribArray(buf.normalAttributeLocation)
      }
      case _ => // Do nothing!
    }
  }

  private class ClothMeshBuffer(val vertexAttributeLocation: Int,
                                val vertexBufferName: Int,
                                val normalAttributeLocation: Int,
                                val normalBufferName: Int,
                                val elementBufferName: Int) {

    val vertexBufferSize = widthVertices * lengthVertices * 3 /* 3 comps per vector */

    val vertexBuffer = Buffers.newDirectFloatBuffer(vertexBufferSize)

    val normalBuffer = Buffers.newDirectFloatBuffer(vertexBufferSize)

    val elementBufferSize = widthFaces * lengthFaces * 2 /* 2 tris per quad */ * 3 /* 3 vertices per tri */

    val elementBuffer = Buffers.newDirectIntBuffer(elementBufferSize)

    def destroy(gl: GL) = {
      gl.glDeleteBuffers(3, Array(vertexBufferName, normalBufferName, elementBufferName), 0)
    }

  }

  private object ClothMeshBuffer {

    def apply(gl: GL, attributeName: String, shaderProgram: ShaderProgram) = {
      val vertexAttributeLocation = gl.getGL2ES2.glGetAttribLocation(shaderProgram.program, "vertex")
      val normalAttributeLocation = gl.getGL2ES2.glGetAttribLocation(shaderProgram.program, "normal")

      val out = Array(-1, -1, -1)
      gl.getGL2ES2.glGenBuffers(3, out, 0)

      new ClothMeshBuffer(vertexAttributeLocation, out(0), normalAttributeLocation, out(1), out(2))
    }

  }
}

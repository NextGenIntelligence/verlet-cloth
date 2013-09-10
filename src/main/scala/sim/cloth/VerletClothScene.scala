package sim.cloth

import javax.media.opengl._
import javax.media.opengl.fixedfunc.GLMatrixFunc
import com.jogamp.opengl.util.PMVMatrix
import com.jogamp.opengl.util.glsl.{ShaderCode, ShaderProgram, ShaderState}
import sim.glutil.{GLAttributeData, GLBuffer, GLFloatBuffer}
import sim.glutil.GLUtil._
import sim.glutil.Vector3DUtil._

class VerletClothScene extends GLEventListener {

  private val shaderProgram = new ShaderProgram()
  private val shaderState = new ShaderState()

  private val mvp = new PMVMatrix()
  private val mvpUniform = new GLUniformData("mvp", 4, 4, mvp.glGetPMvMatrixf)

  private var posAttribute: GLAttributeData = null
  private var vertexBuffer: GLFloatBuffer = null

  private var theta = 0.0f
  private var s = 0.0f
  private var c = 0.0f

  def init(drawable: GLAutoDrawable) = {
    val gl = drawable.getGL
    val glx = gl.getGL2ES2

    val vertexShaderCode = ShaderCode.create(gl.getGL2ES2, GL2ES2.GL_VERTEX_SHADER, 1, getClass, Array("vertex.glsl"), false)
    shaderProgram.add(vertexShaderCode)

    val fragmentShaderCode = ShaderCode.create(gl.getGL2ES2, GL2ES2.GL_FRAGMENT_SHADER, 1, getClass, Array("frag.glsl"), false)
    shaderProgram.add(fragmentShaderCode)

    shaderProgram.link(glx, System.err)
    shaderProgram.validateProgram(glx, System.err)
    shaderProgram.useProgram(glx, true)

    shaderState.attachShaderProgram(glx, shaderProgram, true)
    shaderState.ownUniform(mvpUniform)

    posAttribute = gl.getAttributeData(shaderProgram, "pos")
    vertexBuffer = GLBuffer.createFloatBuffer(gl, GL.GL_ARRAY_BUFFER)
  }

  def dispose(drawable: GLAutoDrawable) = {
    vertexBuffer.delete()
  }

  def display(drawable: GLAutoDrawable) = {
    update(drawable)
    render(drawable)
  }

  def reshape(drawable: GLAutoDrawable, x: Int, y: Int, width: Int, height: Int) = {}

  def update(drawable: GLAutoDrawable) = {
    theta += 0.01f
    s = Math.sin(theta).toFloat
    c = Math.cos(theta).toFloat

    // Set up model-view-projection matrix.
    mvp.glMatrixMode(GLMatrixFunc.GL_MODELVIEW)
    mvp.glLoadIdentity()
    mvp.glTranslatef(0.0f, 0.0f, -4.0f)
    mvp.gluLookAt(0.0f, 2.0f, 0.0f,   // Eye
      0.0f, -2.0f, -4.0f,             // Target
      0.0f, 1.0f, 0.0f)               // Up vector
    mvp.glMatrixMode(GLMatrixFunc.GL_PROJECTION)
    mvp.glLoadIdentity()
    mvp.gluPerspective(45.0f, 1.0f, 0.1f, 1000.0f)

    shaderState.uniform(drawable.getGL.getGL2ES2, mvpUniform)

    // Update vertices.
    val vertices = Array((-c,  -c, 0.0f),
      (0.0f, c, 0.0f),
      (s, -s, 0.0f))

    vertexBuffer.bufferData(vertices, dynamic = true)
  }

  def render(drawable: GLAutoDrawable) = {
    val glx = drawable.getGL.getGL2ES2

    glx.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
    glx.glClear(GL.GL_COLOR_BUFFER_BIT)

    posAttribute.setVerticesFromBuffer(vertexBuffer)
  }
}

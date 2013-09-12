package sim.cloth

import javax.media.opengl._
import javax.media.opengl.fixedfunc.GLMatrixFunc
import com.jogamp.opengl.util.PMVMatrix
import com.jogamp.opengl.util.glsl.{ShaderCode, ShaderProgram, ShaderState}

class VerletClothScene extends GLEventListener {

  private val cloth = new ClothMesh(50.0f, 50.0f)

  private val shaderProgram = new ShaderProgram()
  private val shaderState = new ShaderState()

  private val mvp = new PMVMatrix()
  private val mvpUniform = new GLUniformData("mvp", 4, 4, mvp.glGetPMvMatrixf)

  private val vertices = cloth.createBuffer("vertex")

  private var screenWidth = 1
  private var screenHeight = 1
  private var lastTime = 0l
  private val initialTime = System.currentTimeMillis()

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
    shaderState.ownAttribute(vertices, true)

    gl.getGL2.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_LINE)
  }

  def dispose(drawable: GLAutoDrawable) = {
    vertices.destroy(drawable.getGL)
  }

  def display(drawable: GLAutoDrawable) = {
    update(drawable)
    render(drawable)
  }

  def reshape(drawable: GLAutoDrawable, x: Int, y: Int, width: Int, height: Int) = {
    screenWidth = width
    screenHeight = height
  }

  def update(drawable: GLAutoDrawable) = {
    val gl = drawable.getGL
    val currentTime = System.currentTimeMillis()

    if (currentTime - lastTime >= 15) {
      lastTime = currentTime
      cloth.step(15.0f/1000.0f)

      // Update vertices.
      cloth.bufferData(gl, vertices)
      vertices.enableBuffer(gl, false)
    }

    // Set up model-view-projection matrix.
    mvp.glMatrixMode(GLMatrixFunc.GL_MODELVIEW)
    mvp.glLoadIdentity()
    mvp.glTranslatef(0.0f, 0.0f, -4.0f)
    mvp.gluLookAt(0.0f, 40.0f, -80.0f,  // Eye
      0.0f, 0.0f, 0.0f,                   // Target
      0.0f, 1.0f, 0.0f)                   // Up vector
    //mvp.glRotatef(((currentTime - initialTime) * 360.0f) / 8000.0f, 0, 1, 0)
    mvp.glMatrixMode(GLMatrixFunc.GL_PROJECTION)
    mvp.glLoadIdentity()
    mvp.gluPerspective(45.0f, screenWidth.toFloat / screenHeight, 0.1f, 10000.0f)

    shaderState.uniform(drawable.getGL.getGL2ES2, mvpUniform)
  }

  def render(drawable: GLAutoDrawable) = {
    val glx = drawable.getGL.getGL2ES2

    glx.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
    glx.glClear(GL.GL_COLOR_BUFFER_BIT)

    vertices.enableBuffer(glx, true)
    glx.glDrawArrays(GL.GL_TRIANGLES, 0, vertices.getElementCount)
    vertices.enableBuffer(glx, false)
  }
}

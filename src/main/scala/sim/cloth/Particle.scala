package sim.cloth

import sim.glutil.Vector3DUtil._

class Particle(private var position: Vector3D, sticky: Boolean) {

  private var previousPosition = position
  private var acceleration = Vector3D.ZERO

  private val mass = 1.0f
  private val damping = 20.0f
  private val gravity = -392.0f

  private def applyForce(force: Vector3D) {
    acceleration += force / mass
  }

  def getMass: Float = mass

  def isSticky: Boolean = sticky

  def getPosition: Vector3D = position

  def setPosition(newPosition: Vector3D) = {
    position = newPosition
  }

  def verletIntegrate(dt: Float) = {
    if (!sticky) {
      val gravityForce = (0.0f, mass * gravity, 0.0f)
      applyForce(gravityForce)

      val velocity = position - previousPosition
      acceleration -= velocity * damping / mass
      val nextPosition = position + velocity + (acceleration * 0.5f * dt * dt)

      previousPosition = position
      position = nextPosition
      acceleration = Vector3D.ZERO
    }
  }

}

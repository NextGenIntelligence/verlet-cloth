package sim.cloth

import sim.glutil.Vector3DUtil._

class Particle(private var position: Vector3D, sticky: Boolean) {

  private var previousPosition = position
  private var forces = Vector3D.ZERO

  private val mass = 0.1f

  def applyForce(force: Vector3D) = {
    forces += force
  }

  def applyGravity(gravity: Float) = {
    forces += (0.0f, mass * gravity, 0.0f)
  }

  def solveCollision(sphere: Sphere) = {
    if (sphere.hitTest(position)) {
      val surfacePoint = sphere.surfacePoint(position)
      applyForce(surfacePoint - position)
      setPosition(surfacePoint)
    }
  }

  def getMass: Float = mass

  def isSticky: Boolean = sticky

  def getPosition: Vector3D = position

  def setPosition(newPosition: Vector3D) = {
    position = newPosition
  }

  def getVelocity(dt: Float): Vector3D = {
    (position - previousPosition) / dt
  }

  def verletIntegrate(dt: Float) = {
    if (!sticky) {
      // See <http://www.cs.cmu.edu/afs/cs/academic/class/15462-s13/www/lec_slides/Jakobsen.pdf>.
      val nextPosition = position + (position - previousPosition) + (forces / mass * dt * dt)
      previousPosition = position
      position = nextPosition
      forces = Vector3D.ZERO
    }
  }

}

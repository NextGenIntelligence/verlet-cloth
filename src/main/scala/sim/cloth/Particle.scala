package sim.cloth

import sim.glutil.Vector3DUtil._

class Particle(private var position: Vector3D, sticky: Boolean) {

  private var previousPosition = position
  private var previousDt = 1.0f
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

  def getVelocity: Vector3D = {
    (position - previousPosition) / previousDt // Converts speed per dt to global speed.
  }

  def getAcceleration: Vector3D = {
    forces / mass
  }

  def verletIntegrate(dt: Float) = {
    if (!sticky) {
      // See <http://www.cs.cmu.edu/afs/cs/academic/class/15462-s13/www/lec_slides/Jakobsen.pdf>.
      // We calculate the current velocity on the fly using our position and the last position.
      // By not saving the velocity, we get collision adjustment for free by simply pushing the
      // point out after-the-fact, thereby changing the velocity.
      val nextPosition = position + (getVelocity * dt) + (getAcceleration * dt * dt)

      previousPosition = position
      position = nextPosition
      previousDt = dt

      // Clear all forces for next iteration.
      forces = Vector3D.ZERO
    }
  }

}

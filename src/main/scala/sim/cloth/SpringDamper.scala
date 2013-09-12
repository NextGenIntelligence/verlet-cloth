package sim.cloth

import sim.glutil.Vector3DUtil._

class SpringDamper(p1: Particle, p2: Particle, stiffness: Float, damping: Float) {

  val restLength = (p1.getPosition - p2.getPosition).length

  private def getForces(dt: Float): (Vector3D, Vector3D) = {
    val diffPosition = p1.getPosition - p2.getPosition
    val diffPositionUnit = diffPosition.normalize
    val diffVelocity = p1.getVelocity(dt) - p2.getVelocity(dt)

    val distanceBetween = diffPosition.length

    val springTerm = -stiffness * (distanceBetween - restLength)
    val dampingTerm = -damping * (diffVelocity dot diffPositionUnit)

    val totalForce = (springTerm + dampingTerm) * diffPosition.normalize

    (totalForce, -totalForce)
  }

  def apply(dt: Float) = {
    val forces = getForces(dt)
    p1.applyForce(forces._1)
    p2.applyForce(forces._2)
  }

}

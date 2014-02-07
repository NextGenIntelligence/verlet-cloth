package sim.cloth

import sim.glutil.Vector3DUtil._

class SpringDamper(p1: Particle, p2: Particle, stiffness: Float, damping: Float) {

  val restLength = (p1.getPosition - p2.getPosition).length

  private def getForces(xi: Vector3D, vi: Vector3D, xj: Vector3D, vj: Vector3D): Vector3D = {
    val diffPosition = xi - xj
    val diffPositionUnit = diffPosition.normalize
    val diffVelocity = vi - vj
    val distanceBetween = diffPosition.length

    // F_s = -k_s * (dist(p_i, p_j) - restLength).
    // Dimensional analysis: (N) = (N/m) * (m).
    val springTerm = -stiffness * (distanceBetween - restLength)
    // F_d = -k_d * (dist(v_i, v_j) dot normalize(dist(p_i, p_j))).
    // (N) =
    val dampingTerm = -damping * (diffVelocity dot diffPositionUnit)

    // Total force (F_tot) = spring force (F_s) + damping force (F_d).
    val totalForce = springTerm + dampingTerm

    // Force vector = F_tot * normalize(dist(p_i, p_j)).
    totalForce * diffPositionUnit
  }

  private def getForces(dt: Float): Vector3D = {
    getForces(p1.getPosition, p1.getVelocity(dt), p2.getPosition, p2.getVelocity(dt))
  }

  def apply(dt: Float) = {
    val forces = getForces(dt)
    p1.applyForce(forces)
    p2.applyForce(-forces)
  }

}

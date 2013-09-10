package sim.cloth

import sim.glutil.Vector3DUtil._

class Spring(p1: Particle, p2: Particle) {

  val restLength = (p1.getPosition - p2.getPosition).length

  val stiffness = 1.0f

  def apply() = {
    val delta = p1.getPosition - p2.getPosition
    val deltaLength = delta.length
    val diff = (restLength - deltaLength) / deltaLength

    val rm1 = 1.0f / p1.getMass
    val rm2 = 1.0f / p2.getMass

    if (!p1.isSticky)
      p1.setPosition(p1.getPosition + delta * (rm1 / (rm1 + rm2)) * stiffness * diff)

    if (!p2.isSticky)
      p2.setPosition(p2.getPosition - delta * (rm2 / (rm1 + rm2)) * stiffness * diff)
  }

}

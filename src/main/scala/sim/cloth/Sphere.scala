package sim.cloth

import sim.glutil.Vector3DUtil.Vector3D

class Sphere(val radius: Float) {

  val origin = Vector3D.ZERO

  def hitTest(point: Vector3D): Boolean = {
    (point - origin).length <= radius
  }

  def normalAtPoint(point: Vector3D): Vector3D = {
    (point - origin).normalize
  }

//  /**
//   * Given a particle's point and its previous point, move the particle if it collides with the sphere.
//   * @param point the particle's current simulated point
//   * @param previousPoint the particle's previous simulated point
//   * @return the point on the surface at which a ray between the two points intersects the sphere
//   */
//  def surfacePoint(point: Vector3D, previousPoint: Vector3D): Vector3D = {
//    val ray = (point - previousPoint).normalize
//
//    val dLeftTerm = -(ray dot (previousPoint - origin))
//    val dRightTermUnder = Math.pow(ray dot (previousPoint - origin), 2) - ((previousPoint - origin) dot (previousPoint - origin)) + Math.pow(radius, 2)
//    if (dRightTermUnder < 0.0f) {
//      point // Sanity check; wasn't really a collision!
//    } else {
//      val dRightTerm = Math.sqrt(dRightTermUnder)
//
//      val dist1 = dLeftTerm + dRightTerm
//      val dist2 = dLeftTerm - dRightTerm
//
//      val intersectPoint = previousPoint + ray * Math.min(dist1, dist2).toFloat
//    }
//  }

  /**
   * Given a particle's point, move the particle to the closest point on the sphere's surface.
   * @param point the particle's current simulated point
   * @return the closest point on the surface of the sphere
   */
  def surfacePoint(point: Vector3D): Vector3D = {
    point.normalize * radius
  }

}

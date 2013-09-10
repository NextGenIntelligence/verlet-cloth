package sim.glutil

object Vector3DUtil {

  implicit class Vector3D(u: Triple[Float, Float, Float]) {

    def x: Float = {
      u._1
    }

    def y: Float = {
      u._2
    }

    def z: Float = {
      u._3
    }

    def dot(v: Vector3D): Float = {
      u.x * v.x + u.y * v.y + u.z * v.z
    }

    def *(k: Float): Triple[Float, Float, Float] = {
      (u.x * k, u.y * k, u.z * k)
    }

    def +(v: Vector3D): Triple[Float, Float, Float] = {
      (u.x + v.x, u.y + v.y, u.z + v.z)
    }

    def length = {
      Math.sqrt(u dot u)
    }

  }

  implicit class RichFloat(k: Float) {

    def *(u: Triple[Float, Float, Float]) = {
      u * k
    }

  }

}

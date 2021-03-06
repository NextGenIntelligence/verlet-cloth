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

    def cross(v: Vector3D): Triple[Float, Float, Float] = {
      (u.y * v.z - u.z * v.y, u.z * v.x - u.x * v.z, u.x * v.y - u.y * v.x)
    }

    def *(k: Float): Triple[Float, Float, Float] = {
      (u.x * k, u.y * k, u.z * k)
    }

    def /(k: Float): Triple[Float, Float, Float] = {
      (u.x / k, u.y / k, u.z / k)
    }

    def +(v: Vector3D): Triple[Float, Float, Float] = {
      (u.x + v.x, u.y + v.y, u.z + v.z)
    }

    def -(v: Vector3D): Triple[Float, Float, Float] = {
      (u.x - v.x, u.y - v.y, u.z - v.z)
    }

    def unary_- = {
      (-u.x, -u.y, -u.z)
    }

    def length: Float = {
      Math.sqrt(u dot u).toFloat
    }

    def normalize: Vector3D = {
      u / u.length
    }

    override def toString: String = {
      f"<$x,$y,$z>"
    }

  }

  object Vector3D {

    val ZERO = Vector3D(0.0f, 0.0f, 0.0f)

    def apply(x: Float, y: Float, z: Float): Vector3D = {
      (x, y, z)
    }

  }

  implicit class RichFloat(k: Float) {

    def *(u: Triple[Float, Float, Float]): Triple[Float, Float, Float] = {
      u * k
    }

    def *(u: Vector3D): Triple[Float, Float, Float] = {
      u * k
    }

  }

}

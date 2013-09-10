package sim.cloth

import sim.glutil.Vector3DUtil._

class ClothMesh(width: Float, length: Float) {

  val widthFaces = 48

  val lengthFaces = 48

  val widthVertices = widthFaces + 1

  val lengthVertices = lengthFaces + 1

  val particles =
    (0 until widthVertices).map(x => {
      (0 until lengthVertices).map(y => {
        val position = ((x.toFloat / widthFaces) * width - width / 2.0f,
          100.0f,
          (y.toFloat / lengthFaces) * length - length / 2.0f)
        new Particle(position, (x == widthFaces || x == 0) && (y == lengthFaces || y == 0))
      }).toArray
    }).toArray

  val horizontalSprings =
    (0 until widthFaces).flatMap(x => {
      (0 until lengthVertices).map(y => {
        new Spring(particles(x)(y), particles(x + 1)(y))
      })
    }).toVector

  val verticalSprings =
    (0 until widthVertices).flatMap(x => {
      (0 until lengthFaces).map(y => {
        new Spring(particles(x)(y), particles(x)(y + 1))
      })
    }).toVector

  val allSprings = horizontalSprings ++ verticalSprings

  def step(dt: Float) = {
    allSprings.foreach(_.apply())
    particles.foreach(_.foreach(_.verletIntegrate(dt)))
  }

  def getTris: Array[Vector3D] = {
    (0 until widthFaces).flatMap(x => {
      (0 until lengthFaces).flatMap(y => {
        val p1 = particles(x)(y).getPosition
        val p2 = particles(x + 1)(y).getPosition
        val p3 = particles(x + 1)(y + 1).getPosition
        val p4 = particles(x)(y + 1).getPosition

        List(p1, p2, p3,
          p3, p4, p1)
      })
    }).toArray
  }

}

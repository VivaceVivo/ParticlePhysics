import sbt._

class PhysicsProject(info: ProjectInfo) extends DefaultProject(info)
{
  lazy val hi = task { println("Hello World"); None }
  val scalaSwing = "org.scala-lang" % "scala-swing" % "2.8.1"
  override def mainClass : Option[String] = Some("particlephysics.ParticleBox")
}
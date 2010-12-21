package particlephysics

import scala.swing._
import scala.swing.{ MainFrame, Panel }
import scala.swing.event._
import java.awt.{ Color, Dimension, Graphics2D, Point }

import particlephysics.particle.Particle
import particlephysics.container.ParticleBox2D

/**
 *
 */
object MainSwing extends SimpleSwingApplication {
  lazy val ui = new Panel {
    background = Color.white

    val lIni = 300
    val dim: Dimension = new Dimension(lIni, lIni)
    preferredSize = (dim)
    focusable = true

    val ort = new Point()
    val diameter: Int = 0
    val pBox = initBox2()

//    pBox.boxReport();
    pBox.calcCollisionEventsForAllParticles();
    pBox.particles = pBox.listOrderedByCollisionTime()
    // Das Teilchen das als nächstes kollidiert steht an erster Stelle...
    pBox.realNextCollisionEvent = pBox.particles(0).nextCollisionEvent
//    pBox.particles.foreach(p => println("Next collision [s]: " + p.nextCollisionEventIn))
    println("Nächste Kollision in: " + pBox.realNextCollisionEvent.collisionTime + " s")
    
    override def paintComponent(g: Graphics2D) = {
      super.paintComponent(g)
      g.setColor(new Color(100, 100, 100))
      g.setColor(Color.black)
      def drawParticle(x: Int, y: Int, r: Int) {
        g.drawOval(x - r, y - r, 2 * r, 2 * r)
      }
      for (p <- pBox.particles) {
        val x = (p.position.x * lIni).toInt
        val y = (p.position.y * lIni).toInt
        val rad = (p.radius * lIni).toInt
        drawParticle(x, y, rad)
      }
    }
  }

  def top = new MainFrame {

    title = "Partikel-Box"
    //   contents = ui
    val buttonStart = new Button {
      text = "Step"
    }
    val buttonReset = new Button {
      text = "Reset"
    }

    contents = new BoxPanel(Orientation.Vertical) {
      contents += ui
      contents += buttonStart
      contents += buttonReset
      border = Swing.EmptyBorder(30, 30, 10, 30)

      listenTo(buttonStart, buttonReset)
      reactions += {
        case ButtonClicked(b) => {
          b.text match{
            case "Step" => {
              ui.pBox.nextTimeStep(1.0)
              repaint
              println(ui.pBox.particleReport())
            }
            case "Reset" => {
              contents -= ui
              contents += ui
            }
          }
        }
      }
    }
  }

  def initBox1(): ParticleBox2D = {

    val n = 1
    val particleMass = 0.1
    val particleRadius: Double = 0.04
    val boxWidth: Double = 1.0
    val boxHeigth: Double = 1.0

    var particleList: List[Particle] = Nil
    var x = 0.8
    var y = 0.8
    var newVec = new Vector2D(x, y)
    particleList = (new Particle(particleMass, particleRadius, newVec, newVec * -0.1, 1)) :: particleList

    new ParticleBox2D(boxWidth, boxHeigth, particleList, 0.0)
  }

  def initBox2(): ParticleBox2D = {

    val n = 2
    val particleMass = 0.1
    val particleRadius: Double = 0.04
    val boxWidth: Double = 1.0
    val boxHeigth: Double = 1.0

    var particleList: List[Particle] = Nil
    var x: Double = 0.2
    var y: Double = 0.2
    var newVec = new Vector2D(x, y)
    particleList = (new Particle(particleMass, particleRadius, newVec, newVec * 0.1, 1)) :: particleList
    x = 0.8
    y = 0.8
    newVec = new Vector2D(x, y)
    particleList = (new Particle(particleMass, particleRadius, newVec, newVec * -0.1, 2)) :: particleList

    new ParticleBox2D(boxWidth, boxHeigth, particleList, 0.0)
  }

}

package particlephysics

/**
 * User: Andreas_2
 * Date: 13.11.2010
 * Time: 23:31:57
 */

import container.ParticleBox2D
import particle.Particle
import swing._
import actors._
import actors.Actor._
import java.awt.event.ActionEvent
import java.awt.Color
import javax.swing.{Timer, UIManager}
import java.awt.event.ActionListener
import org.slf4j.LoggerFactory;

object ParticleBox extends SimpleGUIApplication {
  def logger = LoggerFactory.getLogger(ParticleBox.getClass);

  val deltaT: Int = 50
  val frameWidth = 640
  val frameHeight = 480
  val numberOfParticles = 30
  val particleRadius: Double = 0.02
  val velFactor = 0.5

  //Main
  def top = new MainFrame {

    // 1. Define some actions
    val receiver: Actor = actor {
      while (true) {
        receive {
          //         case (increment:Int) => pbar.value=increment
          case (dt: Double) => {
            ui.pBox.nextTimeStep(dt)
            repaint()
            logger.debug(ui.pBox.particleReport())
          }
        }
      }
    }

    val timerSwing: Timer = new Timer(deltaT, new ActionListener() {
      override def actionPerformed(e: ActionEvent) {
        doRepaintAction()
      }
    });

    def doRepaintAction() {
      ui.pBox.nextTimeStep(deltaT / 1000.0)
      labelZeit.text = "Zeit [s]: " + ui.pBox.getTime.toInt
      repaint()
      logger.debug(ui.pBox.particleReport())
    }

    val startSimulation = Action("Start") {
      timerSwing.start
      //      logger.debug("Setze running = true")
      //      timerActor.running = true
      //      timerActor ! receiver
    }

    //stops Simulation
    val pauseSimulation = Action("Pause") {
      //      logger.debug("Setze running = false")
      timerSwing.stop
      //      timerActor.running = false
    }

    //action exit
    val quitAction = Action("Quit") {System.exit(0)}

    //action displaying a Dialog
    val diagAction = Action("Statistik") {
      Dialog.showMessage(secondNewPanel,
        "Anzahl Kollisionen: " + ui.pBox.numberOfCollisions + "\nAnzahl zusätzlicher Teilchenberechnungen: " + ui.pBox.numberOfAdditionalParticleCalcs)
    }


    // 2. Set up the GUI
    //Look and Feel
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      //       UIManager.setLookAndFeel(new com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel);
    } catch {
      case _ => {
        logger.warn("Using default look&feel!")
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      }
    }

    //window title and icon
    title = "Teilchen in einer Box (2-Dimensional)"
    //iconImage = java.awt.Toolkit.getDefaultToolkit.getImage(resourceFromClassloader("C:\tikiwiki-1.9.9\img\tiki\tikibutton.png"))

    //arrange size and center frame on screen
    //    val screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize()
    //    location=new java.awt.Point((screenSize.width-framewidth)/2, (screenSize.height-frameheight)/2)
    //    minimumSize=new java.awt.Dimension(framewidth, frameheight)

    //the menubar with menuitems connected to actions
    menuBar = new MenuBar {
      contents += new Menu("File") {
        contents += new MenuItem(quitAction)
      }
      contents += new Menu("Run") {
        contents += new MenuItem(startSimulation)
        contents += new MenuItem(pauseSimulation)
      }
    }
    val labelAnzahl = new Label {text = "Anzahl:   " + numberOfParticles}
    val labelZeit = new Label {text = "Zeit [s]: " + 0}

    //frame contents with buttons connected to actions
    val firstpanel = new BoxPanel(Orientation.Vertical) {
      contents += new Button {action = startSimulation}
      contents += new Button {action = pauseSimulation}
      contents += labelAnzahl
      contents += labelZeit
      contents += new Button {action = diagAction}
    }

    //    val secondNewPanel = ui
    val secondNewPanel = new BoxPanel(Orientation.Horizontal) {
      contents += ui
      border = Swing.EmptyBorder(30, 30, 30, 30)
    }

    contents = new SplitPane(Orientation.Vertical, firstpanel, secondNewPanel) {
      //       dividerLocation=100
      dividerSize = 0
      oneTouchExpandable = false
    }

  }

  lazy val ui = new Panel {
    background = Color.white

    val lIni = 460
    val dim: Dimension = new Dimension(lIni, lIni)
    preferredSize = (dim)
    focusable = true
    val pBox = initBox(numberOfParticles)

    //  Geschwindigkeitsabhängige Farbe der Teilchen einsachalten:
        pBox.energieDependentColor = true

    //    pBox.boxReport();
    pBox.calcCollisionEventsForAllParticles();
    pBox.particles = pBox.listOrderedByCollisionTime()
    // Das Teilchen das als nächstes kollidiert steht an erster Stelle...
    pBox.realNextCollisionEvent = pBox.particles(0).nextCollisionEvent
    //    pBox.particles.foreach(p => println("Next collision [s]: " + p.nextCollisionEventIn))
    logger.info("Nächste Kollision in: {} s", pBox.realNextCollisionEvent.collisionTime)

    override def paintComponent(g: Graphics2D) = {

      super.paintComponent(g)
      def drawParticle(p: Particle) {
        val x = (p.position.x * lIni).toInt
        val y = (p.position.y * lIni).toInt
        val r = (p.radius * lIni + 2).toInt

        g.setColor(p.color)
        g.fillOval(x - r, y - r, 2 * r, 2 * r)
      }
      pBox.particles.foreach(p => drawParticle(p))
    }
  }

  def initBox(n: Int): ParticleBox2D = {
    val particleMass = 0.1
    val boxWidth: Double = 1.0
    val boxHeigth: Double = 1.0


    var x: Double = 0.0
    var y: Double = 0.0

    val particleColor: Color = Color.blue

    var particleList: List[Particle] = Nil
    n match {
      case 1 => { //Teilchen wird in die Mitte gesetzt
        x = 0.5
        y = 0.5
        val newPosVec = new Vector2D(x, y)
        particleList = (new Particle(particleMass, particleRadius, newPosVec, newPosVec * velFactor, Color.blue, 1)) :: particleList
      }
      case 2 => { //Teilchen werden auf die Diagonale gesetzt
        x = 0.3
        y = 0.3
        val newPosVec1 = new Vector2D(x, y)
        particleList = (new Particle(particleMass, particleRadius, newPosVec1, newPosVec1 * velFactor, Color.blue, 1)) :: particleList
        x = 0.7
        y = 0.7
        val newPosVec2 = new Vector2D(x, y)
        particleList = (new Particle(particleMass, particleRadius, newPosVec2, newPosVec2 * velFactor, Color.red, 2)) :: particleList
      }
      case _ => { //Teilchen werden auf ein quadratisches Raster verteilt (jeweils in die Mitte eines Rasterquadrats)
        val nPerSide = (Math.sqrt(1.0 * n)).round.toInt + 1
        logger.info("nPerSide: {}", nPerSide)
        val dl = (boxWidth - 2 * particleRadius) / nPerSide
        for (i <- 0 until nPerSide;
             j <- 0 until nPerSide
             if (i * nPerSide + j < n)) {
          x = particleRadius + (dl / 2) + i * dl
          y = particleRadius + (dl / 2) + j * dl
          val newPosVec = new Vector2D(x, y)
          if ((i == 0) && (j == 0))
            particleList = (new Particle(particleMass, particleRadius, newPosVec, newPosVec * velFactor, Color.red, 1)) :: particleList
          else
            particleList = (new Particle(particleMass, particleRadius, newPosVec, newPosVec * velFactor, particleColor, i * nPerSide + j + 1)) :: particleList
        }
      }
    }

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
    particleList = (new Particle(particleMass, particleRadius, newVec, newVec * 0.1, new Color(0, 0, 0), 1)) :: particleList
    x = 0.8
    y = 0.8
    newVec = new Vector2D(x, y)
    particleList = (new Particle(particleMass, particleRadius, newVec, newVec * -0.1, new Color(0, 0, 0), 2)) :: particleList

    new ParticleBox2D(boxWidth, boxHeigth, particleList, 0.0)
  }
}
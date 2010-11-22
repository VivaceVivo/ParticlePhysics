package particlephysics.particle

import particlephysics.{CollisionEvent, Vector2D, Position, Velocity}
import org.slf4j.LoggerFactory
import java.awt.Color;

class Particle (mass: Double, val radius: Double,	var position: Vector2D, var velocity: Vector2D,
                var color: Color, val Nr: Int)
        extends Position with Velocity {

  def logger = LoggerFactory.getLogger(getClass);

	require(mass >= 0)
	require(radius >= 0)

  //Konstruktor ohne Farbe
  def this(m: Double, r: Double, pos: Vector2D, vel: Vector2D, Nr: Int) =
    this(m: Double, r: Double, pos: Vector2D, vel: Vector2D, new Color(255, 255, 255), Nr: Int)

  var nextCollisionEvent = new CollisionEvent('N', null, "", 0.0)

  // Aufgrund von numerischen Rundungseffekten braucht man einen Wert, der eine erlaubte
  // Abweichung von einem erwarteten Wert charakterisiert!
  val epsilon = 0.0000000001
  

	//Bewegt das Teilchen auf die Position, die es bei der gegebenen
	//Geschwindigkeit nach einer Flugzeit dt erreichen würde:
	//  r_neu = r_alt + dt * v
	def move(dt: Double): Unit = {
		position = position + velocity * dt 
	}
	
	def doCollisionEvent(){
    if(nextCollisionEvent.collisionType == 'W'){
      logger.debug("Kollision von Teilchen Nr. {} mit Wand: {}" , Nr, nextCollisionEvent.collisionPartnerWall)
      logger.debug("Geschwindigkeit vorher {}", velocity)
      nextCollisionEvent.collisionPartnerWall match {
        case "x0" => accelerate(new Vector2D(-2.0 * this.velocity.x, 0.0))
        case "y0" => accelerate(new Vector2D(0.0, -2.0 * this.velocity.y))
        case "xL" => accelerate(new Vector2D(-2.0 * this.velocity.x, 0.0))
        case "yL" => accelerate(new Vector2D(0.0, -2.0 * this.velocity.y))
        case _ => logger.debug("Falscher Wert in nextCollisionEvent.collisionPartnerWall")
      }
      logger.debug("Geschwindigkeit nachher {}", velocity)
    }
    else if(nextCollisionEvent.collisionType == 'P') {
      //Beim Zusammenstoß zweier Kugeln kann man die Geschwindigkeit jedes Teilchens in zwei Komponenten
      //zerlegen. Eine Komponente entlang der Tangente an beide Kugeln durch den Berührungspunkt der Kugeln.
      //Und eine Komponente senkrecht zu dieser Tangente (Verbindungslinie der beiden Kugelmittelpunkte).
      //Beim Stoß ändert sich die Geschwindigkeitskomponente entlang der Tangente nicht, und die
      //Geschwindigkeitskomponenten entlang der Verbindungslinie der Mittelpunkte werden ausgetauscht.
      //    Geschwindigkeiten unmittelbar vor dem Stoß:
      //        Teilchen 1 V1 = V1_Tangente + V1_Senkrecht
      //        Teilchen 1 V2 = V2_Tangente + V2_Senkrecht
      //    Geschwindigkeiten unmittelbar nach dem Stoß:
      //        Teilchen 1 V1 = V1_Tangente + V2_Senkrecht
      //        Teilchen 1 V2 = V2_Tangente + V1_Senkrecht
      //D.h. die Komponenten senkrecht der Tangente wurden ausgetauscht!      

      logger.debug("Kollision von Teilchen Nr.{} mit Teilchen Nr.{}" , Nr, nextCollisionEvent.collisionPartnerParticle.Nr)
      logger.debug("Geschwindigkeit Nr.{} vorher {}", Nr, velocity)
      logger.debug("Geschwindigkeit Nr.{} vorher {}", nextCollisionEvent.collisionPartnerParticle.Nr, nextCollisionEvent.collisionPartnerParticle.velocity)
      val collPartner = nextCollisionEvent.collisionPartnerParticle
      val dr0 = collPartner.position - this.position

      // Der Abstand der zwei Teilchen muss zum Kollisionszeitpunkt relativ genau der Summe
      // der Teilchenradien entsprechen, sonst ist es eine Kollision mit einem Teilchen,
      // das mal an der Position erwartet wurde, inzwischen aber auf andere Bahnen abgelenkt wurde.
      if ( Math.abs(dr0.abs() - this.radius - collPartner.radius) > epsilon ){
        println("Hier muss eigentlich eine Exception geworfen werden, weil eine imaginäre Teilchenkollision stattfindet")
        println("TeilchenNr." + this.Nr + ", imgainäres Teilchen Nr." + collPartner.Nr)
        println("Abstand der Teilchen: " + dr0.abs(), " Teilchenradien: " + this.radius + "  " + collPartner.radius)
      }

      val eNormal = dr0 / dr0.abs()                               //Normalenvektor, der von r1 nach r2 zeigt

      val dv1Normal   = eNormal * (this.velocity * eNormal)
      val dv1Parallel = this.velocity - dv1Normal
      val dv2Normal   = eNormal * (collPartner.velocity * eNormal)
      val dv2Parallel = collPartner.velocity - dv2Normal

      this.velocity = dv1Parallel + dv2Normal
      collPartner.velocity = dv2Parallel + dv1Normal
      logger.debug("Geschwindigkeit Nr.{} nachher {}", Nr, velocity)
      logger.debug("Geschwindigkeit Nr.{} nachher {}", nextCollisionEvent.collisionPartnerParticle.Nr, nextCollisionEvent.collisionPartnerParticle.velocity)
    }
    else{
        logger.debug("Kollisionsereignis ohne passenden Typparameter: {}",  nextCollisionEvent.collisionType)
    }
	}
	
	//Ändert die Geschwindigkeit des Teilchens um den Vektor dv:
	//   v_neu = v_alt + dv
	def accelerate(dv: Vector2D): Unit = {
		velocity = velocity + dv
	}
	
	//Geschwindigkeit v des Teilchens
	def getVAbs(): Double = velocity.abs()
	
	//Geschwindigkeit des Teilchens zum Quadrat v^2
	def getVAbs2(): Double = velocity.abs2()
	
	//Impuls des Teilchens p = m*v
	def getP(): Vector2D = velocity * mass
	
	//Kinetische Energie des Teilchens Ekin = p^2/2
	def getEk(): Double = getP().abs2() / (2.0 * mass)

	//Report: Informationen über Zustand des Teilchens
	def report() = {
    println("************************************************")
    println("Mass [kg]: {}, Cross Section [m]: {}", mass, radius)
		println("Impuls [kg m/s]: {} Kinetic Energy [J]: {}", getP, getEk())
		println("Position: {}", position)
		println("Velocity: {}", velocity)
    println("************************************************")
	}
}
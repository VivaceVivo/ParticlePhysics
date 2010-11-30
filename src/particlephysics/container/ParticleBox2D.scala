package particlephysics.container

import particlephysics.particle.Particle
import particlephysics.CollisionEvent
import java.awt.Color;
import org.slf4j.LoggerFactory;

class ParticleBox2D(width: Double, height: Double, var particles: List[Particle], t: Double,
  var energieDependentColor: Boolean) {

  def logger = LoggerFactory.getLogger(getClass);

	require(height > 0)
	require(width > 0)
	require (particles != Nil)

  def this(width: Double, height: Double, particles: List[Particle], t: Double) =
    this(width: Double, height: Double, particles: List[Particle], t: Double, false) 

  var realNextCollisionEvent = new CollisionEvent('N', null, "", 0.0)
  //Da die Gesamtenergie (Summe der kinetischen Energie der Teilchen) sich im Verlauf der Zeit nicht
  //ändert (Erhaltungsgröße, konservatives System) und sie daher nicht immer wieder neu berechnet
  //werden braucht, wird sie hier einmalig als Eigenschaft abgelegt.
  //Um dennoch Änderungen dieses Wertes durch numerische Ungenauigkeit feststellen zu können,
  //wird auch noch die Methode "getTotalEnergie" zur Verfügung gestellt.
  val initialTotalEnergie = getTotalEnergie()
  var numberOfCollisions = 0
  var numberOfAdditionalParticleCalcs = 0

  // Aufgrund von numerischen Rundungseffekten braucht man einen Wert, der eine erlaubte
  // Abweichung von einem erwarteten Wert charakterisiert!
  val epsilon = -0.0000000001

	private var time: Double = t

	def getTime = time
	
	def setNextTimeStep(dt: Double) = {
		time += dt
	}

  //List[Particle] sortiert nach Geschwindigkeit
  def listOrderedByVelocity(): List[Particle] = particles.sort( (p1, p2) => p1.velocity.abs() < p2.velocity.abs() )

  //List[Particle] sortiert nach Zeitpunkt des nächsten Kollisions-Ereignis
  def listOrderedByCollisionTime(): List[Particle] =
    particles.sort( (p1, p2) => p1.nextCollisionEvent.collisionTime < p2.nextCollisionEvent.collisionTime )

  // totale kinetische Energie = sum(particle-energies)
  def getTotalEnergie(): Double = {
    var sum: Double = 0.0
    particles.foreach(sum += _.getEk)
    sum
  }

	def nextTimeStep(dt: Double){
		// Wenn es im nächsten Zeitschritt ein Kollisionsereignis gibt, muss dieses Ereignis
		// durchgeführt und anschließend für die beteiligten Teilchen die nächsten Interaktionen
		// (mit Wänden oder anderen Teilchen) bestimmt werden.
		if ((time + dt) >= realNextCollisionEvent.collisionTime){

      val targetTime = time + dt
      // Der Wert für nextSubTimeStep kann auf Grund von numerischen Effekten u.U. (bei beinahe
      // zeitgleich stattfindenden Ereignissen, die besonders gerne durch symmetrische Anfangsbedingungen
      // zustande kommen) auch sehr kleine negative Werte annehmen.
      var nextSubTimeStep: Double = realNextCollisionEvent.collisionTime - time
      var continue = true

      while(continue){
        logger.debug("Nächster Zeitschritt\nZielzeit:        {} \nZeit:            {} \nnextSubTimeStep: {}",
                      Array(targetTime, time, nextSubTimeStep))

        // 1. Konstellation zur nächsten Ereigniszeit herstellen (zeitlich befinden wir uns jetzt noch kurz vor der
        // nächsten Kollision)
        for (p <- this.particles) p.move(nextSubTimeStep)
        setNextTimeStep(nextSubTimeStep)

        // Kollision durchführen (es handelt sich um eine Kollision des ersten Teilchens in der Liste (particles(0)),
        // da diese Liste immer nach der Zeit "nextCollisionEvent.collisionTime" der Teilchen sortiert ist,
        // d.h. das Teichen, welches die nächste Kollision erfährt, steht immer "oben"
        this.particles(0).doCollisionEvent()
        // Zähler für die Anzahl Kollisionen hochzählen
        numberOfCollisions += 1
        //Falls geschwindigkeitsabhängige Farbe der Teilchen eingestellt wurde, wird hier die Farbe der
        //kollidierenden Teilchen neu bestimmt (allerdings nur bei Particle-Particle-Kollision, da nur in diesem
        //Fall ein Energieübertrag zwischen den Teilchen stattfindet (Unendliche Masse der Wand!))
        if(energieDependentColor && (realNextCollisionEvent.collisionType == 'P'))
          velocityDependentColor

        //Für die an der Kollision beteiligten Teilchen muss das nächste Kollisionsereignis neu berechnet werden
        calcNextCollisionEventForParticle(particles(0), particles.drop(1))
        if(realNextCollisionEvent.collisionType == 'P')
          calcNextCollisionEventForParticle(realNextCollisionEvent.collisionPartnerParticle, particles)
        // Es muss aber auch für diejenigen Teilchen die nächste Kollision neu bestimmt werden, die
        // als nächtes mit einem der Teilchen zusammengestoßen wären, die an dem gerade verarbeitetn
        // Stoßprozess beteiligt waren und durch die Änderung ihrer Geschwindigkeit nun nicht mehr
        // ihrem früher prognostizierten Verlauf folgen...
        for(p <- particles.drop(0)
            if(p.nextCollisionEvent.collisionPartnerParticle == particles(0))
            || p.nextCollisionEvent.collisionPartnerParticle == realNextCollisionEvent.collisionPartnerParticle){
          calcNextCollisionEventForParticle(p, particles)
          numberOfAdditionalParticleCalcs += 1
        }

        //Und die particles-Liste muss neu nach der Kollisionszeit sortiert werden...
        particles = listOrderedByCollisionTime()

        if(logger.isDebugEnabled){
          particles.foreach(p => println(p.nextCollisionEvent.collisionTime))
        }

        //Dann erhält man das neue "nächste" CollisionEvent aus dem CollisionEvent des zuoberst stehenden Teilchens:
        realNextCollisionEvent = particles(0).nextCollisionEvent
        logger.debug("Nächste Kollision in: {} s", realNextCollisionEvent.collisionTime)
        if(realNextCollisionEvent.collisionType == 'P')
          logger.debug("Nächste Kollision zwischen Nr. {} und Nr. {}", particles(0).Nr, realNextCollisionEvent.collisionPartnerParticle.Nr)
        else
          logger.debug("Nächste Kollision zwischen Nr. {} und Wand {}", particles(0).Nr, realNextCollisionEvent.collisionPartnerWall)

        if(realNextCollisionEvent.collisionTime <= targetTime)
          nextSubTimeStep = realNextCollisionEvent.collisionTime - time
        else
          continue = false
      }
      //Jetzt noch den restlichen Schritt, um den Zeitsprung dt zu realisieren...
      val lastTimeStep = targetTime - time

      //setNextTimeStep(realNextCollisionEvent.collisionTime - time)
      for (p <- this.particles) p.move(lastTimeStep)

      setNextTimeStep(lastTimeStep)      
		}
		else{
      // Innerhalb des Zeitschritts dt findet kein Kollisionsereignis statt, darum können die Teilchen einfach
      // um dt*dvi entlang ihres Geschwindigkeitsvektors verschoben werden  
			for (p <- this.particles) p.move(dt)
      setNextTimeStep(dt)
		}
	}

	def calcNextCollisionEventForParticle(particle: Particle, pList: List[Particle]) {
    //particle-wall collision
    val nextWallCollEvent = nextPWCollisionEvent(particle)

    val nextParticleCollEvent = nextPPCollisionEvent(particle, pList)

    logger.debug(nextWallCollEvent.reportToString)
    if(nextParticleCollEvent != null) logger.debug(nextParticleCollEvent.reportToString)
    else logger.debug("calcNextCollisionEventForParticle: Kein Teilchen zum Kollidieren")

    if(nextParticleCollEvent == null) particle.nextCollisionEvent = nextWallCollEvent
    else
      if (nextWallCollEvent.collisionTime < nextParticleCollEvent.collisionTime)
        particle.nextCollisionEvent = nextWallCollEvent
      else
        particle.nextCollisionEvent = nextParticleCollEvent
	}
	
//************************************************************************************************
  // Berechnet für jedes Teilchen das nächste Kollisionsereignis (mit Wand oder anderem Teilchen).
	// Genau gesagt wird bestimmt, wann ein Teilchen, sofern es zwischenzeitlich nicht abgelenkt wird,
	// bei geradliniegem Flug mit einer der Wände oder einem anderen Teilchen (bei dem auch nicht
	// berücksichtigt wird, dass es zwischenzeitlich von einem anderen Teilchen oder einer Wand 
	// abgelenkt werden könnte) zusammen stößt.
	// Bei dieser Berechnung erhält man als zuverlässiges Eregebnis nur den Zeitpunkt des von jetzt 
	// an betrachtet nächsten Kollisions-Ereignisses, über alle Teilchen betrachtet. 
	def calcCollisionEventsForAllParticles() {
		//Compute next collision event for each particle (particle-wall or particle-particle collisions)
  	var subsetOfParticles = particles
		for(particle <- particles){

			//particle-wall collision
      val nextWallCollEvent = nextPWCollisionEvent(particle)
			//val nextWallBounceAt = nextBoundaryCollisionEvent(particle)

			//particle-particle collisions
			subsetOfParticles = subsetOfParticles.drop(1)
      //val nextParticleCollisionAt = nextParticleCollisionEvent(particle, subsetOfParticles)
      val nextParticleCollEvent = nextPPCollisionEvent(particle, subsetOfParticles)

      logger.debug(nextWallCollEvent.reportToString)
      if(nextParticleCollEvent != null) logger.debug(nextParticleCollEvent.reportToString)
      else logger.debug("Kein Teilchen für Kollision gefunden oder letztes Teilchen")

      if(nextParticleCollEvent == null) particle.nextCollisionEvent = nextWallCollEvent
      else
        if (nextWallCollEvent.collisionTime < nextParticleCollEvent.collisionTime)
          particle.nextCollisionEvent = nextWallCollEvent
        else
          particle.nextCollisionEvent = nextParticleCollEvent 
		}
	}
	
	// Berechnet, wann das Teilchen "particle" als nächstes mit einem der anderen Teilchen
	// in der Liste "subsetOfParticles" zusammenstößt. Als Ergebnis wird der Zeitpunkt des
	// Zsammentreffens und das Teilchen, mit welchem es zusammenstößt zurück gegeben.
	// Für jedes Partikel-Partikel-Paar wird dabei folgende Rechnung durchgeführt
	// (Die Größen r1, r2, r01, r02, v1, v2 stellen Vektoren dar):
	// Dann gilt für die Bewegungsbahnen (Geraden) der Teilchen 1 und 2 nach der Zeit:
	// r1(t) = r01 + t*v1; r2(t) = r02 + t*v2
	// Dann erhält man den Abstand d der Teilchen voneinander zur Zeit t aus der Beziehung:
	// d^2 = (r2(t) - r1(t))^2 = dr0^2 + t^2 * dv^2 + 2 * t * dr0 * dv,
	// wobei dr0 = r02 - r01 und dv = v2 - v1 sind.
	// Es handelt sich also um eine quadratische Gleichung in der Zeit, die 2, 1 oder keine Lösung
	// haben kann.
	def nextPPCollisionEvent(particle: Particle, subsetOfParticles: List[Particle]): CollisionEvent = {
		var nextParticleCollisionEvent: CollisionEvent = null
    var nextCollisionIn = Double.MaxValue
		val d2 = 4 * particle.radius * particle.radius

		for(p <- subsetOfParticles){
			val dr0  = p.position - particle.position
			val dr02 = dr0 * dr0
			val dv   = p.velocity - particle.velocity
			val dv2  = dv * dv
			val q    = (dr0 * dv) / dv2
			val r    = (dr02 - d2) / dv2

      var eventTime = Double.MaxValue
      val temp = q*q - r
			if( temp >= 0.0 ) {
        // Hier interessiert nur die kleinere der möglichen zwei Lösungen der quadratischen Gleichung,
        // da die zweite Lösung den Fall beschreibt, dass ein Teilchen durch ein anderes Teilchen "hindurch geht"
        // und auf der anderen Seite den Bereich des anderen Teilchens wieder verlässt.
        // Eine Situation, die im hier betrachteten Fall massiver klassischer Teilchen nicht auftreten kann.
				eventTime = -1.0 * q - Math.sqrt(temp) + time
        // Hier wird statt 0 ein sehr kleiner negativer Wert verwendet, weil durch numerische Effekte
        // bei (fast) gleichzeitig stattfindenden Kollisionen (kann z.B. bei sehr symmetrischen
        // Anfangskonfigurationen auftreten) eine Kollision zeitlich in den negativen
        // Bereich rutschen kann. Um diese Fälle noch zu berücksichtigen wird statt mit 0 mit diesem
        // kleinen negativen Betrag gearbeitet.
        if( (eventTime - time >= epsilon) && (eventTime < nextCollisionIn) ) {
          nextCollisionIn = eventTime
          nextParticleCollisionEvent = new CollisionEvent('P', p, "", nextCollisionIn)
        }
        if( (eventTime - time < epsilon)) {
          logger.debug("nextPPCollisionEvent: Zeit < 0: {}", eventTime - time)
        }
			}
		}
		return nextParticleCollisionEvent
	}

	// Berechnet für ein Teilchen in der Box, wann es als nächstes gegen eine Wand stößt
	// (ohne Berücksichtigung der anderen Teilchen in der Box), und um welche der vier
	// Wände es sich handelt.
	// Dabei wird anhand der Geradengleichung des Teilchens: 
	//              r(t) = r0 + t*v
	// bestimmt, wann es gegen eine der Wände (x=0, y=0, x=Lx, y=Ly) stößt.
	// Ist die für eine Wand ermittelte Kollisionszeit negativ, wird sie ignoriert
	// (d.h. der Zeitwert für dieses Ereignis wird auf Double.MaxValue gesetzt),
	// da das Ereignis in der Vergangenheit liegt und für unsere Situation daher
	// irrelevant ist.
	// Von allen 4 Ereignissen wird das als erstes stattfindende ermittelt
	// und als Ergebnis zurück geliefert.
	def nextPWCollisionEvent(particle: Particle): CollisionEvent = {
    val x = particle.position.x
    val y = particle.position.y
    val vx = particle.velocity.x
    val vy = particle.velocity.y
    val r = particle.radius

    var nextBounceIn = Double.MaxValue
    var nextWallCollisionEvent: CollisionEvent = null
    def wallCollision(contactDistance: Double, p: Double, v: Double, wallType: String) = {
      val temp = (contactDistance - p)/v + time
      // Hier wird statt 0 ein sehr kleiner negativer Wert verwendet, weil durch numerische Effekte
      // bei (fast) gleichzeitig stattfindenden Kollisionen (kann z.B. bei sehr symmetrischen
      // Anfangskonfigurationen auftreten) eine Kollision zeitlich in den negativen
      // Bereich rutschen kann. Um diese Fälle noch zu berücksichtigen wird statt mit 0 mit diesem
      // kleinen negativen Betrag gearbeitet.
      if ( ( temp - time > epsilon) && (temp < nextBounceIn) ){
        nextBounceIn = temp
        nextWallCollisionEvent = new CollisionEvent('W', null, wallType, nextBounceIn)
      }
    }

		//Particle hits right wall
    //(nur wenn vx > 0 ist kann eine Kollision mit dieser Wand auftreten)
    if(vx > 0.0) wallCollision(width - r, x, vx, "xL")

		//Particle hits top wall
    //(nur wenn vy > 0 ist kann eine Kollision mit dieser Wand auftreten)
    if(vy > 0.0) wallCollision(height - r, y, vy, "yL")

		//Particle hits left wall
    //(nur wenn vx < 0 ist kann eine Kollision mit dieser Wand auftreten)
    if(vx < 0.0) wallCollision(r, x, vx, "x0")

		//Particle hits bottom  wall
    //(nur wenn vy < 0 ist kann eine Kollision mit dieser Wand auftreten)
    if(vy < 0.0) wallCollision(r, y, vy, "y0")

		return nextWallCollisionEvent
	}
//************************************************************************************************
		

  def velocityDependentColor(){
    //Erstes stoßende Teilchen:
    val eParticle1 = this.particles(0).getEk / initialTotalEnergie
    var col = Math.min(255 * eParticle1 * 30, 255.0)
    logger.debug("e: " + col.toInt)
    this.particles(0).color = col match{
        case 255 => new Color(this.particles(0).color.getRed, 240, 240)
        case _ => new Color(this.particles(0).color.getRed, col.toInt, col.toInt)
    }
    //Zweites stoßendes Teilchen:
    val eParticle2 = realNextCollisionEvent.collisionPartnerParticle.getEk / initialTotalEnergie
    col = Math.min(255 * eParticle2 * 30, 255.0)
    logger.debug("e: " + col.toInt)
    realNextCollisionEvent.collisionPartnerParticle.color =
            col match{
        case 255 =>  new Color(realNextCollisionEvent.collisionPartnerParticle.color.getRed, 240, 240)
        case _ => new Color(realNextCollisionEvent.collisionPartnerParticle.color.getRed, col.toInt, col.toInt)
    }
  }

	//Infos über Box
	def boxReport(){
		logger.debug("******************************************************")
		logger.debug("Höhe der Box [m]:    {}",height)
		logger.debug("Breite der Box [m]:  {}",width)
		logger.debug("Anzahl Teilchen:     {}",particles.length)
		logger.debug("Startzeit [s]:       {}",time)
		logger.debug("Gesamtenergie [J]:   {}",getTotalEnergie())
		logger.debug("******************************************************")
	}
	
	//Infos über Teilchen in Box...
	def particleReport(): String = {
    var str = new String("Zeit: " + time)
		for (particle <- particles){
			str = str + "\nNr: " + particle.Nr + "\tOrt: " + particle.position + "\tGeschw.: " + particle.velocity
		}
    str
	}
}
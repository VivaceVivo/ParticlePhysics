package particlephysics

import particlephysics.particle.Particle
import particlephysics.container.ParticleBox2D

object MainParticle {

  def main(args: Array[String]): Unit = {  
  	
    val pBox = initBox2()
  	
  	for (particle <- pBox.particles) {
  		println("Pos: " + particle.position)
  		println("Vel: " + particle.velocity)
  		print("Speed [m/s]: " + particle.velocity.abs())
  		println("\tEnergie [J]: " + particle.getEk()+"\n"                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          )
  	}
  	
    pBox.boxReport();
    pBox.calcCollisionEventsForAllParticles()
    pBox.particles.foreach(p => println("Next collision [s]: " + p.nextCollisionEvent.collisionTime) )
   
  }
  
  
  def initBox2(): ParticleBox2D = {
  	
  	val n = 2
	  val particleMass = 0.1
  	val particleRadius: Double = 0.1
  	val boxWidth: Double = 1.0
  	val boxHeigth: Double = 1.0
  	
    var particleList : List[Particle] = Nil
		var x: Double = 0.2
		var y: Double = 0.2
		var newVec = new Vector2D(x, y)
 		particleList = (new Particle(particleMass, particleRadius, newVec, newVec * 0.1, 1)) :: particleList
 		x = 0.8
 		y = 0.8
 		newVec = new Vector2D(x, y)
 		particleList = (new Particle(particleMass, particleRadius, newVec, newVec * -0.1, 2)) :: particleList
  	
    new ParticleBox2D(boxWidth, boxHeigth, particleList, 0)
  }

  def initBox(): ParticleBox2D = {
  		
  		val n = 2
  		val particleMass = 0.1
  		val particleRadius: Double = 0.1
  		val boxWidth: Double = 1.0
  		val boxHeigth: Double = 1.0
  		
  		val particleArray: Array[Particle] = new Array[Particle](n)
  		var particleList : List[Particle] = Nil
  		for(i <- 0 until n){
  			val x: Double = particleRadius + Math.random * (boxWidth - 2*particleRadius)
  			val y: Double = particleRadius + Math.random * (boxHeigth - 2*particleRadius)
  			val newVec = new Vector2D(x, y)
  			particleList = (new Particle(particleMass, particleRadius, newVec, newVec * 0.1, i+1)) :: particleList
  		}
  		
  		new ParticleBox2D(boxWidth, boxHeigth, particleList, 0)
  }

}
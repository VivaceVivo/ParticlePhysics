package particlephysics

class Vector2D(val x: Double, val y: Double){
	
	def multiply(c: Double): Vector2D =	new Vector2D(c * this.x, c * this.y)
	
	//Betrag des Vektors
	def abs(): Double = Math.sqrt( this * this) //		Math.sqrt( Math.pow(this.x, 2.0) + Math.pow(this.y, 2.0) )

	//Betragsquadrat des Vektors (abs^2)
	def abs2(): Double = this * this
	
	def + (that: Vector2D): Vector2D = new Vector2D(this.x + that.x, this.y + that.y)
	def - (that: Vector2D): Vector2D = new Vector2D(this.x - that.x, this.y - that.y)
	
	def * (that: Vector2D): Double = this.x * that.x + this.y * that.y
  def * (c: Double): Vector2D = new Vector2D(c * this.x, c * this.y)

  def / (c: Double): Vector2D = new Vector2D(this.x / c, this.y / c)

  override def toString = "(" + x + ", " + y + ")"
}
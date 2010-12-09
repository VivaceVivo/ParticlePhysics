package particlephysics

import particlephysics.particle.Particle

// Diese Klasse beschreeibt das Ereignis einer Teilchen-Kollision, d.h. die Kollision eines Teilchens
// mit einem anderen Teilchen oder mit einer Wand des Behälters, in dem sich das Teilchen befindet.
//
// Dabei sind:
//
// collisionType: Art des nächsten Kollisions-Ereignisses:
//          P: Teilchen-Teilchen-Kollision
//          W: Teilchen-Wand-Kollision
//          N: Unbestimmt
//
// collisionPartnerParticle: Das Teilchen, mit dem das bettrachtete Teilchen kollidiert (falls T-T Kollision)
//
// collisionPartnerWall: Die Behälterwand, mit dem das Teilchen kollidiert (T-W Kollision)
//                x0: linke Wand
//                xL: obere Wand
//                y0: rechte Wand
//                yL: untere Wand
//
// collisionTime: Die Zeit [s], zu der die Kollision stattfinden wird

class CollisionEvent(val collisionType: Char, val collisionPartnerParticle: Particle,
											val collisionPartnerWall: String, val collisionTime: Double) {

  def reportToString() = {
    new String("CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC"
    + "\nCollisionEvent"
    + "\nTYP:      " + collisionType
    + "\nTeilchen: " + collisionPartnerParticle
    + "\nWall:     " + collisionPartnerWall
    + "\nZeit:     " + collisionTime
    + "\nCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC")
  }
}
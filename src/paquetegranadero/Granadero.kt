package paquetegranadero

import robocode.*

class Granadero: JuniorRobot() {

    private val strategy = StrategySafe()

    override fun run(){
        strategy.run(this)
    }

    override fun onHitWall() {
        strategy.run(this)
    }

    override fun onHitByBullet() {
        strategy.onHitByBullet(this)
    }

    override fun onHitRobot() {
        strategy.onHitRobot(this)
    }

    override fun onScannedRobot() {
        strategy.onScannedRobot(this)
    }

}
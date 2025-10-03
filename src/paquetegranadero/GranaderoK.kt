package paquetegranadero

import robocode.*

class GranaderoK: JuniorRobot() {

    private val strategy = StrategyPredictiveCorners()

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
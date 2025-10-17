package paquetegranadero

import robocode.JuniorRobot

class Granadero : JuniorRobot() {
    private val strategist: RobotStrategist = StrategistSanMartin.getInstance()
    private var strategy: RobotStrategy? = null

    private fun updateStrategy() {
        strategy = strategist.decide(this, this.strategy)
    }

    override fun run() {
        updateStrategy()
        strategy!!.run(this)
    }

    /**
     * onScannedRobot: What to do when you see another robot
     */
    override fun onScannedRobot() {
        updateStrategy()
        strategy!!.onScannedRobot(this)
    }

    /**
     * onHitByBullet: What to do when you're hit by a bullet
     */
    override fun onHitByBullet() {
        updateStrategy()
        strategy!!.onHitByBullet(this)
    }

    /**
     * onHitWall: What to do when you hit a wall
     */
    override fun onHitWall() {
        updateStrategy()
        strategy!!.onHitWall(this)
    }

    override fun onHitRobot() {
        updateStrategy()
        strategy!!.onHitRobot(this)
    }
}
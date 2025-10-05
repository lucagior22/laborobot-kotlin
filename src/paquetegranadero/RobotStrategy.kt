package paquetegranadero

import robocode.JuniorRobot

interface RobotStrategy {

    fun run(robot : JuniorRobot)

    fun onScannedRobot(robot : JuniorRobot)

    fun onHitWall(robot : JuniorRobot)

    fun onHitByBullet(robot : JuniorRobot)

    fun onHitRobot(robot : JuniorRobot)

}
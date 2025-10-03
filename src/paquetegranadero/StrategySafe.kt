package paquetegranadero

import robocode.JuniorRobot

class StrategySafe : RobotStrategy {
    private var moveDir  = 1
    private var sweep  = 0
    private val MARGIN  = 120

    override fun run(robot: JuniorRobot) {
        if (robot.robotX < MARGIN || robot.robotX > robot.fieldWidth - MARGIN || robot.robotY < MARGIN || robot.robotY > robot.fieldHeight - MARGIN) {
           robot.turnRight(90)
           moveDir = -moveDir
        }
        robot.ahead(80 * moveDir)
        sweep = (sweep + 45) % 360
        robot.turnTo(sweep)
    }

    override fun onScannedRobot(robot: JuniorRobot) {
        robot.turnTo(robot.scannedAngle)
        robot.fire(if(robot.scannedDistance < 100) 3.0 else 1.0)
    }

    override fun onHitByBullet(robot: JuniorRobot) {
        robot.turnTo(robot.hitByBulletAngle + 45)
        robot.ahead(50)
    }

    override fun onHitWall(robot: JuniorRobot) {
        robot.turnTo(robot.hitWallAngle + 180)
        robot.ahead(robot.fieldWidth / 3)
    }

    override fun onHitRobot(robot: JuniorRobot) {
        robot.turnBackLeft(100, 45)
    }
}
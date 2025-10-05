package paquetegranadero

import robocode.JuniorRobot
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class StrategyPredictiveCorners : RobotStrategy{
    var cycles = 0


    override fun run(robot: JuniorRobot){
        robot.setColors(JuniorRobot.blue, JuniorRobot.white, JuniorRobot.white, JuniorRobot.yellow, JuniorRobot.white)
    if(inCorner(robot)){
        robot.ahead(30)
    }
    if(cycles >= 10){
        robot.turnGunRight(360 - (robot.others * 5))
    }
    cycles++
    }

    fun inCorner(robot: JuniorRobot):Boolean{
        var margin:Double = 100.0

        var esquinaAbajoIzq:Boolean = (robot.robotX <= margin && robot.robotY <= margin)
        var esquinaAbajoDer:Boolean = (robot.robotX >= robot.fieldWidth - margin && robot.robotY <= margin)
        var esquinaArribaIzq:Boolean = (robot.robotX <= margin && robot.robotY >= robot.fieldHeight - margin)
        var esquinaArribaDer:Boolean = (robot.robotX >= robot.fieldWidth - margin && robot.robotY >= robot.fieldHeight - margin)

        return esquinaArribaDer || esquinaArribaIzq || esquinaAbajoIzq || esquinaAbajoDer
    }

    fun calculateFirePower(robot: JuniorRobot): Double{
        var auxFirePower:Double = 1.0;
        if(robot.energy > 80){
            auxFirePower+=1
        } else if(robot.energy > 50){
            auxFirePower += 0.5
        }
        else {
            auxFirePower -= 0.7
        }
        if (robot.scannedVelocity > 5) {
            auxFirePower -= 1.5
        } else if (robot.scannedVelocity == 0) {
            auxFirePower += 1.0
        }

        if (robot.scannedDistance > 400) {
            auxFirePower -= 1.5
        } else if (robot.scannedDistance > 200) {
            auxFirePower -= 0.5
        } else if (robot.scannedDistance < 20 && abs(robot.scannedBearing) < 5) {
            auxFirePower += 2.0
        }

        auxFirePower += robot.others * 0.3

        return min(3.0, max(0.1, auxFirePower))
    }

    override fun onScannedRobot(robot: JuniorRobot){
        if (this.cycles >= 3) {
            this.cycles = 0
            robot.turnGunTo(robot.scannedAngle)
            robot.fire(this.calculateFirePower(robot))
        }
    }

    override fun onHitByBullet(robot: JuniorRobot){
        if (this.inCorner(robot)) {
            robot.ahead(100)
        }
        if (abs(robot.hitByBulletBearing) < 90) {
            robot.turnGunTo(robot.hitByBulletAngle)
        }
    }

    override fun onHitWall(robot: JuniorRobot){
        val turnToAngle: Int

        if (robot.hitWallBearing > 0) {
            turnToAngle = (robot.hitWallAngle + 90) % 360
        } else {
            turnToAngle = (robot.hitWallAngle - 90) % 360
        }

        robot.turnTo(turnToAngle)
        robot.turnGunTo((robot.hitWallAngle + 180) % 360)

        robot.ahead(100)
    }

    override fun onHitRobot(robot: JuniorRobot){
        robot.turnGunTo(robot.hitRobotAngle)
        robot.fire(3.0)
    }

}
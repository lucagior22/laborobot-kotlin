package paquetegranadero

import robocode.JuniorRobot
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

class StrategistRoca private constructor() : RobotStrategist {
    companion object {
        private var instance: StrategistRoca? = null

        fun getInstance(): StrategistRoca {
            return instance ?: StrategistRoca().also { instance = it} // Aprovechamos el operador Elvis de Kotlin
        }
    }
    override fun decide(robot: JuniorRobot, currentStrategy: RobotStrategy?) : RobotStrategy {
        if (robot.others >= 3) {
            if (currentStrategy is StrategyWallHugger) {
                return currentStrategy
            }
            return StrategyWallHugger()
        }
        if (currentStrategy is StrategyCentralControl) {
            return currentStrategy
        }
        return StrategyCentralControl()
    }

    private inner class StrategyWallHugger : RobotStrategy {
        private var clockwise = true

        override fun run(robot: JuniorRobot?) {
            robot?.setColors(JuniorRobot.red, JuniorRobot.black, JuniorRobot.black, JuniorRobot.orange, JuniorRobot.black)

            if (nearWall(robot)) {
                if (clockwise) {
                    robot?.turnRight(90)
                } else {
                    robot?.turnLeft(90)
                }
            }

            robot?.ahead(40)

            robot?.turnGunRight(10)
        }

        override fun onScannedRobot(robot: JuniorRobot?) {
            val firepower = calculateWallHuggerFirePower(robot!!)

            robot.turnGunTo(robot.scannedAngle)
            robot.fire(firepower)

            if (robot.scannedDistance < 80) {
                clockwise = !clockwise
            }
        }

        override fun onHitWall(robot: JuniorRobot?) {
            if (clockwise) {
                robot?.turnRight(90)
            } else {
                robot?.turnLeft(90)
            }
            robot?.ahead(30)
        }

        override fun onHitByBullet(robot: JuniorRobot?) {
            clockwise = !clockwise
            robot?.turnGunTo(robot.hitByBulletAngle)
            robot?.fire(2.0)
        }

        override fun onHitRobot(robot: JuniorRobot?) {
            robot?.turnGunTo(robot.hitRobotAngle)
            robot?.fire(3.0)
            robot?.back(20)
        }

        private fun nearWall(robot : JuniorRobot?) : Boolean {
            val margin = 80
            return robot!!.robotX < margin || robot.robotY < margin || robot.robotX > robot.fieldWidth - margin || robot.robotY > robot.fieldHeight - margin
        }

        private fun calculateWallHuggerFirePower(robot : JuniorRobot) : Double {
            var firepower = 1.5

            if (robot.scannedDistance < 100) firepower += 1.0

            if (robot.scannedVelocity > 4) firepower -= 0.5

            if (robot.energy > 60) firepower += 0.5

            return 3.0.coerceAtMost(0.1.coerceAtLeast(firepower)) // Es equivalente a la solución en Java, el IDEA me sugería el cambio
        }
    }

    private inner class StrategyCentralControl : RobotStrategy {
        private var scanDirection = 1
        private var inCenter = false

        override fun run(robot: JuniorRobot?) {
            robot?.setColors(JuniorRobot.green, JuniorRobot.white, JuniorRobot.white, JuniorRobot.red, JuniorRobot.white)

            if (!inCenter) {
                val centerX = (robot!!.fieldWidth / 2).toDouble()
                val centerY = (robot.fieldHeight / 2).toDouble()

                val angleToCenter = Math.toDegrees(
                    atan2(
                        centerX - robot.robotX,
                        centerY - robot.robotY
                    )
                )

                robot.turnTo(angleToCenter.toInt())

                val distanceToCenter = hypot(
                    centerX - robot.robotX,
                    centerY - robot.robotY
                )

                if (distanceToCenter > 50) {
                    robot.ahead(30)
                } else {
                    inCenter = true
                }
            } else {
                robot?.turnRight(45)
                robot?.ahead(20)
            }

            robot?.turnGunRight(30 * scanDirection)
        }

        override fun onScannedRobot(robot: JuniorRobot?) {
            val firepower = calculateCentralFirePower(robot!!)

            robot.turnGunTo(robot.scannedAngle)
            robot.fire(firepower)

            if (Math.random() < 0.3) {
                scanDirection *= -1
            }
        }

        override fun onHitByBullet(robot: JuniorRobot?) {
            // Moverse un poco cuando nos golpeen
            robot?.turnRight(90)
            robot?.ahead(30)

            // Disparar de vuelta
            robot?.turnGunTo(robot.hitByBulletAngle)
            robot?.fire(2.0)
        }

        override fun onHitWall(robot: JuniorRobot?) {
            // Esto no debería pasar mucho si estamos en el centro
            inCenter = false
            robot?.turnRight(180)
            robot?.ahead(50)
        }

        override fun onHitRobot(robot: JuniorRobot?) {
            robot?.turnGunTo(robot.hitRobotAngle)
            robot?.fire(3.0)
            robot?.back(30)
        }

        private fun calculateCentralFirePower(robot: JuniorRobot): Double {
            var firepower = 2.0 // Base más alta desde posición central

            // Ajustar por distancia (más potencia a distancia media)
            if (robot.scannedDistance in 201..<400) {
                firepower += 0.5
            }

            // Menos potencia si el enemigo se mueve rápido
            if (robot.scannedVelocity > 5) firepower -= 1.0
            else if (robot.scannedVelocity == 0) firepower += 0.5

            // Ajustar por energía propia
            if (robot.energy < 30) firepower -= 0.5

            return min(3.0, max(0.1, firepower))
        }
    }
}
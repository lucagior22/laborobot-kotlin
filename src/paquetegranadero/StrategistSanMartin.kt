package paquetegranadero

import robocode.JuniorRobot
import kotlin.math.*

class StrategistSanMartin private constructor() : RobotStrategist {

    companion object {
        @Volatile
        private var instance: StrategistSanMartin? = null

        fun getInstance(): StrategistSanMartin {
            return instance ?: synchronized(this) {
                instance ?: StrategistSanMartin().also { instance = it }
            }
        }
    }


    override fun decide(robot: JuniorRobot, currentStrategy: RobotStrategy?): RobotStrategy {
        if (robot.others <= 10 && robot.energy < 50) {
            if (currentStrategy is StrategyAggro) {
                return currentStrategy
            }
            return StrategyAggro()
        }

        if (currentStrategy is StrategyPredictiveCorners) {
            return currentStrategy
        }
        return StrategyPredictiveCorners()
    }

    // Clases anidadas
    private inner class StrategyAggro : RobotStrategy {
        private var lastScannedBearing = -1

        override fun run(robot: JuniorRobot?) {
            robot?.setColors(
                JuniorRobot.blue,
                JuniorRobot.white,
                JuniorRobot.white,
                JuniorRobot.yellow,
                JuniorRobot.white
            )

            if (robot?.heading != robot?.gunHeading) {
                robot!!.turnGunTo(robot.heading)
            }

            if (lastScannedBearing < 0) {
                robot!!.turnRight(180)
            } else {
                robot!!.turnLeft(180)
            }
        }

        override fun onScannedRobot(robot: JuniorRobot?) {
            // Obtener información del enemigo
            val enemyDistance = robot?.scannedDistance
            val enemyBearing = robot?.scannedBearing
            val enemyHeading = robot?.scannedHeading
            val enemyVelocity = robot?.scannedVelocity

            if (enemyBearing != null) {
                lastScannedBearing = enemyBearing
            }

            // Calcular la posición absoluta del enemigo
            val enemyX =
                robot!!.robotX + sin(Math.toRadians((robot.heading + enemyBearing!!).toDouble())) * enemyDistance!!
            val enemyY = robot.robotY + cos(Math.toRadians((robot.heading + enemyBearing).toDouble())) * enemyDistance

            // Hacer la predicción del movimiento
            val targetPosition = predictEnemyPosition(
                enemyX,
                enemyY,
                enemyHeading!!.toDouble(),
                enemyVelocity!!.toDouble(),
                enemyDistance.toDouble(),
                robot
            )

            // Calcular el ángulo hacia la posición predicha
            val targetAngle = Math.toDegrees(
                atan2(
                    targetPosition[0] - robot.robotX,
                    targetPosition[1] - robot.robotY
                )
            )

            var turnAngle = targetAngle - robot.gunHeading

            // Normalizar el ángulo (-180 a 180)
            while (turnAngle > 180) turnAngle -= 360.0
            while (turnAngle < -180) turnAngle += 360.0

            robot.turnRight(turnAngle.toInt())

            if (enemyDistance < 100 && enemyVelocity < 3) {
                val firePower = computeFirePower(
                    enemyDistance,
                    enemyVelocity,
                    robot.fieldWidth.toDouble(),
                    robot.fieldHeight.toDouble()
                )
                robot.fire(firePower)
            }
            robot.ahead(min(enemyDistance, robot.fieldWidth / 5))
        }

        fun predictEnemyPosition(
            enemyX: Double, enemyY: Double, enemyHeading: Double,
            enemyVelocity: Double, distance: Double, robot: JuniorRobot
        ): DoubleArray {
            val moveVelocity = (20 - (2 * 3)).toDouble()

            // Tiempo que tardará la bala en llegar
            val timeToTarget = distance / moveVelocity

            // Calcular la posición futura del enemigo
            var futureX = enemyX + sin(Math.toRadians(enemyHeading)) * enemyVelocity * timeToTarget
            var futureY = enemyY + cos(Math.toRadians(enemyHeading)) * enemyVelocity * timeToTarget

            // Verificar límites del campo de batalla
            futureX = max(18.0, min((robot.fieldWidth - 18).toDouble(), futureX))
            futureY = max(18.0, min((robot.fieldHeight - 18).toDouble(), futureY))

            return doubleArrayOf(futureX, futureY)
        }

        override fun onHitByBullet(robot: JuniorRobot?) {
            if (abs(robot!!.hitByBulletBearing) < 5) {
                robot.fire(3.0)
            } else {
                robot.ahead(30)
            }
        }

        override fun onHitWall(robot: JuniorRobot?) {
            robot?.turnTo((robot.hitWallAngle + 180) % 360)
            robot?.ahead(robot.fieldHeight / 3)
        }

        override fun onHitRobot(robot: JuniorRobot?) {
            robot?.turnTo(robot.hitRobotAngle)
            robot?.fire(3.0)
            robot?.ahead(10)
        }


        private fun computeFirePower(
            scannedDistance: Int,
            scannedVelocity: Int,
            fieldWidth: Double,
            fieldHeight: Double
        ): Double {
            val MIN_POWER = 0.1
            val MAX_POWER = 3.0

            val maxDist = hypot(fieldWidth, fieldHeight)
            val norm = min(max(scannedDistance.toDouble(), 0.0) / max(maxDist, 1.0), 1.0)

            val power = MAX_POWER - (MAX_POWER - MIN_POWER) * norm
            var calculatedPower = max(MIN_POWER, min(MAX_POWER, power))

            if (scannedDistance < 50 && scannedVelocity == 0) {
                calculatedPower = 3.0
            }
            return calculatedPower
        }
    }


    private class StrategyPredictiveCorners : RobotStrategy {
        private var cycles = 0

        override fun run(robot: JuniorRobot?) {
            robot?.setColors(
                JuniorRobot.blue,
                JuniorRobot.white,
                JuniorRobot.white,
                JuniorRobot.yellow,
                JuniorRobot.white
            )
            if (!this.inCorner(robot)) {
                robot?.ahead(50)
            }
            if (cycles >= 10) {
                robot?.turnGunRight(360 - (robot.others * 5))
            }

            cycles++
        }

        private fun inCorner(robot: JuniorRobot?): Boolean {
            val margin = 100.0

            val esquinaAbajoIzq = (robot!!.robotX <= margin && robot.robotY <= margin)
            val esquinaAbajoDer = (robot.robotX >= robot.fieldWidth - margin && robot.robotY <= margin)
            val esquinaArribaIzq = (robot.robotX <= margin && robot.robotY >= robot.fieldHeight - margin)
            val esquinaArribaDer =
                (robot.robotX >= robot.fieldWidth - margin && robot.robotY >= robot.fieldHeight - margin)

            return esquinaAbajoIzq || esquinaAbajoDer || esquinaArribaIzq || esquinaArribaDer
        }

        private fun calculateFirePower(robot: JuniorRobot): Double {
            var auxFirePower = 1.0

            if (robot.energy > 80) {
                auxFirePower += 1.0
            } else if (robot.energy > 50) {
                auxFirePower += 0.5
            } else {
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

        override fun onScannedRobot(robot: JuniorRobot?) {
            // Obtener información del enemigo
            val enemyDistance = robot?.scannedDistance?.toDouble()
            val enemyBearing = robot?.scannedBearing?.toDouble()
            val enemyHeading = robot?.scannedHeading?.toDouble()
            val enemyVelocity = robot?.scannedVelocity?.toDouble()

            // Calcular la posición absoluta del enemigo
            val enemyX = robot!!.robotX + sin(Math.toRadians(robot.heading + enemyBearing!!)) * enemyDistance!!
            val enemyY = robot.robotY + cos(Math.toRadians(robot.heading + enemyBearing)) * enemyDistance

            // Hacer la predicción del movimiento
            val targetPosition =
                predictEnemyPosition(enemyX, enemyY, enemyHeading!!, enemyVelocity!!, enemyDistance, robot)

            // Calcular el ángulo hacia la posición predicha
            val targetAngle = Math.toDegrees(
                atan2(
                    targetPosition[0] - robot.robotX,
                    targetPosition[1] - robot.robotY
                )
            )

            // Girar el cañón hacia el objetivo predicho
            var gunTurnAngle = targetAngle - robot.gunHeading

            // Normalizar el ángulo (-180 a 180)
            while (gunTurnAngle > 180) gunTurnAngle -= 360.0
            while (gunTurnAngle < -180) gunTurnAngle += 360.0

            // Girar y disparar
            robot.turnGunRight(gunTurnAngle.toInt())
            robot.fire(this.calculateFirePower(robot)) // Potencia de disparo (1-3 para JuniorRobot)
        }

        /**
         * Predice la posición futura del enemigo basándose en su movimiento lineal
         */
        private fun predictEnemyPosition(
            enemyX: Double,
            enemyY: Double,
            enemyHeading: Double,
            enemyVelocity: Double,
            distance: Double,
            robot: JuniorRobot
        ): DoubleArray {
            // Velocidad de la bala (depende de la potencia de disparo)

            val bulletVelocity = (20 - (2 * 3)).toDouble() // Para potencia 2: 20 - 6 = 14

            // Tiempo que tardará la bala en llegar
            val timeToTarget = distance / bulletVelocity

            // Calcular la posición futura del enemigo
            var futureX = enemyX + sin(Math.toRadians(enemyHeading)) * enemyVelocity * timeToTarget
            var futureY = enemyY + cos(Math.toRadians(enemyHeading)) * enemyVelocity * timeToTarget

            // Verificar límites del campo de batalla
            futureX = max(18.0, min((robot.fieldWidth - 18).toDouble(), futureX))
            futureY = max(18.0, min((robot.fieldHeight - 18).toDouble(), futureY))

            return doubleArrayOf(futureX, futureY)
        }

        override fun onHitByBullet(robot: JuniorRobot?) {
            if (this.inCorner(robot)) {
                robot?.ahead(100)
            }
            if (abs(robot!!.hitByBulletBearing) < 90) {
                robot.turnGunTo(robot.hitByBulletAngle)
            }
        }

        override fun onHitWall(robot: JuniorRobot?) {
            val turnToAngle: Int

            if (robot!!.hitWallBearing > 0) {
                turnToAngle = (robot.hitWallAngle + 90) % 360
            } else {
                turnToAngle = (robot.hitWallAngle - 90) % 360
            }

            robot.turnTo(turnToAngle)
            robot.turnGunTo((robot.hitWallAngle + 180) % 360)

            robot.ahead(100)
        }


        override fun onHitRobot(robot: JuniorRobot?) {
            robot?.turnGunTo(robot.hitRobotAngle)
            robot?.fire(3.0)
        }
    }

}
package confetti

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.PI

class ConfettiPhysicsTest {
    @Test
    fun computeKeyframes_returns41Frames() {
        val output = ConfettiPhysics.computeKeyframes(
            ConfettiPhysics.Input(
                angle = -PI / 2,
                startVelocity = 25.0,
                decay = 0.91,
                gravity = 1.0,
                drift = 0.0,
                wobbleSpeed = 0.08,
                wobbleOffset = 1.0,
                size = 1.0,
                ticks = 150,
                xTiltRotations = 0.0,
                tiltRotations = 3.0,
                zTiltRotations = 0.0,
                rotation = 45.0,
                fadeOutEnd = 0.8,
            ),
        )

        assertEquals(41, output.tx.size)
        assertEquals(41, output.ty.size)
        assertEquals(41, output.scale.size)
        assertEquals(41, output.opacity.size)
    }

    @Test
    fun computeKeyframes_zeroVelocity_holdsAtOrigin() {
        val output = ConfettiPhysics.computeKeyframes(
            ConfettiPhysics.Input(
                angle = -PI / 2,
                startVelocity = 0.0,
                decay = 1.0,
                gravity = 0.0,
                drift = 0.0,
                wobbleSpeed = 0.0,
                wobbleOffset = 0.0,
                size = 0.0,
                ticks = 10,
                xTiltRotations = 0.0,
                tiltRotations = 0.0,
                zTiltRotations = 0.0,
                rotation = 0.0,
                fadeOutEnd = 1.0,
            ),
        )

        assertEquals(0.0, output.tx.last(), 0.0001)
        assertEquals(0.0, output.ty.last(), 0.0001)
        assertEquals(1.0, output.scale.last(), 0.0001)
    }
}

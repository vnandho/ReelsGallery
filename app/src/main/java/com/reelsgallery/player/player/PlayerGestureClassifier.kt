package com.reelsgallery.player.player

/** Pure geometry used by the player gesture layer and its unit tests. */
enum class GestureZone { LEFT, CENTER, RIGHT }
enum class SwipeAction { NONE, NEXT, PREVIOUS }

object PlayerGestureClassifier {
    fun zoneOf(x: Float, width: Float, sideFraction: Float = 0.28f): GestureZone {
        if (width <= 0f) return GestureZone.CENTER
        val side = width * sideFraction.coerceIn(0.20f, 0.40f)
        return when {
            x < side -> GestureZone.LEFT
            x > width - side -> GestureZone.RIGHT
            else -> GestureZone.CENTER
        }
    }

    fun classifySwipe(
        deltaX: Float,
        deltaY: Float,
        touchSlop: Float,
        directionRatio: Float = 1.3f
    ): SwipeAction {
        if (kotlin.math.abs(deltaY) < touchSlop) return SwipeAction.NONE
        if (kotlin.math.abs(deltaY) <= kotlin.math.abs(deltaX) * directionRatio) return SwipeAction.NONE
        return if (deltaY < 0f) SwipeAction.NEXT else SwipeAction.PREVIOUS
    }
}

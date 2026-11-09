package com.reelsgallery.player.player

import org.junit.Assert.assertEquals
import org.junit.Test

class PlayerGestureClassifierTest {
    @Test fun leftAndRightZonesAreBasedOnInitialX() {
        assertEquals(GestureZone.LEFT, PlayerGestureClassifier.zoneOf(20f, 1000f, 0.28f))
        assertEquals(GestureZone.CENTER, PlayerGestureClassifier.zoneOf(500f, 1000f, 0.28f))
        assertEquals(GestureZone.RIGHT, PlayerGestureClassifier.zoneOf(980f, 1000f, 0.28f))
    }

    @Test fun verticalSwipeRequiresTouchSlopAndDominantVerticalMotion() {
        assertEquals(SwipeAction.NONE, PlayerGestureClassifier.classifySwipe(0f, -12f, 24f))
        assertEquals(SwipeAction.NONE, PlayerGestureClassifier.classifySwipe(50f, -55f, 24f))
        assertEquals(SwipeAction.NEXT, PlayerGestureClassifier.classifySwipe(10f, -180f, 24f))
        assertEquals(SwipeAction.PREVIOUS, PlayerGestureClassifier.classifySwipe(10f, 180f, 24f))
    }
}

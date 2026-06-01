package com.confettiprototype.androidsample

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.Thread.sleep

@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun confettiButton_clickTriggersBurstWithoutCrashing() {
        onView(withContentDescription("ConfettiView")).check(matches(isDisplayed()))
        onView(withText("Confetti")).check(matches(isDisplayed()))

        repeat(3) {
            onView(withText("Confetti")).perform(click())
        }

        onView(withContentDescription("ConfettiView")).check(matches(isDisplayed()))
    }

    @Test
    fun recording_singleBurstForVideoCapture() {
        onView(withContentDescription("ConfettiView")).check(matches(isDisplayed()))
        onView(withText("Confetti")).perform(click())
        sleep(4500)
        onView(withContentDescription("ConfettiView")).check(matches(isDisplayed()))
    }
}

package ron.fuelmanager.Activity;

import android.content.Intent;
import android.os.SystemClock;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.action.GeneralClickAction;
import android.support.test.espresso.action.GeneralLocation;
import android.support.test.espresso.action.GeneralSwipeAction;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Swipe;
import android.support.test.espresso.action.Tap;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.test.AndroidTestRunner;
import android.view.MenuItem;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


import android.support.test.rule.ActivityTestRule;
import android.view.View;
import android.widget.NumberPicker;

import org.junit.runner.RunWith;


import ron.fuelmanager.R;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.*;

/**
 * Created by amit on 1/14/2018.
 */



@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest {
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);

    int bugetValueToChange = 49;
    String bugetValueToChangeString = "49.0";
    @Test
    public void changeBudget()
    {
        onView(withId(R.id.tv_budget)).perform(click());
        ViewInteraction numPicker = onView(withClassName(Matchers.equalTo(NumberPicker.class.getName())));
        numPicker.perform(setNumber(bugetValueToChange));
        onView(withText(android.R.string.ok)).perform(click());
        onView(withId(R.id.tv_budget)).check(matches(withText(bugetValueToChangeString)));
    }
    @Test
    public void returnFromMapWhithoutChanges()
    {
        changeBudget();//buget is now "bugetValueToChange"
        onView(withId(R.id.btn_pick_route)).perform(click());
        UiDevice mDevice = UiDevice.getInstance(getInstrumentation());
        assertThat(mDevice, notNullValue());
        mDevice.pressBack();
        //is not changed
        onView(withId(R.id.tv_budget)).check(matches(withText(bugetValueToChangeString)));

    }
    public static ViewAction setNumber(final int num) {
        return new ViewAction() {
            @Override
            public void perform(UiController uiController, View view) {
                NumberPicker np = (NumberPicker) view;
                np.setValue(num);

            }

            @Override
            public String getDescription() {
                return "Set the passed number into the NumberPicker";
            }

            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(NumberPicker.class);
            }
        };
    }
}
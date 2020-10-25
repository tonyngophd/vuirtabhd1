package com.suas.vuirtab1;

import android.content.Context;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented full_screen_video_activity, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under full_screen_video_activity.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.suas.vuirtab1", appContext.getPackageName());
    }
}

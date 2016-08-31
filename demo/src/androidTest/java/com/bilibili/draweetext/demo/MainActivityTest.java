package com.bilibili.draweetext.demo;


import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.bilibili.draweetext.DraweeSpan;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void mainActivityTest() {
        ViewInteraction draweeTextView = onView(withId(R.id.text));

        draweeTextView
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
                .check(ViewAssertions.matches(hasDraweeSpan(spanMatcher("http://img.yo9.com/24fe1ed09fbc11e59d8700163e00043c"))));
    }
    Matcher<DraweeSpan> spanMatcher(final String uri) {
        return new TypeSafeMatcher<DraweeSpan>() {
            @Override
            protected boolean matchesSafely(DraweeSpan item) {
                return TextUtils.equals(item.getImageUri(), uri);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("draweepan: "+uri);
            }
        };
    }
    Matcher<View> hasDraweeSpan(final Matcher<DraweeSpan> spanMatcher) {
        return new BoundedMatcher<View, TextView>(TextView.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("with draweespan: ");
                spanMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(TextView textView) {
                CharSequence text = textView.getText();
                IsInstanceOf.instanceOf(Spanned.class).matches(text);
                DraweeSpan[] spans = ((Spanned) text).getSpans(0, text.length(), DraweeSpan.class);
                assertThat(spans, notNullValue());
                assertThat(spans, not(emptyArray()));
                return hasItemInArray(spanMatcher).matches(spans);

            }
        };
    }

}

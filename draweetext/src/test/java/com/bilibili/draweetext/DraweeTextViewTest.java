package com.bilibili.draweetext;

import android.app.Activity;
import android.text.SpannableStringBuilder;
import android.text.Spanned;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * @author yrom.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class DraweeTextViewTest {

    private DraweeTextView mText;

    @Before
    public void setup() {
        Activity activity = Robolectric.buildActivity(Activity.class).create().get();
        mText = new DraweeTextView(activity);

    }

    @Test
    public void testLifeCycle_spanned() {
        DraweeSpan span1 = Mockito.mock(DraweeSpan.class);
        DraweeSpan span2 = Mockito.mock(DraweeSpan.class);
        SpannableStringBuilder text = new SpannableStringBuilder("adfadfadfadfasdfbaadfadfasdff");
        text.setSpan(span1, 0, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(span2, 6, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        InOrder inOrder = Mockito.inOrder(span1, span2);
        mText.setText(text);
        // should never called attach or detach before textview 'attachToWindow'
        Mockito.verify(span1, Mockito.never()).onDetach();
        Mockito.verify(span1, Mockito.never()).onAttach(mText);
        mText.onAttachedToWindow();
        inOrder.verify(span1).onAttach(mText);
        inOrder.verify(span2).onAttach(mText);
        mText.onStartTemporaryDetach();
        inOrder.verify(span1).onDetach();
        inOrder.verify(span2).onDetach();
        mText.onFinishTemporaryDetach();
        inOrder.verify(span1).onAttach(mText);
        inOrder.verify(span2).onAttach(mText);
        mText.onDetachedFromWindow();
        inOrder.verify(span1).onDetach();
        inOrder.verify(span2).onDetach();
    }

    @Test
    public void testLifeCycle_resetText() {
        DraweeSpan span1 = Mockito.mock(DraweeSpan.class);
        DraweeSpan span2 = Mockito.mock(DraweeSpan.class);
        SpannableStringBuilder text1 = new SpannableStringBuilder("adfadfadfadfafadfasdff");
        text1.setSpan(span1, 0, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        SpannableStringBuilder text2 = new SpannableStringBuilder("adfadfadfadfafadfasdff");
        text2.setSpan(span2, 2, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        mText.setText(text1);
        mText.onAttachedToWindow();
        InOrder inOrder = Mockito.inOrder(span1, span2);
        inOrder.verify(span1).onAttach(mText);
        // reset text in same textview
        mText.setText(text2);
        inOrder.verify(span1).onDetach();
        inOrder.verify(span2).onAttach(mText);
        mText.onDetachedFromWindow();
        inOrder.verify(span2).onDetach();
    }
}
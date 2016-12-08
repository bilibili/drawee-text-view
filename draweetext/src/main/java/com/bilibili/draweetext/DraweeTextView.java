/*
 * Copyright (C) 2016 Bilibili
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bilibili.draweetext;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Spanned;
import android.util.AttributeSet;
import android.widget.TextView;

import com.facebook.drawee.drawable.ForwardingDrawable;
import com.facebook.imagepipeline.animated.base.AnimatableDrawable;

/**
 * Like {@link com.facebook.drawee.view.DraweeView} that displays drawables {@link DraweeSpan} but surrounded with text.
 *
 * @author yrom
 */
public class DraweeTextView extends TextView {
    public DraweeTextView(Context context) {
        super(context);
    }

    public DraweeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DraweeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DraweeTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private boolean mHasDraweeInText;
    // detect drawee-spans has been attached or not
    private boolean mIsSpanAttached;

    @Override
    public void setText(CharSequence text, BufferType type) {
        boolean wasSpanAttached = mIsSpanAttached;
        if (mHasDraweeInText && wasSpanAttached) {
            onDetach(); // detach all old images
            mHasDraweeInText = false;
        }
        if (text instanceof Spanned) {
            // find DraweeSpan in text
            DraweeSpan[] spans = ((Spanned) text).getSpans(0, text.length(), DraweeSpan.class);
            mHasDraweeInText = spans.length > 0;
        }
        super.setText(text, type);
        if (mHasDraweeInText && wasSpanAttached) {
            onAttach(); // reattach drawee spans
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        onAttach();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        onDetach();
    }


    @Override
    public void onStartTemporaryDetach() {
        super.onStartTemporaryDetach();
        onDetach();
    }

    @Override
    public void onFinishTemporaryDetach() {
        super.onFinishTemporaryDetach();
        onAttach();
    }

    @Override
    public void invalidateDrawable(Drawable dr) {
        if (mHasDraweeInText) {
            /* invalidate the whole view in this case because it's very
             * hard to know what the bounds of drawables actually is.
             */
            invalidate();
        } else {
            super.invalidateDrawable(dr);
        }
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || mHasDraweeInText
                // only schedule animation on AnimatableDrawable
                && (who instanceof ForwardingDrawable && who.getCurrent() instanceof AnimatableDrawable);
    }

    /**
     * Attach DraweeSpans in text
     */
    final void onAttach() {
        DraweeSpan[] images = getImages();
        for (DraweeSpan image : images) {
            image.onAttach(this);
        }
        mIsSpanAttached = true;
    }

    private DraweeSpan[] getImages() {
        if (mHasDraweeInText && length() > 0)
            return ((Spanned) getText()).getSpans(0, length(), DraweeSpan.class);
        return new DraweeSpan[0]; //TODO: pool empty typed array
    }

    /**
     * Detach all of the DraweeSpans in text
     */
    final void onDetach() {
        DraweeSpan[] images = getImages();
        for (DraweeSpan image : images) {
            Drawable drawable = image.getDrawable();
            // reset callback first
            if (drawable != null) {
                unscheduleDrawable(drawable);
            }
            image.onDetach();
        }
        mIsSpanAttached = false;
    }
}

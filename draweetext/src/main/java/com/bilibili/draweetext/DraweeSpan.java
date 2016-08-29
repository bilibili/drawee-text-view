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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.text.style.DynamicDrawableSpan;
import android.util.Log;

import com.facebook.common.executors.UiThreadImmediateExecutorService;
import com.facebook.common.logging.FLog;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.datasource.DataSubscriber;
import com.facebook.drawable.base.DrawableWithCaches;
import com.facebook.drawee.components.DeferredReleaser;
import com.facebook.drawee.drawable.ForwardingDrawable;
import com.facebook.drawee.drawable.OrientedDrawable;
import com.facebook.drawee.drawable.TransformAwareDrawable;
import com.facebook.imagepipeline.animated.base.AnimatableDrawable;
import com.facebook.imagepipeline.animated.base.AnimatedDrawable;
import com.facebook.imagepipeline.animated.base.AnimatedImageResult;
import com.facebook.imagepipeline.common.ImageDecodeOptions;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.image.CloseableAnimatedImage;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.image.CloseableStaticBitmap;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.lang.ref.WeakReference;

/**
 * Like {@link com.facebook.drawee.interfaces.DraweeHierarchy} that displays a placeholder
 * until actual image is set.
 * <p/>
 * Usage in DraweeTextView's text.
 *
 * @author yrom
 */
public class DraweeSpan extends DynamicDrawableSpan implements DeferredReleaser.Releasable{
    private static final String TAG = "DraweeSpan";

    private static Drawable createEmptyDrawable() {
        ColorDrawable d = new ColorDrawable(Color.TRANSPARENT);
        d.setBounds(0, 0, 100, 100);
        return d;
    }

    private final DeferredReleaser mDeferredReleaser;
    private final ForwardingDrawable mActualDrawable;
    private CloseableReference<CloseableImage> mFetchedImage;
    private DataSource<CloseableReference<CloseableImage>> mDataSource;
    private boolean mIsRequestSubmitted;
    private Drawable mDrawable;
    private Drawable mPlaceHolder;
    private DraweeTextView mAttachedView;
    private String mImageUri;
    private Point mLayout = new Point();
    private Rect mMargin = new Rect();
    private boolean mIsAttached;
    private boolean mShouldShowAnim = false;

    /**
     * Use {@link Builder} to build a DraweeSpan.
     */
    private DraweeSpan(String uri, int verticalAlignment, Drawable placeHolder, boolean showAnim) {
        super(verticalAlignment);
        mImageUri = uri;
        mShouldShowAnim = showAnim;
        mDeferredReleaser = DeferredReleaser.getInstance();
        mPlaceHolder = placeHolder;
        // create forwarding drawable with placeholder
        mActualDrawable = new ForwardingDrawable(mPlaceHolder);
    }

    protected void layout() {
        mActualDrawable.setBounds(0, 0, mLayout.x, mLayout.y);
    }

    @Override
    public Drawable getDrawable() {
        return mActualDrawable;
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        Drawable d = getDrawable();
        Rect rect = d.getBounds();

        if (fm != null) {
            fm.ascent = -rect.bottom - mMargin.top;
            fm.descent = 0;

            fm.top = fm.ascent;
            fm.bottom = 0;
        }

        return rect.right + mMargin.left + mMargin.right;
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        super.draw(canvas, text, start, end, x + mMargin.left, top, y, bottom, paint);
    }

    public void setImage(Drawable drawable) {
        if (mDrawable != drawable) {
            releaseDrawable(mDrawable);
            setDrawableInner(drawable);
            if (drawable instanceof AnimatedDrawable) {
                ((AnimatableDrawable) drawable).start();
            }
            mDrawable = drawable;
        }
    }

    private void setDrawableInner(Drawable drawable) {
        if(drawable == null) {
            return;
        }
        mActualDrawable.setDrawable(drawable);
    }

    public void reset() {
        setDrawableInner(mPlaceHolder);
    }

    public void onAttach(@NonNull DraweeTextView view) {
        mIsAttached = true;
        if (mAttachedView != view) {
            mActualDrawable.setCallback(null);
            if (mAttachedView != null) {
                throw new IllegalStateException("has been attached to view:" + mAttachedView);
            }
            mAttachedView = view;
            setDrawableInner(mDrawable);
            mActualDrawable.setCallback(mAttachedView);
        }
        mDeferredReleaser.cancelDeferredRelease(this);
        if (!mIsRequestSubmitted) {
            try {
                ImagePipelineFactory.getInstance();
            } catch (NullPointerException e) {
                // Image pipeline is not initialized
                ImagePipelineFactory.initialize(mAttachedView.getContext().getApplicationContext());
            }
            submitRequest();
        } else if (mShouldShowAnim && mDrawable instanceof AnimatableDrawable) {
            ((AnimatableDrawable) mDrawable).start();
        }
    }

    private void submitRequest() {
        if(TextUtils.isEmpty(getImageUri())) {
            return;
        }

        mIsRequestSubmitted = true;
        final String id = getId();
        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(getImageUri()))
                .setImageDecodeOptions(ImageDecodeOptions.newBuilder().setDecodePreviewFrame(true).build())
                .build();

        mDataSource = ImagePipelineFactory.getInstance().getImagePipeline()
                .fetchDecodedImage(request, null);
        DataSubscriber<CloseableReference<CloseableImage>> subscriber
                = new BaseDataSubscriber<CloseableReference<CloseableImage>>() {
            @Override
            protected void onNewResultImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {
                boolean isFinished = dataSource.isFinished();
                CloseableReference<CloseableImage> result = dataSource.getResult();
                if (result != null) {
                    onNewResultInternal(id, dataSource, result, isFinished);
                } else if (isFinished) {
                    onFailureInternal(id, dataSource, new NullPointerException(), /* isFinished */ true);
                }
            }

            @Override
            protected void onFailureImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {
                onFailureInternal(id, dataSource, dataSource.getFailureCause(), /* isFinished */ true);
            }
        };
        mDataSource.subscribe(subscriber, UiThreadImmediateExecutorService.getInstance());
    }

    @NonNull
    protected String getImageUri() {
        return mImageUri;
    }

    protected String getId() {
        return String.valueOf(getImageUri().hashCode());
    }

    private void onFailureInternal(String id,
            DataSource<CloseableReference<CloseableImage>> dataSource,
            Throwable throwable, boolean isFinished) {
        if (FLog.isLoggable(Log.WARN)) {
            FLog.w(DraweeSpan.class, id + " load failure", throwable);
        }
        // ignored this result
        if (!getId().equals(id)
                || dataSource != mDataSource
                || !mIsRequestSubmitted) {
            dataSource.close();
            return;
        }
        if (isFinished) {
            mDataSource = null;
            // Set the previously available image if available.
            setDrawableInner(mDrawable);
        }
    }

    private void onNewResultInternal(String id,
            DataSource<CloseableReference<CloseableImage>> dataSource,
            CloseableReference<CloseableImage> result,
            boolean isFinished) {
        // ignored this result
        if (!getId().equals(id)
                || dataSource != mDataSource
                || !mIsRequestSubmitted) {
            CloseableReference.closeSafely(result);
            dataSource.close();
            return;
        }
        Drawable drawable;
        try {
            drawable = createDrawable(result);
        } catch (Exception exception) {
            CloseableReference.closeSafely(result);
            onFailureInternal(id, dataSource, exception, isFinished);
            return;
        }
        CloseableReference previousImage = mFetchedImage;
        Drawable previousDrawable = mDrawable;
        mFetchedImage = result;
        try {
            // set the new image
            if (isFinished) {
                mDataSource = null;
                setImage(drawable);
            }
        } finally {
            if (previousDrawable != null && previousDrawable != drawable) {
                releaseDrawable(previousDrawable);
            }
            if (previousImage != null && previousImage != result) {
                CloseableReference.closeSafely(previousImage);
            }
        }
    }

    private Drawable createDrawable(CloseableReference<CloseableImage> result) {
        CloseableImage closeableImage = result.get();
        if (closeableImage instanceof CloseableStaticBitmap) {
            CloseableStaticBitmap closeableStaticBitmap = (CloseableStaticBitmap) closeableImage;
            BitmapDrawable bitmapDrawable = createBitmapDrawable(closeableStaticBitmap.getUnderlyingBitmap());
            return (closeableStaticBitmap.getRotationAngle() != 0 && closeableStaticBitmap.getRotationAngle() != -1
                    ? new OrientedDrawable(bitmapDrawable, closeableStaticBitmap.getRotationAngle()) : bitmapDrawable);
        } else if (closeableImage instanceof CloseableAnimatedImage) {
            AnimatedImageResult image = ((CloseableAnimatedImage) closeableImage).getImageResult();

            if (mShouldShowAnim) {
                AnimatedDrawable drawable = ImagePipelineFactory.getInstance().getAnimatedDrawableFactory().create(image);
                drawable.setLogId(getId());
                return drawable;
            }

            int frame = image.getFrameForPreview();
            CloseableReference<Bitmap> bitmap = null;
            if (frame >= 0) {
                bitmap = image.getDecodedFrame(frame);
            }
            if (bitmap == null) {
                bitmap = image.getPreviewBitmap();
            }
            if (bitmap != null && bitmap.get() != null) {
                BitmapDrawable bitmapDrawable = createBitmapDrawable(bitmap.get());
                return bitmapDrawable;
            }
        }
        throw new UnsupportedOperationException("Unrecognized image class: " + closeableImage);
    }

    protected BitmapDrawable createBitmapDrawable(Bitmap bitmap) {
        BitmapDrawable drawable;
        if (mAttachedView != null) {
            final Context context = mAttachedView.getContext();
            drawable = new BitmapDrawable(context.getResources(), bitmap);
        } else {
            // can't happen
            drawable = new BitmapDrawable(null, bitmap);
        }
        return drawable;
    }

    public void onDetach() {
        if (!mIsAttached)
            return;
        if (mShouldShowAnim && mDrawable instanceof AnimatableDrawable) {
            ((AnimatableDrawable) mDrawable).stop();
        }
        mActualDrawable.setCallback(null);
        mAttachedView = null;
        reset();
        mDeferredReleaser.scheduleDeferredRelease(this);
    }

    @Override
    public void release() {
        mIsRequestSubmitted = false;
        mIsAttached = false;
        mAttachedView = null;
        if (mDataSource != null) {
            mDataSource.close();
            mDataSource = null;
        }
        if (mDrawable != null) {
            releaseDrawable(mDrawable);
        }
        mDrawable = null;
        if (mFetchedImage != null) {
            CloseableReference.closeSafely(mFetchedImage);
            mFetchedImage = null;
        }
    }

    void releaseDrawable(@Nullable Drawable drawable) {
        if (drawable instanceof AnimatableDrawable && ((AnimatableDrawable) drawable).isRunning()) {
            ((AnimatableDrawable) drawable).stop();
        }
        if (drawable instanceof DrawableWithCaches) {
            ((DrawableWithCaches) drawable).dropCaches();
        }
    }

    public static class Builder {
        String uri;
        int width = 100;
        int height = 100;
        int verticalAlignment = ALIGN_BOTTOM;
        Drawable placeholder = createEmptyDrawable();
        boolean showAnim;
        Rect margin = new Rect();

        public Builder(String uri) {
            this(uri, false);
        }

        /**
         * DraweeSpan builder.
         *
         * @param uri image uri.
         * @param verticalAlignment one of {@link #ALIGN_BOTTOM} or {@link #ALIGN_BASELINE}.
         */
        public Builder(String uri, boolean alignBaseline) {
            this.uri = uri;
            if(alignBaseline) {
                this.verticalAlignment = ALIGN_BASELINE;
            }
        }

        public Builder setLayout(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        /**
         * You can set margin in left, right and top. Bottom is in baseline.
         */
        public Builder setMargin(int margin) {
            this.margin.set(margin, margin, margin, 0);
            return this;
        }

        /**
         * You can set margin in left, right and top. Bottom is in baseline.
         */
        public Builder setMargin(int left, int top, int right) {
            this.margin.set(left, top, right, 0);
            return this;
        }

        public Builder setPlaceHolderImage(Drawable placeholder) {
            this.placeholder = placeholder;
            return this;
        }

        /**
         * Show anim in animate drawable.
         * @param showAnim If set true, gif image would animate immediately.
         */
        public Builder setShowAnimaImmediately(boolean showAnim) {
            this.showAnim = showAnim;
            return this;
        }

        public DraweeSpan build() {
            DraweeSpan span = new DraweeSpan(uri, verticalAlignment, placeholder, showAnim);
            span.mLayout.set(width, height);
            span.mMargin.set(margin.left, margin.top, margin.right, 0);
            span.layout();
            return span;
        }
    }
}
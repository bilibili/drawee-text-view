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
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.style.DynamicDrawableSpan;
import android.util.Log;
import android.view.View;

import com.facebook.common.executors.UiThreadImmediateExecutorService;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.datasource.DataSubscriber;
import com.facebook.drawable.base.DrawableWithCaches;
import com.facebook.drawee.components.DeferredReleaser;
import com.facebook.drawee.drawable.ForwardingDrawable;
import com.facebook.drawee.drawable.OrientedDrawable;
import com.facebook.imagepipeline.animated.base.AnimatedDrawable;
import com.facebook.imagepipeline.animated.base.AnimatedImageResult;
import com.facebook.imagepipeline.common.ImageDecodeOptions;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.image.CloseableAnimatedImage;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.image.CloseableStaticBitmap;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

/**
 * Like {@link com.facebook.drawee.interfaces.DraweeHierarchy} that displays a placeholder
 * until actual image is set.
 * <p/>
 * Usage in DraweeTextView's text.
 *
 * @author yrom
 */
public class DraweeSpan extends DynamicDrawableSpan implements DeferredReleaser.Releasable, Drawable.Callback {


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
    private View mAttachedView;
    private String mImageUri;
    private boolean mIsAttached;
    private boolean mShouldShowAnim = false;

    /**
     * @see ImageRequest#fromUri(String)
     * @see com.facebook.common.util.UriUtil
     */
    public DraweeSpan(String uri) {
        this(uri, false);
    }

    public DraweeSpan(String uri, Drawable placeHolder) {
        this(uri, placeHolder, false);
    }

    public DraweeSpan(String uri, boolean showAnim) {
        this(uri, createEmptyDrawable(), showAnim);
    }

    public DraweeSpan(String uri, Drawable placeHolder, boolean showAnim) {
        super(ALIGN_BOTTOM);
        mImageUri = uri;
        mShouldShowAnim = showAnim;
        mDeferredReleaser = DeferredReleaser.getInstance();
        mPlaceHolder = placeHolder;
        // create forwarding drawable with placeholder
        mActualDrawable = new ForwardingDrawable(mPlaceHolder);
        Rect bounds = mPlaceHolder.getBounds();
        if (bounds.right == 0 || bounds.bottom == 0) {
            mActualDrawable.setBounds(0, 0, mPlaceHolder.getIntrinsicWidth(), mPlaceHolder.getIntrinsicHeight());
        } else {
            mActualDrawable.setBounds(bounds);
        }
    }

    @Override
    public Drawable getDrawable() {
        return mActualDrawable;
    }

    public void setImageWithIntrinsicBounds(Drawable drawable) {
        if (mDrawable != drawable) {
            releaseDrawable(mDrawable);
            mActualDrawable.setDrawable(drawable);
            if (drawable instanceof AnimatedDrawable) {
                drawable.setCallback(DraweeSpan.this);
            }
            mDrawable = drawable;
        }
    }

    public void reset() {
        mActualDrawable.setDrawable(mPlaceHolder);
    }

    /**
     * set bounds
     */
    public void setSize(int width, int height) {
        mActualDrawable.setBounds(0, 0, width, height);
    }

    public void onAttach(@NonNull View view) {
        mIsAttached = true;
        if (mAttachedView != view) {
            mActualDrawable.setCallback(null);
            if (mAttachedView != null) {
                throw new IllegalStateException("has been attached to view:" + mAttachedView);
            }
            mAttachedView = view;
            mActualDrawable.setCallback(mAttachedView);

            if (mDrawable != null) {
                mActualDrawable.setDrawable(mDrawable);
            }
            if (mActualDrawable.getCurrent() instanceof AnimatedDrawable) {
                mActualDrawable.getCurrent().setCallback(this);
            }
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
        }
    }

    private void submitRequest() {
        mIsRequestSubmitted = true;
        final String id = getId();
        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(getImageUri()))
                .setImageDecodeOptions(ImageDecodeOptions.newBuilder().setDecodePreviewFrame(false).build()).build();

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
        if (BuildConfig.DEBUG) {
            Log.e("ImageSpan", id + " load failure", throwable);
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
            if (mDrawable != null) {
                mActualDrawable.setDrawable(mDrawable);
            }
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
                setImageWithIntrinsicBounds(drawable);
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
                final AnimatedDrawable drawable = ImagePipelineFactory.getInstance().getAnimatedDrawableFactory().create(image);
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                //mActualDrawable.setBounds(drawable.getBounds());
                drawable.setCallback(mAttachedView);
                drawable.start();
                return drawable;
            }

            int frame = image.getFrameForPreview();
            CloseableReference<Bitmap> bitmap;
            if (frame >= 0) {
                bitmap = image.getDecodedFrame(frame);
            } else {
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
        if (drawable instanceof DrawableWithCaches) {
            ((DrawableWithCaches) drawable).dropCaches();
        }
    }

    @Override
    public void invalidateDrawable(Drawable who) {
        mAttachedView.invalidate();
    }

    @Override
    public void scheduleDrawable(Drawable who, Runnable what, long when) {
        mAttachedView.postDelayed(what, when - SystemClock.uptimeMillis());
    }

    @Override
    public void unscheduleDrawable(Drawable who, Runnable what) {
        mAttachedView.removeCallbacks(what);
    }
}
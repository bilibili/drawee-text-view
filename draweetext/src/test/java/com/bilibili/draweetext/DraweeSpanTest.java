package com.bilibili.draweetext;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import com.facebook.common.references.CloseableReference;
import com.facebook.common.references.ResourceReleaser;
import com.facebook.datasource.DataSource;
import com.facebook.datasource.DataSources;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.image.CloseableStaticBitmap;
import com.facebook.imagepipeline.image.ImmutableQualityInfo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowBitmap;
import org.robolectric.shadows.ShadowLooper;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * @author yrom.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21, manifest = Config.NONE)
public class DraweeSpanTest {
    @Test
    public void testBuild() {
        Drawable ph = mock(Drawable.class);
        DraweeSpan span = new DraweeSpan.Builder("http://test")
                .setLayout(100, 100)
                .setPlaceHolderImage(ph)
                .build();
        assertEquals("http://test", span.getImageUri());
        assertEquals(ph, span.getDrawable().getCurrent());
        assertEquals(new Rect(0, 0, 100, 100), span.getDrawable().getBounds());
    }

    @Test
    public void testLifeCycle(){
        Activity activity = Robolectric.buildActivity(Activity.class).create().get();
        ResourceReleaser<Bitmap> releaser = mock(ResourceReleaser.class);
        Bitmap bitmap = ShadowBitmap.createBitmap(1,1, Bitmap.Config.ARGB_8888);
        CloseableImage image = Mockito.spy(new CloseableStaticBitmap(
                bitmap,
                releaser,
                ImmutableQualityInfo.FULL_QUALITY, 0));

        DraweeTextView textview = new DraweeTextView(activity);
        DraweeSpan span = new DraweeSpan.Builder("http://test")
                .build();
        DraweeSpan spy = spy(span);
        DataSource<CloseableReference<CloseableImage>> dataSource = DataSources.immediateDataSource(CloseableReference.of(image));
        doReturn(dataSource)
                .when(spy).fetchDecodedImage();
        spy.onAttach(textview);
        ShadowLooper.runUiThreadTasks();
        verify(spy).createBitmapDrawable(bitmap);
        verify(image, never()).close();
        spy.onDetach();
        verify(spy).reset();
        ShadowLooper.runUiThreadTasks();
        verify(image).close();
        verify(releaser).release(bitmap);
    }
}
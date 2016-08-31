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

package com.bilibili.draweetext.demo;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import com.bilibili.draweetext.DraweeSpan;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Just for develop */
//        FLog.setMinimumLoggingLevel(Log.VERBOSE);

        setContentView(R.layout.activity_main);
        TextView textview = (TextView) findViewById(R.id.text);
        textview.setMovementMethod(ScrollingMovementMethod.getInstance());
        textview.setText(buildText());
    }

    CharSequence buildText() {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append("2333333333333");
        builder.append("\nwh=50, margin=0:www");
        int start = builder.length();
        builder.append("[img]");
        builder.setSpan(new DraweeSpan.Builder("http://img.yo9.com/24fe1ed09fbc11e59d8700163e00043c")
                            .setLayout(50, 50).build(),
                        start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append("www");
        builder.append("\nwh=100, margin=8, align baseline:");
        start = builder.length();
        builder.append("[img]");
        builder.setSpan(new DraweeSpan.Builder("http://img.yo9.com/24fe1ed09fbc11e59d8700163e00043c", true)
                            .setLayout(100, 100)
                            .setMargin(8)
                            .build(),
                start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append("www");
        builder.append("\nwh=100, margin=4, 4, 8，webp:");
        start = builder.length();
        builder.append("[img]");
        builder.setSpan(new DraweeSpan.Builder("http://img.yo9.com/25126a209fbc11e59d8700163e00043c@100w.webp")
                            .setLayout(100, 100).setMargin(4, 4, 8).build(),
                start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append("www");
        builder.append("\nwh=150, margin=0:www");
        start = builder.length();
        builder.append("[img]");
        builder.setSpan(new DraweeSpan.Builder("http://img.yo9.com/250c9dc09fbc11e59d8700163e00043c")
                            .setLayout(150, 150).build(),
                start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append("www");
        builder.append("\n\n");
        start = builder.length();
        builder.append("[emotion:tv_cheers]");
        DraweeSpan span = new DraweeSpan.Builder("http://static.yo9.com/web/emotions/tv_cheers.png").build();
        builder.setSpan(span, start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append("bilibili- ( ゜- ゜)つロ 乾杯~\n");


        builder.append("why I so diao");
        start = builder.length();
        builder.append("[img]");
        Drawable placeHolder = new ColorDrawable(Color.RED);
        span = new DraweeSpan.Builder("http://img.yo9.com/c82aa6c003d311e6ac3c00163e000cde@320w_720h.jpg")
                   .setLayout(360, 720)
                   .setPlaceHolderImage(placeHolder)
                   .build();
        builder.setSpan(span, start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append("sad sad");
        start = builder.length();
        builder.append("[emotion:tv_sad]");
        placeHolder = getResources().getDrawable(R.mipmap.ic_launcher);
        span = new DraweeSpan.Builder("http://static.yo9.com/web/emotions/tv_sad.png")
                   .setLayout(150, 150)
                   .setPlaceHolderImage(placeHolder)
                   .build();
        builder.setSpan(span, start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        builder.append("\n\n");
        builder.append("This is a gif, margin=10:");
        start = builder.length();
        builder.append("[gif:d559f520246811e69a4a00163e000cdb]");
        placeHolder = new ColorDrawable(Color.BLUE);
        builder.setSpan(new DraweeSpan.Builder("http://img.yo9.com/d559f520246811e69a4a00163e000cdb")
                            .setPlaceHolderImage(placeHolder)
                            .setLayout(200, 197)
                            .setMargin(10)
                            .setShowAnimaImmediately(true)
                            .build(),
                start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append("\nDisabled animation");
        start = builder.length();
        builder.append("[gif:d559f520246811e69a4a00163e000cdb]");
        placeHolder = new ColorDrawable(Color.BLUE);
        builder.setSpan(new DraweeSpan.Builder("http://img.yo9.com/d559f520246811e69a4a00163e000cdb")
                            .setPlaceHolderImage(placeHolder)
                            .setLayout(200, 197)
                            .setShowAnimaImmediately(false)
                            .build(),
                start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return builder;
    }
}

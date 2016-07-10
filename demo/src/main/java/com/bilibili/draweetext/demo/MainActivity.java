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
        setContentView(R.layout.activity_main);
        TextView textview = (TextView) findViewById(R.id.text);
        textview.setMovementMethod(ScrollingMovementMethod.getInstance());
        textview.setText(buildText());
    }

    CharSequence buildText() {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append("2333333333333\n");
        int start = builder.length();
        builder.append("[img]");
        builder.setSpan(new DraweeSpan("http://img.yo9.com/24fe1ed09fbc11e59d8700163e00043c"),
                start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        start = builder.length();
        builder.append("[img]");
        builder.setSpan(new DraweeSpan("http://img.yo9.com/25126a209fbc11e59d8700163e00043c"),
                start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        start = builder.length();
        builder.append("[img]");
        builder.setSpan(new DraweeSpan("http://img.yo9.com/250c9dc09fbc11e59d8700163e00043c"),
                start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append("\n\n");
        start = builder.length();
        builder.append("[emotion:tv_cheers]");
        DraweeSpan span = new DraweeSpan("http://static.yo9.com/web/emotions/tv_cheers.png");
        builder.setSpan(span, start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append("bilibili- ( ゜- ゜)つロ 乾杯~\n");


        builder.append("why I so diao\n");
        start = builder.length();
        builder.append("[img]");
        Drawable placeHolder = new ColorDrawable(Color.RED);
        placeHolder.setBounds(0, 0, 360, 720);
        span = new DraweeSpan("http://img.yo9.com/c82aa6c003d311e6ac3c00163e000cde@320w_720h.jpg", placeHolder);
        builder.setSpan(span, start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append("\n\n");
        builder.append("sad sad");
        start = builder.length();
        builder.append("[emotion:tv_sad]");
        placeHolder = getResources().getDrawable(R.mipmap.ic_launcher);
        placeHolder.setBounds(0, 0, 150, 150);
        span = new DraweeSpan("http://static.yo9.com/web/emotions/tv_sad.png", placeHolder);
        builder.setSpan(span, start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        builder.append("\n\n");
        builder.append("This is a gif");
        start = builder.length();
        builder.append("[img]");
        builder.setSpan(new DraweeSpan("asset:///cat.gif", true),
                start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);


        return builder;
    }
}

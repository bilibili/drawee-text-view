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
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bilibili.draweetext.DraweeSpan;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ViewPager pager = findViewById(R.id.container);
        pager.setAdapter(new SectionsAdapter(getSupportFragmentManager()));
    }


    private class SectionsAdapter extends FragmentPagerAdapter {

        SectionsAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Simple";
                case 1:
                    return "ListView";
                case 2:
                    return "RecyclerView";
                default:
                    throw new IndexOutOfBoundsException();
            }
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return SimpleFragment.newInstance();
                case 1:
                    return ListViewFragment.newInstance();
                case 2:
                    return RecyclerViewFragment.newInstance();
                default:
                    throw new IndexOutOfBoundsException();
            }
        }
    }


    public static class SimpleFragment extends Fragment {
        public static Fragment newInstance() {
            return new SimpleFragment();
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_simple, container, false);
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            final TextView textview = view.findViewById(R.id.text1);
            textview.setMovementMethod(ScrollingMovementMethod.getInstance());
            textview.setText(buildText());
            view.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (v.getTag() == "") {
                        textview.setText(buildText());
                        v.setTag(null);
                    } else {
                        textview.setText(buildText2());
                        v.setTag("");
                    }
                }
            });
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

        CharSequence buildText2() {
            SpannableStringBuilder builder = new SpannableStringBuilder();
            builder.append("Reset text in same DraweeTextView~~~~");
            int start = builder.length();
            builder.append("[img]");
            builder.setSpan(new DraweeSpan.Builder("http://img.yo9.com/24fe1ed09fbc11e59d8700163e00043c")
                            .setLayout(50, 50).build(),
                    start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.append("\n\n");
            builder.append("This is a gif, margin=10:");
            start = builder.length();
            builder.append("[gif:d559f520246811e69a4a00163e000cdb]");
            Drawable placeHolder = new ColorDrawable(Color.BLUE);
            builder.setSpan(new DraweeSpan.Builder("http://img.yo9.com/d559f520246811e69a4a00163e000cdb")
                            .setPlaceHolderImage(placeHolder)
                            .setLayout(200, 197)
                            .setMargin(10)
                            .setShowAnimaImmediately(true)
                            .build(),
                    start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.append("\n\n");
            start = builder.length();
            builder.append("[emotion:tv_cheers]");
            builder.setSpan(new DraweeSpan.Builder("http://static.yo9.com/web/emotions/tv_cheers.png").build()
                    , start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.append("bilibili- ( ゜- ゜)つロ 乾杯~\n");
            return builder;
        }

    }

    static String[] EMOTIONS = new String[]{
            "http://i0.hdslb.com/bfs/vip/f80d384875183dfe2e24be13011c595c0210d273.png@.webp",
            "http://i0.hdslb.com/bfs/vip/eb41a8c04840e4f77e76a4bff7a29ac89c432f4e.png@.webp",
            "http://i0.hdslb.com/bfs/vip/16b8794be990cefa6caeba4d901b934a227ee3b8.png@.webp",
            "http://i0.hdslb.com/bfs/vip/d1628c43d35b1530c0504a643ff80b6189fa0a43.png@.webp",
            "http://i0.hdslb.com/bfs/vip/fdb5870f32cfaf7949e0f88a13f6feba4a48b719.png@.webp",
            "http://i0.hdslb.com/bfs/vip/3754ee6e5985bd0bd7dfb668981f2a8733398ebd.png@.webp",
            "http://i0.hdslb.com/bfs/vip/0b41f509351958dbb63d472fec0132d1bd03bd14.png@.webp",
            "http://i0.hdslb.com/bfs/vip/6f058f78bce5d1c9b370c3807c891e685bb68a17.png",
            "http://i0.hdslb.com/bfs/vip/7f482b82a3de44ae14537cbafcbc40cf65f7113e.png",
            "http://i0.hdslb.com/bfs/vip/458982f20f0b7dc68c0ddac89f51ecb7c3d16a83.png",
            "http://i0.hdslb.com/bfs/vip/6846363907204271f0a57472744642c8882b4019.png",
            "http://i0.hdslb.com/bfs/vip/de3aee88f7b6cc20ba9480c96c02f83a844381a9.png",
            "http://i0.hdslb.com/bfs/vip/7a4cb0b644214d476ce198ddf6a7a0aa31311199.png",
            "http://i0.hdslb.com/bfs/vip/a695fe1301aab2675ab6f6e34757c25a863a8617.png@.webp",
            "http://i0.hdslb.com/bfs/vip/77545a5e420e2c43e0e4a7996a71769638ae3f90.png",
            "http://i0.hdslb.com/bfs/vip/af8f017e383a1999e26a7f91c3ec3c83fbb7ba77.png",
            "http://static.yo9.com/web/static/emotions/tv_cheers.png",
            "http://static.yo9.com/web/static/emotions/tv_huaji.png",
            "http://static.yo9.com/web/static/emotions/tv_mygod.png",
            "http://static.yo9.com/web/static/emotions/tv_angry.png",
            "http://static.yo9.com/web/static/emotions/tv_thinking.png",
            "http://static.yo9.com/web/static/emotions/tv_zhuangb.png",
            "http://static.yo9.com/web/static/emotions/tv_shocking.png",
            "http://static.yo9.com/web/static/emotions/tv_pill.png",
            "http://static.yo9.com/web/static/emotions/tv_kira.png",
            "http://static.yo9.com/web/static/emotions/tv_ji.png",
    };

    public static class ListViewFragment extends ListFragment {

        public static Fragment newInstance() {
            return new ListViewFragment();
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setListAdapter(new ArrayAdapter<>(getContext(), R.layout.simple_text,
                    buildArray()));
        }

        private CharSequence[] buildArray() {
            CharSequence[] sequences = new CharSequence[EMOTIONS.length * 2];
            for (int i = 0; i < sequences.length; i++) {
                SpannableStringBuilder builder = new SpannableStringBuilder();
                String emotion = EMOTIONS[i % EMOTIONS.length];
                builder.append(emotion).append('\n');
                int start = builder.length();
                builder.append("[emotion]");
                builder.setSpan(new DraweeSpan.Builder(emotion).setLayout(140, 140).build(), start, builder.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                builder.append("~~~~~~~~~~~~~~~~~~");
                sequences[i] = builder;
            }
            return sequences;
        }
    }

    public static class RecyclerViewFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_recycler, container, false);
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            RecyclerView recyclerView = view.findViewById(R.id.recycler);
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            recyclerView.setHasFixedSize(true);

            recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
                @Override
                public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                    outRect.inset(8, 8);
                }
            });
            final CharSequence[] sequences = new CharSequence[EMOTIONS.length * 2];
            for (int i = 0; i < sequences.length; i++) {
                SpannableStringBuilder builder = new SpannableStringBuilder();
                String emotion = EMOTIONS[i % EMOTIONS.length];
                builder.append(String.valueOf(i)).append("~~~").append('\n');
                int start = builder.length();
                builder.append("[emotion]");
                builder.setSpan(new DraweeSpan.Builder(emotion).setLayout(120, 120).build(), start, builder.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                builder.append("\n~~~~~");
                sequences[i] = builder;
            }
            recyclerView.setAdapter(new RecyclerView.Adapter<TextViewHolder>() {

                @Override
                public TextViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                    return new TextViewHolder(LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.simple_text, parent, false));
                }

                @Override
                public void onBindViewHolder(TextViewHolder holder, int position) {
                    holder.text.setText(sequences[position]);
                }

                @Override
                public int getItemCount() {
                    return sequences.length;
                }
            });
        }

        static class TextViewHolder extends RecyclerView.ViewHolder {
            TextView text;

            TextViewHolder(View itemView) {
                super(itemView);
                text = (TextView) itemView;
            }
        }

        public static Fragment newInstance() {
            return new RecyclerViewFragment();
        }
    }
}

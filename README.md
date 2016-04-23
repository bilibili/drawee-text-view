# DraweeTextView
Simple drawee spannable text view based on [Fresco][1].

# Usage
```java
DraweeTextView textview = (DraweeTextView)findViewById(R.id.text);

SpannableStringBuilder builder = new SpannableStringBuilder();
builder.append("2333333\n")
start = builder.length();
builder.append("[emotion:tv_cheers]");
DraweeSpan span = new DraweeSpan("http://static.yo9.com/web/emotions/tv_cheers.png");
builder.setSpan(span, start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
builder.append("bilibili- ( ゜- ゜)つロ 乾杯~\n");

...

textview.setText(builder);
```
![demo](art/screenshot.png)


[1]: https://github.com/facebook/fresco

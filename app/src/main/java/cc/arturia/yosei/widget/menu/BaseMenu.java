package cc.arturia.yosei.widget.menu;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import cc.arturia.yosei.R;

/**
 * Author: Arturia
 * Date: 2016/12/9
 */
public class BaseMenu extends FrameLayout implements IMenu {

    protected FrameLayout rootLayout;
    protected TextView titleTxt;
    protected TextView summaryTxt;
    protected View underline;

    public BaseMenu(Context context) {
        this(context, null);
    }

    public BaseMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();

        String title = "";
        String summary = "";
        boolean hasUnderline = false;

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MenuItem, defStyleAttr, 0);
        int n = a.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.MenuItem_aliceTitle) {
                title = a.getString(attr);
            } else if (attr == R.styleable.MenuItem_aliceSummary) {
                summary = a.getString(attr);
            } else if (attr == R.styleable.MenuItem_aliceUnderline) {
                hasUnderline = a.getBoolean(attr, false);
            }
        }
        a.recycle();

        if (!TextUtils.isEmpty(title)) {
            titleTxt.setText(title);
        }
        if (!TextUtils.isEmpty(summary)) {
            summaryTxt.setText(summary);
        } else {
            summaryTxt.setVisibility(GONE);
        }
        underline.setVisibility(hasUnderline ? VISIBLE : GONE);
    }

    private void init() {
        inflate(getContext(), R.layout.menu_base, this);
        rootLayout = findViewById(R.id.root_layout);
        rootLayout.setBackgroundResource(R.drawable.bg_item_selector_white);
        titleTxt = findViewById(R.id.tv_title);
        summaryTxt = findViewById(R.id.tv_summary);
        underline = findViewById(R.id.underline);

        setEnabled(true);
        setClickable(true);
    }

    public String getTitle() {
        return titleTxt.getText().toString();
    }

    public void setTitle(String title) {
        titleTxt.setText(title);
    }

    public String getSummary() {
        return summaryTxt.getText().toString();
    }

    public void setSummary(String summary) {
        summaryTxt.setText(summary);
    }

    @Override
    public void setTitleColor(int titleColor) {
        titleTxt.setTextColor(titleColor);
    }

    @Override
    public void setSummaryColor(int summaryColor) {
        summaryTxt.setTextColor(summaryColor);
    }

    @Override
    public void setUnderlineResource(int resId) {
        underline.setBackgroundResource(resId);
    }

    @Override
    public void setRootLayoutResource(int resId) {
        rootLayout.setBackgroundResource(resId);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }
}

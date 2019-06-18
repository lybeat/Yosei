package cc.arturia.yosei.widget.magictab;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cc.arturia.yosei.R;
import cc.arturia.yosei.util.ColorUtil;


/**
 * Author: Arturia
 * Date: 2016/9/17
 */
public class MagicTab extends FrameLayout {

    public static final int MODE_NORMAL = 0;
    public static final int MODE_BLANK = 1;

    public static final int THEME_DAY = 1000;
    public static final int THEME_NIGHT = 1001;

    public interface TabIcon {

        int getPageNormalIconIdDay(int position);

        int getPageNormalIconIdNight(int position);

        int getPagePressedIconId(int position);
    }

    private LinearLayout tabsContainer;
    private ViewPager viewPager;

    private int tabCount;
    private int currentPosition;
    private int mode;
    private int blankIndex;

    private int tabPadding; // dp
    private int tabTextSize; // sp
    private int tabTextColorNormal;
    private int tabTextColorPressed;

    public MagicTab(Context context) {
        this(context, null);
    }

    public MagicTab(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MagicTab(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setWillNotDraw(false);

        tabsContainer = new LinearLayout(context);
        tabsContainer.setOrientation(LinearLayout.HORIZONTAL);
        tabsContainer.setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        tabsContainer.setGravity(Gravity.CENTER);
        addView(tabsContainer);

        initAttrs(context);

        tabTextColorNormal = ColorUtil.getColor(context, R.color.color_content);
    }

    private void initAttrs(Context context) {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        tabPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, dm);
        tabTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, dm);
        tabTextColorNormal = ColorUtil.getColor(context, R.color.color_content);
        tabTextColorPressed = ColorUtil.getColor(context, R.color.color_accent);
    }

    public void setViewPager(ViewPager viewPager) {
        this.viewPager = viewPager;

        if (viewPager.getAdapter() == null) {
            throw new IllegalStateException("ViewPager does not have adapter instance.");
        }

        viewPager.addOnPageChangeListener(new PagerListener());

        notifyDataSetChanged();
    }

    public void setMode(int mode) {
        this.mode = mode;
        notifyDataSetChanged();
    }

    public void notifyDataSetChanged() {
        if (viewPager == null) {
            return;
        }

        tabsContainer.removeAllViews();
        tabCount = viewPager.getAdapter().getCount();
        if (mode == MODE_NORMAL) {
            addTabsNormal();
        } else {
            addTabsBlank();
        }
    }

    private void addTabsNormal() {
        for (int i = 0; i < tabCount; i++) {
            if (i == currentPosition) {
                addTab(i, (String) viewPager.getAdapter().getPageTitle(i), tabTextColorPressed,
                        ((TabIcon) viewPager.getAdapter()).getPagePressedIconId(i));
            } else {
                addTab(i, (String) viewPager.getAdapter().getPageTitle(i), tabTextColorNormal,
                        ((TabIcon) viewPager.getAdapter()).getPageNormalIconIdDay(i));
            }
        }
    }

    private void addTabsBlank() {
        for (int i = 0; i < tabCount + 1; i++) {
            if (i == blankIndex) {
                addBlankTab(blankIndex);
            } else if (i < blankIndex) {
                if (i == currentPosition) {
                    addTab(i, (String) viewPager.getAdapter().getPageTitle(i), tabTextColorPressed,
                            ((TabIcon) viewPager.getAdapter()).getPagePressedIconId(i));
                } else {
                    addTab(i, (String) viewPager.getAdapter().getPageTitle(i), tabTextColorNormal,
                            ((TabIcon) viewPager.getAdapter()).getPageNormalIconIdDay(i));
                }
            } else {
                if (i == currentPosition + 1) {
                    addTab(i, (String) viewPager.getAdapter().getPageTitle(i - 1), tabTextColorPressed,
                            ((TabIcon) viewPager.getAdapter()).getPagePressedIconId(i - 1));
                } else {
                    addTab(i, (String) viewPager.getAdapter().getPageTitle(i - 1), tabTextColorNormal,
                            ((TabIcon) viewPager.getAdapter()).getPageNormalIconIdDay(i - 1));
                }
            }
        }
    }

    public void setBlankIndex(int index) {
        if (viewPager == null) {
            return;
        }

        if (index < 0 || index >= viewPager.getAdapter().getCount()) {
            blankIndex = viewPager.getAdapter().getCount() / 2;
        } else {
            this.blankIndex = index;
        }
        notifyDataSetChanged();
    }

    private void addTab(final int position, String title, int titleColor, int resId) {
        ImageView tabImg = new ImageView(getContext());
        tabImg.setImageResource(resId);

        LinearLayout tab = new LinearLayout(getContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.MATCH_PARENT, 1);
        tab.setBackgroundResource(R.drawable.bg_btn_selector_white);
        tab.setOrientation(LinearLayout.VERTICAL);
        tab.setLayoutParams(lp);
        tab.setPadding(0, tabPadding, 0, tabPadding);
        tab.setGravity(Gravity.CENTER);
        tab.setFocusable(true);
        tab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mode == MODE_NORMAL) {
                    viewPager.setCurrentItem(position, false);
                } else {
                    if (position < blankIndex) {
                        viewPager.setCurrentItem(position, false);
                    } else {
                        viewPager.setCurrentItem(position - 1, false);
                    }
                }
            }
        });
        tab.addView(tabImg);
        if (title != null && !title.equals("")) {
            TextView tabTxt = new TextView(getContext());
            tabTxt.setSingleLine();
            tabTxt.setText(title);
            tabTxt.setTextColor(titleColor);
            tabTxt.setTextSize(TypedValue.COMPLEX_UNIT_PX, tabTextSize);
            tabTxt.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            tab.addView(tabTxt);
        }

        tabsContainer.addView(tab, position, lp);
    }

    private void addBlankTab(int position) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.MATCH_PARENT, 1);
        LinearLayout tab = new LinearLayout(getContext());
        tab.setOrientation(LinearLayout.VERTICAL);
        tab.setLayoutParams(lp);
        tab.setPadding(0, tabPadding, 0, tabPadding);
        tabsContainer.addView(tab, position, lp);
    }

    private class PagerListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            currentPosition = position;
            notifyDataSetChanged();
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }
}

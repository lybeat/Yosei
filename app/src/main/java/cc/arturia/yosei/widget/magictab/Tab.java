package cc.arturia.yosei.widget.magictab;

import android.support.v4.app.Fragment;

/**
 * Author: Arturia
 * Date: 2016/9/17
 */
public class Tab {

    private Fragment fragment;
    private String title;
    private int normalIconIdDay;
    private int normalIconIdNight;
    private int pressedIconId;

    public Tab() {}

    public Tab(Fragment fragment, int normalIconIdDay, int pressedIconId) {
        this.fragment = fragment;
        this.normalIconIdDay = normalIconIdDay;
        this.normalIconIdNight = normalIconIdDay;
        this.pressedIconId = pressedIconId;
    }

    public Tab(Fragment fragment, String title, int normalIconIdDay, int pressedIconId) {
        this.fragment = fragment;
        this.title = title;
        this.normalIconIdDay = normalIconIdDay;
        this.normalIconIdNight = normalIconIdDay;
        this.pressedIconId = pressedIconId;
    }

    public Tab(Fragment fragment, int normalIconIdNight, int normalIconIdDay, int pressedIconId) {
        this.fragment = fragment;
        this.normalIconIdNight = normalIconIdNight;
        this.normalIconIdDay = normalIconIdDay;
        this.pressedIconId = pressedIconId;
    }

    public Tab(Fragment fragment, String title, int normalIconIdDay,
               int normalIconIdNight, int pressedIconId) {
        this.fragment = fragment;
        this.title = title;
        this.normalIconIdDay = normalIconIdDay;
        this.normalIconIdNight = normalIconIdNight;
        this.pressedIconId = pressedIconId;
    }

    public Fragment getFragment() {
        return fragment;
    }

    public void setFragment(Fragment fragment) {
        this.fragment = fragment;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getNormalIconIdDay() {
        return normalIconIdDay;
    }

    public void setNormalIconIdDay(int normalIconIdDay) {
        this.normalIconIdDay = normalIconIdDay;
    }

    public int getNormalIconIdNight() {
        return normalIconIdNight;
    }

    public void setNormalIconIdNight(int normalIconIdNight) {
        this.normalIconIdNight = normalIconIdNight;
    }

    public int getPressedIconId() {
        return pressedIconId;
    }

    public void setPressedIconId(int pressedIconId) {
        this.pressedIconId = pressedIconId;
    }
}
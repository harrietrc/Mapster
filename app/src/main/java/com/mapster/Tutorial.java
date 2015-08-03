package com.mapster;

import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import tourguide.tourguide.Overlay;
import tourguide.tourguide.Pointer;
import tourguide.tourguide.ToolTip;
import tourguide.tourguide.TourGuide;

/**
 * Created by tommyngo on 2/08/15.
 */
public class Tutorial {
    private TourGuide _tutorialHandler;
    private ToolTip _toolTip;
    private Overlay _overlay;
    private Pointer _pointer;
    public Tutorial(ActionBarActivity activity){
        _tutorialHandler = TourGuide.init(activity);
        _pointer = new Pointer();
    }

    public void setToolTip(String title, String description, int gravity, int backgroundColor){
        _toolTip = new ToolTip()
                .setTitle(title)
                .setDescription(description)
                .setShadow(true)
                .setGravity(gravity)
                .setBackgroundColor(backgroundColor);
    }

    public void setOverlayCircle(){
        _overlay =  new Overlay().setEnterAnimation(getEnterAnimation()).setExitAnimation(getExitAnimation());
    }

    public void setOverlayRectangular(){
        _overlay =  new Overlay().setEnterAnimation(getEnterAnimation()).setExitAnimation(getExitAnimation()).setStyle(Overlay.Style.Rectangle);
    }

    public void cleanUp(){
        _tutorialHandler.cleanUp();
    }

    public TourGuide getTourGuideHandler(){
        return _tutorialHandler;
    }

    public void setTutorialByClick(View view){
        _tutorialHandler = _tutorialHandler.with(TourGuide.Technique.Click)
                                           .setOverlay(_overlay).setPointer(_pointer)
                                           .setToolTip(_toolTip).playOn(view);

    }

    private Animation getEnterAnimation(){
        Animation enterAnimation = new AlphaAnimation(0f, 0.5f);
        enterAnimation.setDuration(600);
        enterAnimation.setFillAfter(true);
        return enterAnimation;
    }

    private Animation getExitAnimation(){
        Animation exitAnimation = new AlphaAnimation(0.5f, 0f);
        exitAnimation.setDuration(600);
        exitAnimation.setFillAfter(true);
        return exitAnimation;
    }

}

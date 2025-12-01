package com.example.docknet.ui;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

public class AnimationHelper {
    private static final int TAG_ANIMATOR = -12345;

    public static void setupImageAnimation(ImageView image) {
        if (image == null) return;
        ObjectAnimator animator = ObjectAnimator.ofFloat(image, "rotation", 0f, 360f);
        animator.setDuration(13000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        image.setTag(TAG_ANIMATOR, animator);
        image.post(animator::start);
    }

    public static void stopImageAnimation(ImageView image) {
        if (image == null) return;
        Object obj = image.getTag(TAG_ANIMATOR);
        if (obj instanceof ObjectAnimator) {
            ObjectAnimator animator = (ObjectAnimator) obj;
            try {
                animator.cancel();
            } catch (Exception ignored) {}
            image.setTag(TAG_ANIMATOR, null);
        }
    }
}

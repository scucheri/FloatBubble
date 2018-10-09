package com.ss.android.floatbubble;

import android.content.Context;

public class FBMeatureUtil {
    private static float pixelsPerOneDp;
    private static boolean inited;

    private static void init(Context context) {
        pixelsPerOneDp = context.getResources().getDisplayMetrics().densityDpi / 160f;
        inited = true;
    }

    public static int pxToDp(Context context,int px) {
        if (inited == false) {
            init(context);
        }
        return Math.round(px / pixelsPerOneDp);
    }

    public static int dpToPx(Context context, int dp) {
        if (inited == false) {
            init(context);
        }
        return Math.round(dp * pixelsPerOneDp);
    }

}

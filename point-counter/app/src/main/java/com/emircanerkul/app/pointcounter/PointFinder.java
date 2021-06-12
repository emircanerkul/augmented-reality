package com.emircanerkul.app.pointcounter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PointFinder extends AsyncTask {

    private List<Point> count;
    private Bitmap bitmap;
    private Integer colorTolerance, grayTolerance;
    private Boolean onDebug;
    private TextView textView;
    private ProgressBar progressBar;
    private ImageView imageView;
    private Context context;

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    public PointFinder(Context context, ImageView imageView, ProgressBar progressBar, TextView textView, Integer colorTolerance, Integer grayTolerance, Boolean onDebug) {
        this.context = context;
        this.bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap().copy(Bitmap.Config.RGB_565, true);
        this.imageView = imageView;
        this.progressBar = progressBar;
        this.textView = textView;
        this.colorTolerance = colorTolerance;
        this.grayTolerance = grayTolerance;
        this.onDebug = onDebug;
    }

    public void setOnDebug(Boolean onDebug) {
        this.onDebug = onDebug;
    }

    private int getMostCommonColor(Bitmap image) {

        Map<Integer, Integer> colorMap = new HashMap<>();
        int height = image.getHeight();
        int width = image.getWidth();

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int rgb = image.getPixel(i, j);
                if (!isGray(getRGBArr(rgb))) {
                    Integer counter = colorMap.get(rgb);
                    if (counter == null) {
                        counter = 0;
                    }

                    colorMap.put(rgb, ++counter);
                }
            }
        }

        List<Map.Entry<Integer, Integer>> list = new LinkedList<>(colorMap.entrySet());

        Collections.sort(list, (Map.Entry<Integer, Integer> obj1, Map.Entry<Integer, Integer> obj2)
                -> ((Comparable) obj1.getValue()).compareTo(obj2.getValue()));

        if (list.size() == 0) return 1;
        Map.Entry<Integer, Integer> entry = list.get(list.size() - 1);
        int[] rgb = getRGBArr(entry.getKey());

        return ((rgb[0] & 0x0ff) << 16) | ((rgb[1] & 0x0ff) << 8) | (rgb[2] & 0x0ff);
    }

    private int[] getRGBArr(int pixel) {
        int red = (pixel >> 16) & 0xff;
        int green = (pixel >> 8) & 0xff;
        int blue = (pixel) & 0xff;

        return new int[]{red, green, blue};
    }

    private boolean isGray(int[] rgbArr) {
        int rgDiff = rgbArr[0] - rgbArr[1];
        int rbDiff = rgbArr[0] - rgbArr[2];

        if (rgDiff > grayTolerance || rgDiff < -grayTolerance) {
            if (rbDiff > grayTolerance || rbDiff < -grayTolerance) {
                return false;
            }
        }
        return true;
    }

    private boolean isEqualT(int a, int b) {
        return Math.abs(a - b) < colorTolerance;
    }

    private void ImageViewAnimatedChange(Context c, final ImageView v, final Bitmap new_image) {
        final Animation anim_out = AnimationUtils.loadAnimation(c, android.R.anim.slide_out_right);
        final Animation anim_in = AnimationUtils.loadAnimation(c, android.R.anim.slide_in_left);
        anim_out.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                v.setImageBitmap(new_image);
                anim_in.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                    }
                });
                v.startAnimation(anim_in);
            }
        });
        v.startAnimation(anim_out);
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        int bW = bitmap.getWidth();
        int bH = bitmap.getHeight();
        int cColor = getMostCommonColor(bitmap);
        count = new ArrayList<>();

        for (int x = 0; x < bW; x++) {
            for (int y = 0; y < bH; y++) {
                int p = bitmap.getPixel(x, y);

                if (isEqualT(Color.red(p), Color.red(cColor)) &&
                        isEqualT(Color.blue(p), Color.blue(cColor)) &&
                        isEqualT(Color.green(p), Color.green(cColor)))
                    bitmap.setPixel(x, y, Color.rgb(0, 0, 0));
                else bitmap.setPixel(x, y, Color.rgb(255, 255, 255));


            }
            publishProgress((((float) (x + 1) / (bW + 1))) * 10);
        }

        for (int y = 0; y < bH; y++) {
            for (int x = 0; x < bW; x++) {
                if (bitmap.getPixel(x, y) == -16777216) {
                    for (int r1 = 0; r1 < 3; r1++) {
                        for (int r2 = 0; r2 < 3; r2++) {
                            if (count.contains(new Point(x - r1, y - r2)))
                                count.remove(count.indexOf(new Point(x - r1, y - r2)));
                            if (count.contains(new Point(x + 1 + r1, y - 1 - r2)))
                                count.remove(count.indexOf(new Point(x + 1 + r1, y - 1 - r2)));
                        }
                    }
                    count.add(new Point(x, y));
                }
            }

            publishProgress((((float) (y + 1) / (bH + 1))) * 90 + 10);
        }

        if (onDebug) {
            Log.e("Total:", String.valueOf(count.size()));
            for (Iterator<Point> iterator = count.iterator(); iterator.hasNext(); ) {
                Point point = iterator.next();

                bitmap.setPixel(point.x, point.y, Color.rgb(255, 0, 0));
            }
        }
        return "";
    }

    @Override
    protected void onPostExecute(Object o) {
        publishProgress(0f);
        ImageViewAnimatedChange(context, imageView, bitmap);
        textView.setText(String.valueOf(count.size()));

    }

    @Override
    protected void onProgressUpdate(Object[] values) {
        progressBar.setProgress((int) ((float) values[0] * 1000000));
    }
}

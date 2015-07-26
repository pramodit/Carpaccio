package com.github.florent37.carpacciocontrollers;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.flaviofaria.kenburnsview.KenBurnsView;
import com.github.florent37.carpacciocontrollers.helper.FastBlurHelper;
import com.github.florent37.carpacciocontrollers.helper.GrayScaleHelper;
import com.github.florent37.carpacciocontrollers.transformation.BlurTransformation;
import com.github.florent37.carpacciocontrollers.transformation.GrayScaleTransformation;
import com.github.florent37.materialimageloading.MaterialImageLoading;
import com.squareup.picasso.Callback;

import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Transformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by florentchampigny on 21/07/15.
 */
public class ImageViewController {

    private static final String TAG = "ImageViewController";

    class ImageViewToAnimateMaterial {
        ImageView imageView;
        int duration;

        public ImageViewToAnimateMaterial(ImageView imageView, int duration) {
            this.imageView = imageView;
            this.duration = duration;
        }
    }


    List<ImageViewToAnimateMaterial> imageViewToAnimateMaterial = new ArrayList<>();
    HashMap<View,List<Transformation>> transformations = new HashMap<>();

    protected void addTransformation(View view,Transformation transformation){
        List<Transformation> list = transformations.get(view);
        if(list == null) {
            list = new ArrayList<>();
            transformations.put(view,list);
        }
        list.add(transformation);
    }

    public void url(final ImageView imageView, String url) {
        RequestCreator requestCreator = Picasso.with(imageView.getContext()).load(url);
        if(transformations.get(imageView) != null) {
            requestCreator = requestCreator.transform(transformations.get(imageView));
            transformations.remove(imageView);
        }
        requestCreator.into(imageView, new Callback() {
            @Override
            public void onSuccess() {
                onImageLoadedFromUrl(imageView);
            }

            @Override
            public void onError() {

            }
        });
    }

    //can be overrided
    protected void onImageLoadedFromUrl(ImageView imageView) {
        startWaitingAnimateMaterialImageView(imageView);
    }

    protected void startWaitingAnimateMaterialImageView(ImageView imageView) {
        ImageViewToAnimateMaterial toRemove = null;
        for (ImageViewToAnimateMaterial toAnimateMaterial : imageViewToAnimateMaterial) {
            if (toAnimateMaterial.imageView.equals(imageView)) {
                toRemove = toAnimateMaterial;
                startAnimateMaterialImageView(toAnimateMaterial.imageView, toAnimateMaterial.duration);
            }
        }
        if (toRemove != null)
            imageViewToAnimateMaterial.remove(toRemove);
    }


    protected void startAnimateMaterialImageView(ImageView imageView, int duration) {
        try {
            MaterialImageLoading.animate(imageView).setDuration(duration).start();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public void animateMaterial(ImageView imageView, String duration) {
        Integer dur = CommonViewController.stringToInt(duration);
        if (dur != null) {
            if (imageView.getDrawable() == null) {
                imageViewToAnimateMaterial.add(new ImageViewToAnimateMaterial(imageView, dur));
            } else {
                startAnimateMaterialImageView(imageView, dur);
            }
        }
    }

    public void kenburns(ImageView imageView) {
        ReplaceViewController replaceViewController = new ReplaceViewController();
        KenBurnsView kenBurnsView = replaceViewController.replace(imageView, "com.flaviofaria.kenburnsview.KenBurnsView");
    }

    public void willBlur(ImageView imageView, String radius){
        addTransformation(imageView,new BlurTransformation(CommonViewController.stringToInt(radius)));
    }

    public void willGrayScale(ImageView imageView){
        addTransformation(imageView,new GrayScaleTransformation());
    }

    public void blur(final ImageView imageView, final String radiusString) {

        imageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    Bitmap newBitmap = FastBlurHelper.getBitmapBlurFromView(imageView, CommonViewController.stringToInt(radiusString));
                    imageView.setImageBitmap(newBitmap);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }, 10);

    }

    public void greyScale(final ImageView imageView, final String radiusString) {

        imageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    Bitmap newBitmap = GrayScaleHelper.getBitmapGreyScaleFromView(imageView);
                    imageView.setImageBitmap(newBitmap);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }, 10);

    }
}

package com.example.carpool_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

public class StartIntroSliderAdapter extends PagerAdapter {

    Context context;
    LayoutInflater layoutInflater;

    public StartIntroSliderAdapter(Context context) {
        this.context = context;
    }

    public int[] slide_images = {
            R.drawable.haereitti,
            R.drawable.detailstesti
    };

    public String[] slide_headings = {
            "Testi1",
            "Testi2"
    };

    public String[] slide_descs = {
            "testihommiahaaa",
            "toinen testihommaaa"
    };

    @Override
    public int getCount() {
        return slide_headings.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == (RelativeLayout) object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.slide_layout, container, false);

        ImageView slideImageView = (ImageView) view.findViewById(R.id.startIntro_slide_image);
        TextView slideHeading = (TextView) view.findViewById(R.id.startIntro_slide_heading);
        TextView slideDescription = (TextView) view.findViewById(R.id.startIntro_slide_desc);

        slideImageView.setImageResource(slide_images[position]);
        slideHeading.setText(slide_headings[position]);
        slideDescription.setText(slide_descs[position]);

        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((RelativeLayout)object);
    }
}

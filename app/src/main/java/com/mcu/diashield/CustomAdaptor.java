package com.mcu.diashield;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomAdaptor extends BaseAdapter {
    RatingBar ratingBar1;
    ArrayList<String> arrayList;
    Context context;
    LayoutInflater inflater;
    TextView textView;
    float[] rating;

    public CustomAdaptor(ArrayList<String> arrayList, Context context, float[] rating) {
        this.arrayList = arrayList;
        this.context = context;
        this.rating = rating;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    public float[] getRating()
    {
        return rating;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View v = inflater.inflate(R.layout.list_lay,null);
        textView = v.findViewById(R.id.textView3);
        ratingBar1 = v.findViewById(R.id.ratingBar2);
        textView.setText(arrayList.get(i));
        ratingBar1.setRating(rating[i]);

        ratingBar1.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                rating[i] = v;
                ratingBar.setRating(v);
                //Output.message(context,"rating "+rating[i]);
            }
        });

        return v;
    }
}

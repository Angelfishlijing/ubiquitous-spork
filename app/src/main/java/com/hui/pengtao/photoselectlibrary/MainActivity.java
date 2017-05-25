package com.hui.pengtao.photoselectlibrary;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.facebook.drawee.view.SimpleDraweeView;
import com.hui.pengtao.photoselectlibrary.activity.ImageSelectorActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    SimpleDraweeView iv ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iv = (SimpleDraweeView)findViewById(R.id.iv_pic_bg) ;
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectPhoto(view) ;
            }
        });
    }

    public void selectPhoto(View v){
        ArrayList<String> images = new ArrayList<>();
        ImageSelectorActivity.start(this, 5, ImageSelectorActivity.MODE_MULTIPLE, true, false, false, images);

    }
}

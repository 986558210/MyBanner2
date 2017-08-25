package com.example.mybanner2;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.banner.Banner;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Banner mBanner;//轮播图
    private List<String> mImages=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBanner = (Banner) findViewById(R.id.banner);
        mImages.add("http://fdfs.xmcdn.com/group26/M00/92/06/wKgJWFkVdbCSfGEaAALFo21GXxo938_android_large.jpg");
        mImages.add("http://fdfs.xmcdn.com/group27/M01/8E/80/wKgJW1kVePLzosGjAAIg7qid8nw914_android_large.jpg");
        mImages.add("http://fdfs.xmcdn.com/group26/M00/91/C9/wKgJRlkVf6Ow3z_EAAI7VV-UZew523_android_large.jpg");
        mImages.add("http://fdfs.xmcdn.com/group26/M06/91/62/wKgJRlkVeS6z6RWDAAHb0lueHgY111_android_large.jpg");
        mImages.add("http://fdfs.xmcdn.com/group27/M06/67/00/wKgJW1kSuNThZLySAARxLtV2RkI156_android_large.jpg");
        mImages.add("http://fdfs.xmcdn.com/group27/M09/CD/9D/wKgJR1kH5j3wIeceAAJme6hHjsg639_android_large.jpg");
        mImages.add("http://fdfs.xmcdn.com/group23/M05/B6/BA/wKgJNFiAc7Hhvbl1AAW3aA1GUqU096_android_large.jpg");
        mBanner.setImageList(mImages, null);
    }
}

package com.example.banner;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Scroller;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.banner.listener.OnBannerClickListener;
import com.example.banner.listener.OnLoadImageListener;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Banner extends FrameLayout implements ViewPager.OnPageChangeListener {

    private int mIndicatorWidth;
    private int mIndicatorHeight;
    private int mIndicatorMargin;
    private int mDelayTime;
    private int mDuration;
    private boolean isAutoPlay;
    private int mIndicatorSelectedResId;
    private int mIndicatorUnselectedResId;
    private int defaultImage = -1;
    private int count = 0;
    private int currentItem;
    private int gravity = -1;
    private int scaleType = 0;
    private List<ImageView> imageViews;//图片集合
    private Context context;
    private ViewPager viewPager;
    private LinearLayout indicator_bottom, indicator_top;
    private Handler handler = new Handler();
    private OnLoadImageListener imageListener;
    private BannerPagerAdapter adapter;
    private ViewPager.OnPageChangeListener mOnPageChangeListener;
    private OnBannerClickListener listener;
    private ImageView imageView_sel;
    private LinearLayout.LayoutParams mLayoutParams;

    public Banner(Context context) {
        this(context, null);
    }

    public Banner(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Banner(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        imageViews = new ArrayList<>();
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        View view = LayoutInflater.from(context).inflate(R.layout.banner, this, true);
        viewPager = (ViewPager) view.findViewById(R.id.viewpager);

        //指示器的底层点的父布局
        indicator_bottom = (LinearLayout) view.findViewById(R.id.indicator_bottom);
        //上层滑动的点的父布局
        indicator_top = (LinearLayout) view.findViewById(R.id.indicator_top);

        handleTypedArray(context, attrs);
        initViewPagerScroll();
    }

    private void handleTypedArray(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.Banner);
        mIndicatorWidth = typedArray.getDimensionPixelSize(R.styleable.Banner_indicator_width, 7);
        mIndicatorHeight = typedArray.getDimensionPixelSize(R.styleable.Banner_indicator_height, 7);
        mIndicatorMargin = typedArray.getDimensionPixelSize(R.styleable.Banner_indicator_margin, 5);
        mIndicatorSelectedResId = typedArray.getResourceId(R.styleable.Banner_indicator_drawable_selected, R.drawable.gray_radius);
        mIndicatorUnselectedResId = typedArray.getResourceId(R.styleable.Banner_indicator_drawable_unselected, R.drawable.white_radius);
        scaleType = typedArray.getInt(R.styleable.Banner_image_scale_type, 0);
        defaultImage = typedArray.getResourceId(R.styleable.Banner_default_image, -1);
        mDelayTime = typedArray.getInt(R.styleable.Banner_delay_time, 2000);
        mDuration = typedArray.getInt(R.styleable.Banner_duration, 700);
        isAutoPlay = typedArray.getBoolean(R.styleable.Banner_is_auto_play, true);
        typedArray.recycle();
    }

    /**
     * 为VP添加滚动器，为了控制切换的时间
     */
    private void initViewPagerScroll() {
        try {
            Field mField = ViewPager.class.getDeclaredField("mScroller");
            mField.setAccessible(true);
            ViewPagerScroller mScroller = new ViewPagerScroller(viewPager.getContext());
            mField.set(viewPager, mScroller);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加图片的方法
     *
     * @param imagesUrl     图片的地址集合
     * @param imageListener
     */
    public void setImageList(List<?> imagesUrl, OnLoadImageListener imageListener) {
        if (imagesUrl == null || imagesUrl.size() <= 0) {
            return;
        }
        count = imagesUrl.size();
        imageViews.clear();
        createIndicator();
        for (int i = 1; i <= count; i++) {
            ImageView iv = new ImageView(context);
            if (scaleType == 0)
                iv.setScaleType(ScaleType.FIT_XY);
            else
                iv.setScaleType(ScaleType.CENTER_CROP);
            Object url;
            if (i == 0) {
                url = imagesUrl.get(count - 1);
            } else if (i == count + 1) {
                url = imagesUrl.get(0);
            } else {
                url = imagesUrl.get(i - 1);
            }
            imageViews.add(iv);
            if (imageListener != null) {
                imageListener.OnLoadImage(iv, url);
            } else {
                if (defaultImage != -1)
                    Glide.with(context)
                            .load(url)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .crossFade().placeholder(defaultImage)
                            .error(defaultImage)
                            .into(iv);
                else
                    Glide.with(context)
                            .load(url)
                            .crossFade()
                            .into(iv);
            }
        }
        setData();
    }

    private void setData() {
        currentItem = Integer.MAX_VALUE / 2 - Integer.MAX_VALUE / 2 % count;
        if (adapter == null) {
            adapter = new BannerPagerAdapter();
            viewPager.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
        viewPager.setFocusable(true);
        viewPager.setCurrentItem(currentItem);
        viewPager.addOnPageChangeListener(this);
        if (gravity != -1) {
            indicator_bottom.setGravity(gravity);
            indicator_top.setGravity(gravity);
        }
        if (isAutoPlay)
            startAutoPlay();
    }

    private void createIndicator() {

        //创建底部的点
        indicator_bottom.removeAllViews();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mIndicatorWidth, mIndicatorHeight);
        params.leftMargin = mIndicatorMargin;
        params.rightMargin = mIndicatorMargin;
        for (int i = 0; i < count; i++) {
            ImageView imageView = new ImageView(context);
            imageView.setScaleType(ScaleType.CENTER_CROP);
            imageView.setImageResource(mIndicatorUnselectedResId);
            indicator_bottom.addView(imageView, params);
        }

        //创建选中的点
        indicator_top.removeAllViews();
        imageView_sel = new ImageView(context);
        imageView_sel.setScaleType(ScaleType.CENTER_CROP);
        imageView_sel.setImageResource(mIndicatorSelectedResId);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(count * mIndicatorWidth + (count - 1) * 2 * mIndicatorMargin, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        indicator_top.setLayoutParams(layoutParams);
        indicator_top.addView(imageView_sel, new LinearLayout.LayoutParams(mIndicatorWidth, mIndicatorHeight));
    }

    private void startAutoPlay() {
        handler.removeCallbacks(task);
        handler.postDelayed(task, mDelayTime);
    }

    public void stopAutoPlay() {
        handler.removeCallbacks(task);
    }

    private final Runnable task = new Runnable() {
        @Override
        public void run() {
            if (count > 1 && isAutoPlay) {
                currentItem += 1;
                viewPager.setCurrentItem(currentItem);
                handler.postDelayed(task, mDelayTime);
            }
        }
    };

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (isAutoPlay) {
            int action = ev.getAction();
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_OUTSIDE) {
                startAutoPlay();
            } else if (action == MotionEvent.ACTION_DOWN) {
                stopAutoPlay();
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    class BannerPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            container.addView(imageViews.get(position % count));
            ImageView view = imageViews.get(position % count);
            if (listener != null) {
                view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.OnBannerClick(position);
                    }
                });
            }
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(imageViews.get(position % count));
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        currentItem = viewPager.getCurrentItem();
        if (mOnPageChangeListener != null) {
            mOnPageChangeListener.onPageScrollStateChanged(state);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (mOnPageChangeListener != null) {
            mOnPageChangeListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
        }

        //俩点之间的距离
        int dis = (int) (indicator_bottom.getChildAt(1).getX() - indicator_bottom.getChildAt(0).getX());
        mLayoutParams = (LinearLayout.LayoutParams) imageView_sel.getLayoutParams();
        if(! (position % count == count - 1)) {
            mLayoutParams.leftMargin = Math.round(dis * (position % count + positionOffset));
        }else if(positionOffset<=0.5){
            mLayoutParams.leftMargin = Math.round(dis * (position % count));
        }else {
            mLayoutParams.leftMargin = Math.round(0);
        }
        imageView_sel.setLayoutParams(mLayoutParams);
    }

    @Override
    public void onPageSelected(int position) {
        if (mOnPageChangeListener != null) {
            mOnPageChangeListener.onPageSelected(position);
        }
    }

    public void setOnBannerClickListener(OnBannerClickListener listener) {
        this.listener = listener;
    }

    class ViewPagerScroller extends Scroller {

        public ViewPagerScroller(Context context) {
            super(context);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            super.startScroll(startX, startY, dx, dy, mDuration);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy) {
            super.startScroll(startX, startY, dx, dy, mDuration);
        }
    }
}

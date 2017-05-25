package com.hui.pengtao.photoselectlibrary.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ListView;
import android.widget.PopupWindow;


import com.hui.pengtao.photoselectlibrary.R;
import com.hui.pengtao.photoselectlibrary.adapter.ImageFolderAdapter;
import com.hui.pengtao.photoselectlibrary.model.LocalMediaFolder;
import com.hui.pengtao.photoselectlibrary.util.ScreenUtils;

import java.lang.reflect.Method;
import java.util.List;


/**
 * Created by firefox on
 * 2017/05/16
 */
public class FolderWindow extends PopupWindow {


    private Context context;
    private View window;
    private ListView mListView;
    private ImageFolderAdapter adapter;

    private boolean isDismiss = false;


    public FolderWindow(Context context) {
        this.context = context;
        window = LayoutInflater.from(context).inflate(R.layout.window_folder, null);
        this.setContentView(window);
        this.setWidth(ScreenUtils.getScreenWidth(context));
        this.setHeight(ScreenUtils.getScreenHeight(context) - ScreenUtils.dip2px(context, 96));
        setPopupWindowTouchModal(this, false);
        this.setAnimationStyle(R.style.WindowStyle);
        this.setFocusable(true);
        this.setOutsideTouchable(true);
        this.update();
        this.setBackgroundDrawable(new ColorDrawable(Color.argb(153, 0, 0, 0)));

        initView();
        registerListener();
    }

    public void initView() {
        adapter = new ImageFolderAdapter(context);

        mListView = (ListView) window.findViewById(R.id.folder_list);
//        mListView.addItemDecoration(new ItemDivider());
//        mListView.setLayoutManager(new LinearLayoutManager(context));
        mListView.setAdapter(adapter);
    }

    public void registerListener() {

    }
    public void bindFolder(List<LocalMediaFolder> folders){
        adapter.bindFolder(folders);
    }
    @Override
    public void showAsDropDown(View anchor) {
        super.showAsDropDown(anchor);
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.up_in);
        mListView.startAnimation(animation);
    }
    public void setOnItemClickListener(ImageFolderAdapter.OnItemClickListener onItemClickListener){
        adapter.setOnItemClickListener(onItemClickListener);
    }
    @Override
    public void dismiss() {
        if(isDismiss){
            return;
        }
        isDismiss = true;
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.down_out);
        mListView.startAnimation(animation);
        dismiss();
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isDismiss = false;
                FolderWindow.super.dismiss();
            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public static void setPopupWindowTouchModal(PopupWindow popupWindow, boolean touchModal) {
        if (null == popupWindow) {
            return;
        }
        Method method;
        try {
            method = PopupWindow.class.getDeclaredMethod("setTouchModal",boolean.class);
            method.setAccessible(true);
            method.invoke(popupWindow, touchModal);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//    public class ItemDivider extends ListView.ItemDecoration {
//        private Drawable mDrawable;
//        public ItemDivider() {
//            mDrawable = context.getResources().getDrawable(R.drawable.item_divider);
//        }
//        @Override
//        public void onDrawOver(Canvas c, ListView parent) {
//            final int left = ScreenUtils.dip2px(parent.getContext(),16);
//            final int right = parent.getWidth() - left;
//
//            final int childCount = parent.getChildCount();
//            for (int i = 0; i < childCount - 1; i++) {
//                final View child = parent.getChildAt(i);
//                final ListView.LayoutParams params = (ListView.LayoutParams) child.getLayoutParams();
//                final int top = child.getBottom() + params.bottomMargin;
//                final int bottom = top + mDrawable.getIntrinsicHeight();
//                mDrawable.setBounds(left, top, right, bottom);
//                mDrawable.draw(c);
//            }
//        }
//
//        @Override
//        public void getItemOffsets(Rect outRect, int position, RecyclerView parent) {
//            outRect.set(0, 0, 0, mDrawable.getIntrinsicWidth());
//        }
//
//    }

}

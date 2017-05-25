package com.hui.pengtao.photoselectlibrary.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.view.SimpleDraweeView;
import com.hui.pengtao.photoselectlibrary.R;
import com.hui.pengtao.photoselectlibrary.activity.ImagePreviewActivity;
import com.hui.pengtao.photoselectlibrary.photodraweeview.Attacher;
import com.hui.pengtao.photoselectlibrary.photodraweeview.OnViewTapListener;


/**
 * Created by dee on 15/11/25.
 */
public class ImagePreviewFragment extends Fragment {

	public static final String PATH = "path";

	public static ImagePreviewFragment getInstance(String path) {
		ImagePreviewFragment fragment = new ImagePreviewFragment();
		Bundle bundle = new Bundle();
		bundle.putString(PATH, path);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View contentView = inflater.inflate(R.layout.fragment_image_preview, container, false);
		final SimpleDraweeView imageView = (SimpleDraweeView) contentView.findViewById(R.id.preview_image);
		final Attacher mAttacher = new Attacher(imageView);
		Uri uri = Uri.parse("file://" + getArguments().getString(PATH));

		imageView.setImageURI(uri);
		mAttacher.setOnViewTapListener(new OnViewTapListener() {
			@Override
			public void onViewTap(View view, float x, float y) {
				ImagePreviewActivity activity = (ImagePreviewActivity) getActivity();
//                activity.switchBarVisibility();
			}
		});
		return contentView;
	}
}

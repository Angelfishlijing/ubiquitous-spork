package com.hui.pengtao.photoselectlibrary.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.hui.pengtao.photoselectlibrary.R;
import com.hui.pengtao.photoselectlibrary.model.LocalMedia;
import com.hui.pengtao.photoselectlibrary.model.LocalMediaFolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by firefox on 2017/05/16.
 */
public class ImageFolderAdapter extends BaseAdapter {
    private Context context;
    private List<LocalMediaFolder> folders = new ArrayList<>();
    private int checkedIndex = 0;

    private OnItemClickListener onItemClickListener;

    public ImageFolderAdapter(Context context) {
        this.context = context;
    }

    public void bindFolder(List<LocalMediaFolder> folders) {
        this.folders = folders;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return folders == null ? 0 : folders.size();
    }

    @Override
    public Object getItem(int position) {
        return folders.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder mHoler = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_folder, null);
            mHoler = new ViewHolder();
            mHoler.firstImage = (SimpleDraweeView) convertView.findViewById(R.id.first_image);
            mHoler.folderName = (TextView) convertView.findViewById(R.id.folder_name);
            mHoler.imageNum = (TextView) convertView.findViewById(R.id.image_num);
            mHoler.isSelected = (ImageView) convertView.findViewById(R.id.is_selected);
            convertView.setTag(mHoler);
        } else {
            mHoler = (ViewHolder) convertView.getTag();
        }
        final LocalMediaFolder folder = folders.get(position);
        Uri uri = Uri.parse("file://" + folder.getFirstImagePath());
        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri).setResizeOptions(new ResizeOptions(200, 200)).build();
        PipelineDraweeController controller = (PipelineDraweeController) Fresco.newDraweeControllerBuilder()
                .setImageRequest(request).build();
        mHoler.firstImage.setController(controller);
        mHoler.folderName.setText(folder.getName());
        mHoler.imageNum.setText(context.getString(R.string.num_postfix, "" + folder.getImageNum()));

        mHoler.isSelected.setVisibility(checkedIndex == position ? View.VISIBLE : View.GONE);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    checkedIndex = position;
                    notifyDataSetChanged();
                    onItemClickListener.onItemClick(folder);
                }
            }
        });


        return convertView;
    }

    static class ViewHolder {
        SimpleDraweeView firstImage;
        TextView folderName;
        TextView imageNum;
        ImageView isSelected;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(LocalMediaFolder folder);
    }
}

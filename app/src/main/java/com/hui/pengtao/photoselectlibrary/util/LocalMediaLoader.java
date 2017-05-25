package com.hui.pengtao.photoselectlibrary.util;

import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;


import com.hui.pengtao.photoselectlibrary.R;
import com.hui.pengtao.photoselectlibrary.model.LocalMedia;
import com.hui.pengtao.photoselectlibrary.model.LocalMediaFolder;


import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;


/**
 * Created by dee on 15/11/19.
 */
public class LocalMediaLoader {
    // load type
    public static final int TYPE_IMAGE = 1;
    public static final int TYPE_VIDEO = 2;
    public static final int PAGE_NUM = 200;

    private final static String[] IMAGE_PROJECTION = {
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.BUCKET_ID};

    private final static String[] VIDEO_PROJECTION = {
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media._ID,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Video.Media.DURATION};

    private int type = TYPE_IMAGE;
    private FragmentActivity activity;

    private LocalMediaFolder currentMediaFolder ;

    public LocalMediaLoader(FragmentActivity activity, int type) {
        this.activity = activity;
        this.type = type;
    }

    private String currentBucketId;
    
    public LocalMediaFolder getCurrentMediaFolder() {
        return currentMediaFolder;
    }

    public void setCurrentMediaFolder(LocalMediaFolder currentMediaFolder) {
        this.currentMediaFolder = currentMediaFolder;
    }

    public void loadAllFolder(final LocalMediaLoadListener imageLoadListener) {
        Observable.just("")
                .map(new Func1<String, List<LocalMediaFolder>>() {

                    @Override
                    public List<LocalMediaFolder> call(String s) {
                        List<LocalMediaFolder> folders = new ArrayList<LocalMediaFolder>();

                        String[] columns = {MediaStore.Images.Media._ID, MediaStore.Images.Thumbnails.DATA, MediaStore.Images.Media.DATA, MediaStore.Images.Media.BUCKET_ID,
                                MediaStore.Images.Media.BUCKET_DISPLAY_NAME, "COUNT(1) AS count"};
                        String selection = MediaStore.Images.Media.MIME_TYPE + "=? or "
                                + MediaStore.Images.Media.MIME_TYPE + "=? and 0==0) GROUP BY (" + MediaStore.Images.Media.BUCKET_ID;
                        String sortOrder = MediaStore.Images.Media.DATE_ADDED +" DESC ";
                        Cursor cursor = null;
                        try {
                            cursor = activity.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, selection,
                                    new String[]{"image/jpeg", "image/png"}, sortOrder);
                            if (cursor != null && cursor.moveToFirst()) {

                                int columnPath = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                                int columnId = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID);

                                int columnFileName = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
                                int columnCount = cursor.getColumnIndex("count");

                                do {
                                    String pathStr = cursor.getString(columnPath);
                                    LocalMediaFolder folderBean = new LocalMediaFolder();
                                    folderBean.setFirstImagePath(pathStr);
                                    folderBean.setName(cursor.getString(columnFileName));
                                    folderBean.setImageNum(cursor.getInt(columnCount));
                                    String bucketName = cursor.getString(columnFileName);
                                    folderBean.setPath(cursor.getString(columnId));

                                    if (!Environment.getExternalStorageDirectory().getPath().contains(bucketName)) {
                                        folders.add(folderBean);
                                    }
                                } while (cursor.moveToNext());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            if (cursor != null)
                                cursor.close();
                        }

                        return folders;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<LocalMediaFolder>>() {
                    @Override
                    public void call(List<LocalMediaFolder> folders) {
                        LocalMediaFolder totalFolder = new LocalMediaFolder();
                        totalFolder.setName(activity.getString(R.string.all_image));
                        if (folders == null) {
                            folders = new ArrayList<LocalMediaFolder>();
                            totalFolder.setImageNum(0);
                        }else{
                            int numTotal = 0;
                            for (LocalMediaFolder folder : folders) {
                                numTotal += folder.getImageNum();
                            }
                            totalFolder.setImageNum(numTotal);
                            if(!folders.isEmpty() && folders.get(0)!= null)
                            totalFolder.setFirstImagePath(folders.get(0).getFirstImagePath());
                        }
                        totalFolder.setPath("");
                        folders.add(0, totalFolder);
                        setCurrentMediaFolder(totalFolder);
                        imageLoadListener.loadCompleteFolders(folders);
                    }
                });
    }




    public void loadImageByPage(final LocalMediaLoadListener imageLoadListener, final int page){
        loadImageByPage(imageLoadListener,page,currentBucketId);
    }

    public void loadImageByPage(final LocalMediaLoadListener imageLoadListener, final int page, String bucketId) {
        this.currentBucketId = bucketId;

        Observable.just(bucketId)
                .flatMap(new Func1<String, Observable<ArrayList<LocalMedia>>>() {
                    @Override
                    public Observable<ArrayList<LocalMedia>> call(String value) {
                        String selection = (TextUtils.isEmpty(value) ? "" : MediaStore.Images.Media.BUCKET_ID + " = ? and ") + "(" + MediaStore.Images.Media.MIME_TYPE + "=? or "
                                + MediaStore.Images.Media.MIME_TYPE + "=? )";
                        String args[] = TextUtils.isEmpty(value) ? new String[]{"image/jpeg", "image/png"} : new String[]{value, "image/jpeg", "image/png"};

                        ArrayList<LocalMedia> allImages = new ArrayList<LocalMedia>();


                        Cursor cursor = null;
                        try {
                            cursor = activity.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                    IMAGE_PROJECTION, selection, args
                                    , IMAGE_PROJECTION[2] + " DESC LIMIT " + PAGE_NUM + " offset " + page * PAGE_NUM);
                            if (cursor != null && cursor.moveToFirst()) {

                                int columnPath = cursor.getColumnIndexOrThrow(IMAGE_PROJECTION[0]);
                                int columnId = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID);
                                int columnTime = cursor.getColumnIndexOrThrow(IMAGE_PROJECTION[2]);

                                do {
                                    String path = cursor.getString(columnPath);
                                    // 如原图路径不存在或者路径存在但文件不存在,就结束当前循环
                                    if (TextUtils.isEmpty(path) || !new File(path).exists()) {
                                        continue;
                                    }
                                    if (isBadImage(path)) {
                                        continue;
                                    }

                                    long dateTime = cursor.getLong(columnTime);
                                    int duration = (type == TYPE_VIDEO ? cursor.getInt(cursor.getColumnIndexOrThrow(VIDEO_PROJECTION[5])) : 0);

                                    LocalMedia image = new LocalMedia(path, dateTime, duration);
//                                    Log.e("hpt", cursor.getCount() + " -         " + value + " -------------------------- " +
//                                            cursor.getString(columnId));
//                                    LocalMediaFolder folder = getImageFolder(path, imageFolders);
//                                    Log.i("FolderName", folder.getName());

//                                    folder.getImages().add(image);
//                                    folder.setImageNum(folder.getImageNum() + 1);
//

                                    allImages.add(image);
//                                    allImageFolder.setImageNum(allImageFolder.getImageNum() + 1);
                                } while (cursor.moveToNext());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            if (cursor != null)
                                cursor.close();
                        }

                        return Observable.just(allImages);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ArrayList<LocalMedia>>() {

                    @Override
                    public void call(ArrayList<LocalMedia> localMedias) {
                        imageLoadListener.loadComplete(localMedias);
                    }
                });
    }

    private boolean isBadImage(String path) {
        try {
            BitmapFactory.Options options = null;
            if (options == null) options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            BitmapFactory.decodeFile(path, options); //filePath代表图片路径
            if (options.mCancel || options.outWidth == -1
                    || options.outHeight == -1) {
                //表示图片已损毁
                return true;
            }

        } catch (Exception e) {
            return true;
        }

        return false;
    }

    private void sortFolder(List<LocalMediaFolder> imageFolders) {
        // 文件夹按图片数量排序
        Collections.sort(imageFolders, new Comparator<LocalMediaFolder>() {
            @Override
            public int compare(LocalMediaFolder lhs, LocalMediaFolder rhs) {
                if (lhs.getImages() == null || rhs.getImages() == null) {
                    return 0;
                }
                int lsize = lhs.getImageNum();
                int rsize = rhs.getImageNum();
                return lsize == rsize ? 0 : (lsize < rsize ? 1 : -1);
            }
        });
    }

    private LocalMediaFolder getImageFolder(String path, List<LocalMediaFolder> imageFolders) {
        File imageFile = new File(path);
        File folderFile = imageFile.getParentFile();

        for (LocalMediaFolder folder : imageFolders) {
            if (folder.getName().equals(folderFile.getName())) {
                return folder;
            }
        }
        LocalMediaFolder newFolder = new LocalMediaFolder();
        newFolder.setName(folderFile.getName());
        newFolder.setPath(folderFile.getAbsolutePath());
        newFolder.setFirstImagePath(path);
        imageFolders.add(newFolder);
        return newFolder;
    }

    public interface LocalMediaLoadListener {
        void loadComplete(List<LocalMedia> images);

        void loadCompleteFolders(List<LocalMediaFolder> folders);
    }

}

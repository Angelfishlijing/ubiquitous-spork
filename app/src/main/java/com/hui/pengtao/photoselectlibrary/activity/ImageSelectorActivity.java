package com.hui.pengtao.photoselectlibrary.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.hui.pengtao.photoselectlibrary.R;
import com.hui.pengtao.photoselectlibrary.adapter.ImageFolderAdapter;
import com.hui.pengtao.photoselectlibrary.adapter.ImageListAdapter;
import com.hui.pengtao.photoselectlibrary.model.LocalMedia;
import com.hui.pengtao.photoselectlibrary.model.LocalMediaFolder;
import com.hui.pengtao.photoselectlibrary.util.FileUtils;
import com.hui.pengtao.photoselectlibrary.util.GridSpacingItemDecoration;
import com.hui.pengtao.photoselectlibrary.util.LocalMediaLoader;
import com.hui.pengtao.photoselectlibrary.util.PermissionHelper;
import com.hui.pengtao.photoselectlibrary.util.ScreenUtils;
import com.hui.pengtao.photoselectlibrary.view.FolderWindow;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

/**
 * Created by Angelfishli on 16/7/04.
 */
public class ImageSelectorActivity extends AppCompatActivity {

    public final static int REQUEST_IMAGE = 66;

    public final static int REQUEST_CAMERA = 67;

    public final static String BUNDLE_CAMERA_PATH = "CameraPath";

    public final static String REQUEST_OUTPUT = "outputList";

    public final static String EXTRA_SELECT_MODE = "SelectMode";

    public final static String EXTRA_SHOW_CAMERA = "ShowCamera";

    public final static String EXTRA_ENABLE_PREVIEW = "EnablePreview";

    public final static String EXTRA_ENABLE_CROP = "EnableCrop";

    public final static String EXTRA_MAX_SELECT_NUM = "MaxSelectNum";


    public final static int MODE_MULTIPLE = 1;

    public final static int MODE_SINGLE = 2;

    private int maxSelectNum = 9;

    private int spanCount = 3;

    private int selectMode = MODE_MULTIPLE;

    private boolean showCamera = true;

    private boolean enablePreview = true;

    private boolean enableCrop = false;

    private Toolbar toolbar;

    private TextView doneText;

    private TextView previewText;

    private RecyclerView mGridView;

    private ImageListAdapter imageAdapter;

    private LinearLayout folderLayout;

    private TextView folderName;

    private FolderWindow folderWindow;

    private String cameraPath;

    ArrayList<String> images = new ArrayList<>();

    private static final String INTENT_EXTER_LAST_SELECTED_IMAGES = "LastSelectedImagess";

    private ArrayList<String> mLastSelectedImages;

    private List<LocalMedia> selectedImages = new ArrayList<LocalMedia>();

    //权限管理
    private boolean isRequireCheck = true;
    private PermissionHelper mHelper;
    //是否是第一次打开
    private boolean isFirst = true;

    private SmoothProgressBar mProgressBar;
    private IMideaLoadListener mideaLoadListener;


    public static void start(Activity activity, int maxSelectNum, int mode, boolean isShow, boolean enablePreview, boolean enableCrop, ArrayList<String> lastSelectedImages) {
        Intent intent = new Intent(activity, ImageSelectorActivity.class);
        intent.putExtra(EXTRA_MAX_SELECT_NUM, maxSelectNum);
        intent.putExtra(EXTRA_SELECT_MODE, mode);
        intent.putExtra(EXTRA_SHOW_CAMERA, isShow);
        intent.putExtra(EXTRA_ENABLE_PREVIEW, enablePreview);
        intent.putExtra(EXTRA_ENABLE_CROP, enableCrop);
        intent.putStringArrayListExtra(INTENT_EXTER_LAST_SELECTED_IMAGES, lastSelectedImages);
        activity.startActivityForResult(intent, REQUEST_IMAGE);
    }

    public static void start(Activity activity, int maxSelectNum, int mode, boolean isShow, boolean enablePreview, boolean enableCrop, ArrayList<String> lastSelectedImages, int requestCode) {
        Intent intent = new Intent(activity, ImageSelectorActivity.class);
        intent.putExtra(EXTRA_MAX_SELECT_NUM, maxSelectNum);
        intent.putExtra(EXTRA_SELECT_MODE, mode);
        intent.putExtra(EXTRA_SHOW_CAMERA, isShow);
        intent.putExtra(EXTRA_ENABLE_PREVIEW, enablePreview);
        intent.putExtra(EXTRA_ENABLE_CROP, enableCrop);
        intent.putStringArrayListExtra(INTENT_EXTER_LAST_SELECTED_IMAGES, lastSelectedImages);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imageselector);
        maxSelectNum = getIntent().getIntExtra(EXTRA_MAX_SELECT_NUM, 9);
        selectMode = getIntent().getIntExtra(EXTRA_SELECT_MODE, MODE_MULTIPLE);
        showCamera = getIntent().getBooleanExtra(EXTRA_SHOW_CAMERA, true);
        enablePreview = getIntent().getBooleanExtra(EXTRA_ENABLE_PREVIEW, true);
        enableCrop = getIntent().getBooleanExtra(EXTRA_ENABLE_CROP, false);
        mLastSelectedImages = getIntent().getStringArrayListExtra(INTENT_EXTER_LAST_SELECTED_IMAGES);
        if (selectMode == MODE_MULTIPLE) {
            enableCrop = false;
        } else {
            enablePreview = false;
        }
        if (savedInstanceState != null) {
            cameraPath = savedInstanceState.getString(BUNDLE_CAMERA_PATH);
        }
        mHelper = new PermissionHelper(this);
        mideaLoadListener = new IMideaLoadListener();
        initView();
        registerListener();

    }

    private LocalMediaLoader loader;


    public void loadImage(int currentPage) {
        loader = new LocalMediaLoader(this, LocalMediaLoader.TYPE_IMAGE);
        loadImageWithPageAndPath(currentPage, null);
        loader.loadAllFolder(mideaLoadListener);
    }

    public void loadImageWithPageAndPath(final int currentPage) {
        loader.loadImageByPage(mideaLoadListener, currentPage);
    }

    public void loadImageWithPageAndPath(final int currentPage, String path) {
        loader.loadImageByPage(mideaLoadListener, currentPage, path);

    }

    public class IMideaLoadListener implements LocalMediaLoader.LocalMediaLoadListener {


        @Override
        public void loadComplete(List<LocalMedia> folders) {
            mProgressBar.setVisibility(View.GONE);
//                folderWindow.bindFolder(folders);
//                List<LocalMedia> currentImages = folders.get(0).getImages();
            if (currentPage == 0)
                imageAdapter.clear();
            imageAdapter.bindImages(folders);
            if (mLastSelectedImages == null || mLastSelectedImages.isEmpty() || folders == null) {
                return;
            } else {
                Iterator<String> iterator = mLastSelectedImages.iterator();
                while (iterator.hasNext()) {
                    LocalMedia img = new LocalMedia(iterator.next());
                    if (mLastSelectedImages.contains(img.getPath())) {
                        selectedImages.add(img);
                    }
                }
            }
            imageAdapter.bindSelectImages(selectedImages);
        }

        @Override
        public void loadCompleteFolders(List<LocalMediaFolder> folders) {

            folderWindow.bindFolder(folders);
        }
    }

    /**
     * 用户权限处理,
     * 如果全部获取, 则直接过.
     * 如果权限缺失, 则提示Dialog.
     *
     * @param requestCode  请求码
     * @param permissions  权限
     * @param grantResults 结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PermissionHelper.PERMISSION_REQUEST_CAMERA_CODE && mHelper.isPermissionGrant(grantResults)) {
            isRequireCheck = true;
            currentPage = 0;

            loadImage(currentPage);
        } else {
            isRequireCheck = false;
            mHelper.startAppSettings();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isRequireCheck) {
            //权限没有授权，进入授权界面
            if (mHelper.checkCameraAndStorageWithActivity()) {

                if (isFirst) {
                    isFirst = false;
                    currentPage = 0;
                    loadImage(currentPage);
                }
            }
        } else {
            isRequireCheck = true;
        }
    }

    public void initView() {
        folderWindow = new FolderWindow(this);
        toolbar = (Toolbar) findViewById(R.id.image_toolbar);
        toolbar.setTitle("");
        toolbar.setNavigationIcon(R.mipmap.ic_back);
        setSupportActionBar(toolbar);
        doneText = (TextView) findViewById(R.id.done_text);
        doneText.setVisibility(selectMode == MODE_MULTIPLE ? View.VISIBLE : View.GONE);
        ((TextView) findViewById(R.id.toolbar_title)).setText(getString(R.string.picture));
        previewText = (TextView) findViewById(R.id.preview_text);
        previewText.setVisibility(enablePreview ? View.VISIBLE : View.GONE);


        folderLayout = (LinearLayout) findViewById(R.id.folder_layout);
        folderName = (TextView) findViewById(R.id.folder_name);

        mGridView = (RecyclerView) findViewById(R.id.folder_list);
        mGridView.setHasFixedSize(true);
        mGridView.addItemDecoration(new GridSpacingItemDecoration(spanCount, ScreenUtils.dip2px(this, 2), false));
        mGridView.setLayoutManager(new GridLayoutManager(this, spanCount));
        mProgressBar = (SmoothProgressBar) findViewById(R.id.progress_wheel);

        imageAdapter = new ImageListAdapter(this, maxSelectNum, selectMode, showCamera, enablePreview);
        mGridView.setAdapter(imageAdapter);

    }

    protected boolean isSlideToBottom(RecyclerView recyclerView) {
        if (recyclerView == null) return false;
        if (!recyclerView.canScrollVertically(1))
            return true;
        return false;
    }

    int currentPage = 0;

    public void registerListener() {
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        folderLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (folderWindow.isShowing()) {
                    folderWindow.dismiss();
                } else {
                    folderWindow.showAsDropDown(toolbar);
                }
            }
        });
        mGridView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                Log.e("hpt", currentPage + " ************ onScrolled ******************** " + (isSlideToBottom(recyclerView) && imageAdapter.getItemCount() >= LocalMediaLoader.PAGE_NUM &&
                        imageAdapter.getItemCount() < loader.getCurrentMediaFolder().getImageNum()));
                if (isSlideToBottom(recyclerView) && currentPage == 0 && imageAdapter.getItemCount() < LocalMediaLoader.PAGE_NUM) {
                    return;
                }
                if (isSlideToBottom(recyclerView) &&
                        imageAdapter.getItemCount() < loader.getCurrentMediaFolder().getImageNum()) {
                    loadImageWithPageAndPath(++currentPage);
                }
            }
        });


        imageAdapter.setOnImageSelectChangedListener(new ImageListAdapter.OnImageSelectChangedListener() {
            @Override
            public void onChange(List<LocalMedia> selectImages) {
                int size = 0;
                boolean enable = selectImages.size() != 0;
                doneText.setEnabled(enable ? true : false);
                previewText.setEnabled(enable ? true : false);
                if (enable) {
                    size = selectImages.size();
                    doneText.setText(getString(R.string.done_num, "" + size, "" + maxSelectNum));
                    previewText.setText(getString(R.string.preview_num, "" + selectImages.size()));
                } else {
                    doneText.setText(R.string.done);
                    previewText.setText(R.string.preview);
                }
            }


            @Override
            public void onTakePhoto() {
                if (mLastSelectedImages != null && !mLastSelectedImages.isEmpty() &&
                        mLastSelectedImages.size() >= maxSelectNum && selectMode == MODE_MULTIPLE) {
                    Toast.makeText(ImageSelectorActivity.this,
                            getString(R.string.message_max_num, "" + maxSelectNum), Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                startCamera();
            }

            @Override
            public void onPictureClick(LocalMedia media, int position) {
                if (enablePreview) {
                    startPreview(imageAdapter.getImages(), position);
                } else if (enableCrop) {
                    startCrop(media.getPath());
                } else {
                    onSelectDone(media.getPath());
                }
            }
        });

        doneText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSelectDone(imageAdapter.getSelectedImages());
            }
        });
        folderWindow.setOnItemClickListener(new ImageFolderAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(LocalMediaFolder folder) {
                folderWindow.dismiss();
//                imageAdapter.bindImages(images);

                if (folderName.getText().toString().equals(folder.getName())) {
                    return;
                }
                loader.setCurrentMediaFolder(folder);
                folderName.setText(folder.getName());
                currentPage = 0;
                Log.e("hpt", "folderWindow.setOnItemClickListene ------------ " + folder.getName().equals(getString(R.string.all_image)));
                if (folder.getName().equals(getString(R.string.all_image)))
                    loadImageWithPageAndPath(currentPage, null);
                else
                    loadImageWithPageAndPath(currentPage, folder.getPath());


            }
        });
        previewText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPreview(imageAdapter.getSelectedImages(), 0);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            // on take photo success
            if (requestCode == REQUEST_CAMERA) {
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(cameraPath))));
                if (enableCrop) {
                    startCrop(cameraPath);
                } else {
                    if (mLastSelectedImages != null && !mLastSelectedImages.isEmpty() && selectMode == MODE_MULTIPLE) {
                        images.addAll(mLastSelectedImages);
                    }
                    onSelectDone(cameraPath);
                }
            }
            //on preview select change
            else if (requestCode == ImagePreviewActivity.REQUEST_PREVIEW) {
                boolean isDone = data.getBooleanExtra(ImagePreviewActivity.OUTPUT_ISDONE, false);
                List<LocalMedia> images = (List<LocalMedia>) data.getSerializableExtra(ImagePreviewActivity.OUTPUT_LIST);
                if (isDone) {
                    onSelectDone(images);
                } else {
                    imageAdapter.bindSelectImages(images);
                }
            }
            // on crop success
            else if (requestCode == ImageCropActivity.REQUEST_CROP) {
                String path = data.getStringExtra(ImageCropActivity.OUTPUT_PATH);
                onSelectDone(path);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(BUNDLE_CAMERA_PATH, cameraPath);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    /**
     * start to camera、preview、crop
     */
    public void startCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            File cameraFile = FileUtils.createCameraFile(this);
            cameraPath = cameraFile.getAbsolutePath();
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cameraFile));
            startActivityForResult(cameraIntent, REQUEST_CAMERA);
        }
    }

    public void startPreview(List<LocalMedia> previewImages, int position) {
        ImagePreviewActivity.startPreview(this, previewImages, imageAdapter.getSelectedImages(), maxSelectNum, position);
    }

    public void startCrop(String path) {
        ImageCropActivity.startCrop(this, path);
    }

    /**
     * on select done
     *
     * @param medias
     */
    public void onSelectDone(List<LocalMedia> medias) {


        for (LocalMedia media : medias) {
            images.add(media.getPath());
        }
        onResult(images);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    public void onSelectDone(String path) {

        images.add(path);
        onResult(images);
    }

    public void onResult(ArrayList<String> images) {
        setResult(RESULT_OK, new Intent().putStringArrayListExtra(REQUEST_OUTPUT, images));
        finish();
    }

    public static Intent buildIntent(Context context) {
        Intent intent = new Intent(context, ImageSelectorActivity.class);
        return intent;
    }

}

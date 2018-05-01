package com.treasure.cropview.ui.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.treasure.cropview.R;
import com.treasure.cropview.callback.CropCallback;
import com.treasure.cropview.ui.CropImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {
    @BindView(R.id.pic_clip_image)
    CropImageView mClipImage;
    @BindView(R.id.loading_layout)
    FrameLayout mLoadingLayout;

    private Uri mSourceUri = null;
    private boolean isFlipY;
    private boolean isFlipX;
    private ImageSaveTask saveTask;

    @Override
    public void loadContentLayout() {
        setContentView(R.layout.activity_pic_clip);
    }

    @Override
    public void initView() {
        mClipImage.setImageResource(R.mipmap.pic_test);
        mSourceUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.mipmap.pic_test);
    }

    @Override
    public void setListener() {

    }

    @OnClick({R.id.pic_clip_next, R.id.pic_clip_rotate, R.id.pic_clip_flip_x,
            R.id.pic_clip_flip_y})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.pic_clip_next:
                if (mSourceUri != null) {
                    checkStoragePermission();
                }
                break;
            case R.id.pic_clip_rotate:
                mClipImage.rotateImage(CropImageView.RotateDegrees.ROTATE_90D);
                break;
            case R.id.pic_clip_flip_y:
                //以y轴翻转  左右
                mClipImage.setScaleX(mClipImage.getScaleX() * -1.0f);
                if (!isFlipY) {
                    isFlipY = true;
                } else {
                    isFlipY = false;
                }
                break;
            case R.id.pic_clip_flip_x:
                mClipImage.setScaleY(mClipImage.getScaleY() * -1.0f);
                if (!isFlipX) {
                    isFlipX = true;
                } else {
                    isFlipX = false;
                }
                break;
        }
    }

    public void cropImage() {
        mLoadingLayout.setVisibility(View.VISIBLE);
        mClipImage.crop(mSourceUri).execute(new CropCallback() {
            @Override
            public void onSuccess(Bitmap cropped) {
                Matrix matrix = new Matrix();
                if (isFlipX && !isFlipY)
                    matrix.setScale(1, -1);
                if (!isFlipX && isFlipY)
                    matrix.setScale(-1, 1);
                if (isFlipX && isFlipY)
                    matrix.setScale(-1, -1);
                cropped = Bitmap.createBitmap(cropped, 0, 0, cropped.getWidth(), cropped.getHeight(), matrix, true);

                saveTask = new ImageSaveTask(cropped);
                saveTask.execute();
            }

            @Override
            public void onError(Throwable e) {
                mLoadingLayout.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "crop failed！", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String saveImage(Bitmap bmp, int quality) {
        if (bmp == null) {
            return null;
        }
        File appDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (appDir == null) {
            return null;
        }
        String fileName = System.currentTimeMillis() + ".png";
        File file = new File(appDir, fileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, quality, fos);
            fos.flush();
            return file.getAbsolutePath();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private void scanFile(Context context, String filePath) {
        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        scanIntent.setData(Uri.fromFile(new File(filePath)));
        context.sendBroadcast(scanIntent);
    }

    //请求存储权限
    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= 21 && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 200);
        } else {
            cropImage();
        }
    }

    //动态权限
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 200) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkStoragePermission();
            } else {
                new AlertDialog.Builder(MainActivity.this)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                MainActivity.this.finish();
                            }
                        })
                        .setMessage("检测到没有存储权限，请开启存储权限后，在使用产品")
                        .setIcon(R.mipmap.ic_logo_t)
                        .create()
                        .show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onDestroy() {
        if (saveTask != null && !saveTask.isCancelled()) {
            saveTask.cancel(true);
            saveTask = null;
        }
        super.onDestroy();
    }

    public class ImageSaveTask extends AsyncTask<Void, Void, Boolean> {
        private Bitmap resultBitmap;
        private String mSavedFile;

        public ImageSaveTask(Bitmap resultBitmap) {
            this.resultBitmap = resultBitmap;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            mSavedFile = saveImage(resultBitmap, 100);
            if (mSavedFile != null) {
                scanFile(MainActivity.this, mSavedFile);
                return true;
            } else {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            mLoadingLayout.setVisibility(View.GONE);
            if (aBoolean) {
                Intent intent = new Intent(MainActivity.this, PicDisposeResultActivity.class);
                intent.putExtra("save_path", mSavedFile);
                startActivity(intent);
            }
        }
    }
}


package com.treasure.cropview.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.treasure.cropview.R;

import butterknife.BindView;
import butterknife.OnClick;

public class PicDisposeResultActivity extends BaseActivity {

    @BindView(R.id.pic_dispose_result_image)
    ImageView resultImg;

    private String save_path;

    @Override
    public void loadContentLayout() {
        setContentView(R.layout.activity_pic_dispose_result);
    }

    @Override
    public void initView() {
        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {
        if (intent != null) {
            save_path = intent.getStringExtra("save_path");
        }
    }

    @Override
    public void setListener() {
        if (!TextUtils.isEmpty(save_path)) {
            Bitmap bitmap = BitmapFactory.decodeFile(save_path);
            resultImg.setImageBitmap(bitmap);
        }
    }


    @OnClick({R.id.back})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.back:
                onBackPressed();
                break;
        }
    }
}

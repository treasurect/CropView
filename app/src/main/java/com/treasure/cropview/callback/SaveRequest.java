package com.treasure.cropview.callback;

import android.graphics.Bitmap;
import android.net.Uri;

import com.treasure.cropview.ui.CropImageView;

public class SaveRequest {

  private CropImageView cropImageView;
  private Bitmap image;
  private Bitmap.CompressFormat compressFormat;
  private int compressQuality = -1;

  public SaveRequest(CropImageView cropImageView, Bitmap image) {
    this.cropImageView = cropImageView;
    this.image = image;
  }

  public SaveRequest compressFormat(Bitmap.CompressFormat compressFormat) {
    this.compressFormat = compressFormat;
    return this;
  }

  public SaveRequest compressQuality(int compressQuality) {
    this.compressQuality = compressQuality;
    return this;
  }

  private void build() {
    if (compressFormat != null) {
      cropImageView.setCompressFormat(compressFormat);
    }
    if (compressQuality >= 0) {
      cropImageView.setCompressQuality(compressQuality);
    }
  }

  public void execute(Uri saveUri, SaveCallback callback) {
    build();
    cropImageView.saveAsync(saveUri, image, callback);
  }
}

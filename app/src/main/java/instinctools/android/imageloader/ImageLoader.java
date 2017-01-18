package instinctools.android.imageloader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Executor;

import instinctools.android.App;
import instinctools.android.cache.BitmapCacheMgr;
import instinctools.android.constans.Constants;
import instinctools.android.executors.ImageTaskExecutor;

public class ImageLoader {
    private static final String TAG = "ImageLoader";

    private static final BitmapCacheMgr mBitmapCacheMgr;

    // Current loading images
    private static final Executor ImageBitmapExecutor = ImageTaskExecutor.create();

    static {
        mBitmapCacheMgr = new BitmapCacheMgr.Builder().enableSDCardCache(Constants.DISK_MAX_CACHE_SIZE, App.getAppContext()).build();
    }

    private ImageLoader() {
    }

    public static ImageTarget what(@NonNull String url) {
        return new ImageTarget(url);
    }

    public static class ImageTarget {
        private String mUrl;
        private WeakReference<ImageView> mImageViewReference;
        private ImagePlaceholder mImagePlaceholder;

        ImageTarget(String url) {
            this.mUrl = url;
        }

        public ImageTarget what(String url) {
            this.mUrl = url;
            return this;
        }

        public ImageTarget error(int drawId) {
            if (mImagePlaceholder == null)
                mImagePlaceholder = new ImagePlaceholder();

            mImagePlaceholder.setErrorId(drawId);
            return this;
        }

        public ImageTarget loading(int drawId) {
            if (mImagePlaceholder == null)
                mImagePlaceholder = new ImagePlaceholder();

            mImagePlaceholder.setLoadingId(drawId);
            return this;
        }

        public ImageTarget in(@NonNull ImageView image) {
            this.mImageViewReference = new WeakReference<>(image);
            return this;
        }

        public void load(@NonNull ImageLoadingStateListener listener) {
            if (TextUtils.isEmpty(mUrl) || mImageViewReference.get() == null)
                return;

            new ImageBitmapWorker(this, listener).executeOnExecutor(ImageBitmapExecutor);
        }

        public void load() {
            new ImageBitmapWorker(this).executeOnExecutor(ImageBitmapExecutor);
        }
    }

    private static class ImageBitmapWorker extends AsyncTask<Void, Void, Bitmap> {
        private final ImageTarget mImageTarget;
        private final ImageLoadingStateListener mListener;

        ImageBitmapWorker(ImageTarget image) {
            this.mImageTarget = image;
            this.mListener = null;
        }

        ImageBitmapWorker(ImageTarget image, ImageLoadingStateListener listener) {
            this.mImageTarget = image;
            this.mListener = listener;
        }

        private Bitmap loadingFromNetwork() {
            URL url;
            Bitmap bitmap;
            try {
                url = new URL(mImageTarget.mUrl);
            } catch (MalformedURLException e) {
                Log.e(TAG, "Fail parse url for loading image", e);
                return null;
            }

            try {
                bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            } catch (IOException e) {
                Log.e(TAG, "Fail open connection for loading image", e);
                return null;
            }

            return bitmap;
        }

        @Override
        protected void onPreExecute() {
            ImagePlaceholder imagePlaceholder = mImageTarget.mImagePlaceholder;
            if (imagePlaceholder == null)
                return;

            if (imagePlaceholder.getLoadingId() != 0) {
                ImageView imageView = mImageTarget.mImageViewReference.get();
                if (imageView != null)
                    imageView.setImageDrawable(ContextCompat.getDrawable(imageView.getContext(), imagePlaceholder.getLoadingId()));
            }

            if (mListener != null)
                mListener.onPrepare();
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            Bitmap bitmap = mBitmapCacheMgr.getFromCache(mImageTarget.mUrl);
            if (bitmap == null)
                bitmap = loadingFromNetwork();

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap == null) {
                ImagePlaceholder imagePlaceholder = mImageTarget.mImagePlaceholder;
                if (imagePlaceholder != null && imagePlaceholder.getErrorId() != 0) {
                    ImageView imageView = mImageTarget.mImageViewReference.get();
                    if (imageView != null)
                        imageView.setImageDrawable(ContextCompat.getDrawable(imageView.getContext(), imagePlaceholder.getErrorId()));
                }

                if (mListener != null)
                    mListener.onError();

                return;
            }

            mBitmapCacheMgr.addToCache(mImageTarget.mUrl, bitmap);

            ImageView imageView = mImageTarget.mImageViewReference.get();
            if (imageView != null)
                imageView.setImageBitmap(bitmap);

            if (mListener != null)
                mListener.onLoaded(bitmap);
        }
    }
}

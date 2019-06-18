package cc.arturia.yosei.util;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * Author: Arturia
 * Date: 2015/12/20
 */
public class PictureUtil {

    /**
     * 从路径中得到一张图片，并缩放到指定的大小
     *
     * @param path
     * @param desWidth
     * @param desHeight
     * @return
     */
    public static Bitmap getScaledBitmapFromPath(String path, int desWidth, int desHeight) {

        // Read in the dimensions of the image on disk
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        float srcWidth = options.outWidth;
        float srcHeight = options.outHeight;

        int inSamplesSize;
        if (srcHeight > desHeight || srcWidth > desWidth) {
            inSamplesSize = Math.round(srcHeight / desHeight);
        } else {
            inSamplesSize = Math.round(srcWidth / desWidth);
        }

        options = new BitmapFactory.Options();
        options.inSampleSize = inSamplesSize;

        return BitmapFactory.decodeFile(path, options);
    }

    /**
     * 从资源文件中获取一张图片，并缩放到指定大小
     *
     * @param context
     * @param resourceId
     * @return
     */
    public static Bitmap getScaledBitmapFromResource(Context context, int resourceId, int desWidth, int desHeight) {
        // Read in the dimensions of the image on disk
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), resourceId, options);

        float srcWidth = options.outWidth;
        float srcHeight = options.outHeight;

        int inSamplesSize;
        if (srcHeight > desHeight || srcWidth > desWidth) {
            inSamplesSize = Math.round(srcHeight / desHeight);
        } else {
            inSamplesSize = Math.round(srcWidth / desWidth);
        }

        options = new BitmapFactory.Options();
        options.inSampleSize = inSamplesSize;

        return BitmapFactory.decodeResource(context.getResources(), resourceId, options);
    }

    public static Bitmap getScaledBitmapFromUri(Context context, Uri uri, int desHeight) {
        InputStream input = null;
        try {
            input = context.getContentResolver().openInputStream(uri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, options);

            float srcHeight = options.outHeight;

            int inSamplesSize = 1;
            if (srcHeight > desHeight) {
                inSamplesSize = Math.round(srcHeight / desHeight);
            }
            options = new BitmapFactory.Options();
            options.inSampleSize = inSamplesSize;

            input = context.getContentResolver().openInputStream(uri);
            return BitmapFactory.decodeStream(input, null, options);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Bitmap getBitmapFromUri(Context context, Uri uri) {
        try {
            return MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param srcBmp
     * @return
     */
    public static Bitmap getRoundBitmap(Bitmap srcBmp) {
        final Paint paint = new Paint();

        int width = srcBmp.getWidth();
        int height = srcBmp.getHeight();
        int left, top, right, bottom;
        if (width <= height) {
            left = 0;
            top = (height - width) / 2;
            right = width;
            bottom = top + width;
        } else {
            left = (width - height) / 2;
            top = 0;
            right = left + height;
            bottom = height;
        }
        final Rect rect = new Rect(left, top, right, bottom);
        final RectF rectF = new RectF(rect);

        Bitmap desBmp = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(desBmp);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);

        int radius = (width > height ? width : height) / 2;
        canvas.drawRoundRect(rectF, radius, radius, paint);

        // 设置当两个图形相交时的模式, SRC_IN为取SRC图形相交的部分, 多余的将被去掉
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(srcBmp, rect, rect, paint);
        srcBmp.recycle();

        return desBmp;
    }

    public static Bitmap getFilletBitmap(Bitmap srcBmp, int radius) {
        final Paint paint = new Paint();

        int width = srcBmp.getWidth();
        int height = srcBmp.getHeight();
        int left, top, right, bottom;
        if (width <= height) {
            left = 0;
            top = (height - width) / 2;
            right = width;
            bottom = top + width;
        } else {
            left = (width - height) / 2;
            top = 0;
            right = left + height;
            bottom = height;
        }
        final Rect rect = new Rect(left, top, right, bottom);
        final RectF rectF = new RectF(rect);

        Bitmap desBmp = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(desBmp);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);

        canvas.drawRoundRect(rectF, radius, radius, paint);

        // 设置当两个图形相交时的模式, SRC_IN为取SRC图形相交的部分, 多余的将被去掉
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(srcBmp, rect, rect, paint);
//        srcBmp.recycle();

        return desBmp;
    }

    /**
     * @param activity
     * @param path
     * @return
     */
    public static Bitmap drawHandBitmap(Activity activity, String path) {

        Bitmap desBmp = BitmapFactory.decodeFile(path);
        desBmp = PictureUtil.getRoundBitmap(desBmp);

        Paint paint = new Paint();
        paint.setColor(0xffffffff);
        paint.setStrokeWidth(10);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);

        int width = desBmp.getWidth();
        int height = desBmp.getHeight();
        float cx = width / 2;
        float cy = height / 2;
        float radius = width > height ? width / 2 - 5 : height / 2 - 5;
        Canvas canvas = new Canvas(desBmp);
        canvas.drawCircle(cx, cy, radius, paint);

        return desBmp;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Config.ARGB_8888 : Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap(width, height, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);

        return bitmap;
    }

    public static byte[] BitmapToBytes(Context context, Bitmap bmp) {
        if (bmp == null)
            return null;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(CompressFormat.PNG, 100, baos);

        return baos.toByteArray();
    }

    public static void savePictureToFile(Context context, Bitmap bmp,
                                         String path, String format) {
        Date date = new Date();
        String name = DateUtil.getChangeDateFormat(date, "yyyyMMddHHmmss");

        try {
            FileOutputStream fos = new FileOutputStream(path + "/" + name
                    + "." + format);
            if (format.equalsIgnoreCase("png")) {
                bmp.compress(CompressFormat.PNG, 0, fos);
            } else if (format.equalsIgnoreCase("jpeg")) {
                bmp.compress(CompressFormat.JPEG, 100, fos);
            } else if (format.equalsIgnoreCase("webp")) {
                bmp.compress(CompressFormat.WEBP, 100, fos);
            }
            fos.close();
        } catch (Exception e) {
            Toast.makeText(context, "保存失败", Toast.LENGTH_SHORT).show();
        }
    }

    public static void savePictureToFile(Context context, Bitmap bmp, File file) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(CompressFormat.JPEG, 100, fos);
            fos.close();
        } catch (Exception e) {
//            Toast.makeText(context, "保存失败", Toast.LENGTH_SHORT).show();
        }
    }

    private static Bitmap createWaterMaskBitmap(Bitmap src, Bitmap watermark,
                                                int paddingLeft, int paddingTop) {
        if (src == null) {
            return null;
        }
        int width = src.getWidth();
        int height = src.getHeight();
        //创建一个bitmap
        Bitmap newb = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        //将该图片作为画布
        Canvas canvas = new Canvas(newb);
        //在画布 0，0坐标上开始绘制原始图片
        canvas.drawBitmap(src, 0, 0, null);
        //在画布上绘制水印图片
        canvas.drawBitmap(watermark, paddingLeft, paddingTop, null);
        // 保存
        canvas.save();
        // 存储
        canvas.restore();
        return newb;
    }

    public static Bitmap createWaterMaskLeftBottom(
            Context context, Bitmap src, Bitmap watermark,
            int paddingLeft, int paddingBottom) {
        return createWaterMaskBitmap(src, watermark, UnitUtil.dp2px(context, paddingLeft),
                src.getHeight() - watermark.getHeight() - UnitUtil.dp2px(context, paddingBottom));
    }

    public static Bitmap drawTextToRightBottom(Context context, Bitmap bitmap, String text,
                                               int size, int color, int paddingRight, int paddingBottom) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setTextSize(UnitUtil.dp2px(context, size));
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        return drawTextToBitmap(context, bitmap, text, paint, bounds,
                bitmap.getWidth() - bounds.width() - UnitUtil.dp2px(context, paddingRight),
                bitmap.getHeight() - UnitUtil.dp2px(context, paddingBottom));
    }

    public static Bitmap drawTextToLeftBottom(Context context, Bitmap bitmap, String text,
                                              int size, int color, int paddingLeft, int paddingBottom) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setTextSize(UnitUtil.dp2px(context, size));
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        return drawTextToBitmap(context, bitmap, text, paint, bounds,
                UnitUtil.dp2px(context, paddingLeft),
                bitmap.getHeight() - UnitUtil.dp2px(context, paddingBottom));
    }

    private static Bitmap drawTextToBitmap(Context context, Bitmap bitmap, String text,
                                           Paint paint, Rect bounds, int paddingLeft, int paddingTop) {
        Config bitmapConfig = bitmap.getConfig();

        paint.setDither(true); // 获取跟清晰的图像采样
        paint.setFilterBitmap(true);// 过滤一些
        if (bitmapConfig == null) {
            bitmapConfig = Config.ARGB_8888;
        }
        bitmap = bitmap.copy(bitmapConfig, true);
        Canvas canvas = new Canvas(bitmap);

        canvas.drawText(text, paddingLeft, paddingTop, paint);
        return bitmap;
    }

    public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    public static String getRealFilePath(final Context context, final Uri uri) {
        if (null == uri)
            return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri,
                    new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;

    }
}

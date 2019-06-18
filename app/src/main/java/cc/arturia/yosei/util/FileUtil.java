package cc.arturia.yosei.util;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Author: lybeat
 * Date: 2016/5/7
 */
public class FileUtil {

    private FileUtil() {
        throw new UnsupportedOperationException("Cannot be instantiated");
    }

    public static File createRoot(String fileName) {
        File root = new File(Environment.getExternalStorageDirectory(), fileName);
        if (!root.exists()) {
            root.mkdirs();
        }
        return root;
    }

    public static File createChild(File root, String fileName) {
        File child = new File(root, fileName);
        if (!child.exists()) {
            child.mkdirs();
        }
        return child;
    }

    public static String getExtensionName(String path) {
        String exName = null;

        if (path != null && path.length() > 0) {
            int start = path.lastIndexOf('.');
            if (start > -1 && start < (path.length() - 1)) {
                exName = path.substring(start + 1);
            }
        }
        return exName;
    }

    public static File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueName);
    }

    public static String getFileSize(File file) {
        if (file == null || !file.exists()) {
            return String.valueOf(0);
        }

        DecimalFormat format = new DecimalFormat(".0");

        double size = file.length();
        if (size >= 1024 * 1024 * 1024) {
            return size / 1024 / 1024 / 1024 + "G";
        } else if (size >= 1024 * 1024) {
            return size / 1024 / 1024 + "M";
        } else {
            return format.format(size / 1024) + "KB";
        }
    }

    public static String getFileSize(long size) {
        DecimalFormat format = new DecimalFormat(".0");

        if (size >= 1024 * 1024 * 1024) {
            return size / 1024 / 1024 / 1024 + "G";
        } else if (size >= 1024 * 1024) {
            return size / 1024 / 1024 + "M";
        } else {
            return format.format(size / 1024) + "KB";
        }
    }

    public static void deleteFolderFile(final File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            return;
        }
        for (File file : dir.listFiles()) {
            if (file.isFile())
                file.delete();
            else if (file.isDirectory())
                deleteFolderFile(file);
        }
    }

    public static void deleteFileByPath(String path) {
        File file = new File(path);
        file.delete();
    }

    public static void rename(String oldPath, String newPath) {
        File oldFile = new File(oldPath);
        File newFile = new File(newPath);
        oldFile.renameTo(newFile);
    }

    public static boolean isExist(String path) {
        File file = new File(path);
        return file.exists();
    }

    public static void saveFile(InputStream in, File file) {
        byte[] buff = new byte[2048];
        int length;
        try {
            FileOutputStream fos = new FileOutputStream(file);
            while ((length = in.read(buff)) != -1) {
                fos.write(buff, 0, length);
            }
            in.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveFile(File file, Uri uri) throws Exception {
        FileInputStream inStream = new FileInputStream(new File(uri.getPath()));
        FileOutputStream outStream = new FileOutputStream(file);
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
        inStream.close();
        outStream.close();
    }

    public static List<StorageInfo> listAllStorage(Context context) {
        ArrayList<StorageInfo> storages = new ArrayList<>();
        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        try {
            Class<?>[] paramClasses = {};
            Method getVolumeList = StorageManager.class.getMethod("getVolumeList", paramClasses);
            Object[] params = {};
            Object[] invokes = (Object[]) getVolumeList.invoke(storageManager, params);

            if (invokes != null) {
                StorageInfo info;
                for (Object obj : invokes) {
                    Method getPath = obj.getClass().getMethod("getPath");
                    String path = (String) getPath.invoke(obj, new Object[0]);
                    info = new StorageInfo(path);

                    Method getVolumeState = StorageManager.class.getMethod("getVolumeState", String.class);
                    info.state = (String) getVolumeState.invoke(storageManager, info.path);

                    Method isRemovable = obj.getClass().getMethod("isRemovable");
                    info.isRemoveable = (Boolean) isRemovable.invoke(obj, new Object[0]);
                    storages.add(info);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        storages.trimToSize();
        return storages;
    }

    public static List<StorageInfo> getAvailableStorage(Context context) {
        List<StorageInfo> storages = new ArrayList<>();
        List<StorageInfo> infos = listAllStorage(context);
        for (StorageInfo info : infos) {
            File file = new File(info.path);
            if ((file.exists()) && (file.isDirectory()) && (file.canWrite())) {
                if (info.isMounted()) {
                    storages.add(info);
                }
            }
        }
        return storages;
    }

    public static ArrayList<File> getFileListByPath(String path) {
        File direction = new File(path);
        File[] result = direction.listFiles();
        ArrayList<File> fileList = new ArrayList<>();
        for (File file : result) {
            if (file.isDirectory() && !file.getName().startsWith(".")) {
                fileList.add(file);
            }
        }
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });
        return fileList;
    }

    public static File getFileByPath(String path) {
        return new File(path);
    }

    public static boolean writeFileFromString(String path, String content, boolean append) {
        return writeFileFromString(getFileByPath(path), content, append);
    }

    public static boolean writeFileFromString(File file, String content, boolean append) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file, append));
            writer.write(content);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 指定编码按行读取文件到链表中
     *
     * @param filePath    文件路径
     * @param charsetName 编码格式
     * @return 文件行链表
     */
    public static List<String> readFile2List(String filePath, String charsetName) {
        return readFile2List(getFileByPath(filePath), charsetName);
    }

    /**
     * 指定编码按行读取文件到链表中
     *
     * @param file        文件
     * @param charsetName 编码格式
     * @return 文件行链表
     */
    public static List<String> readFile2List(File file, String charsetName) {
        return readFile2List(file, 0, 0x7FFFFFFF, charsetName);
    }

    /**
     * 指定编码按行读取文件到链表中
     *
     * @param filePath    文件路径
     * @param st          需要读取的开始行数
     * @param end         需要读取的结束行数
     * @param charsetName 编码格式
     * @return 包含制定行的list
     */
    public static List<String> readFile2List(String filePath, int st, int end, String
            charsetName) {
        return readFile2List(getFileByPath(filePath), st, end, charsetName);
    }

    /**
     * 指定编码按行读取文件到链表中
     *
     * @param file        文件
     * @param st          需要读取的开始行数
     * @param end         需要读取的结束行数
     * @param charsetName 编码格式
     * @return 包含从start行到end行的list
     */
    public static List<String> readFile2List(File file, int st, int end, String charsetName) {
        if (file == null) return null;
        if (st > end) return null;
        BufferedReader reader = null;
        try {
            String line;
            int curLine = 1;
            List<String> list = new ArrayList<>();
            if (TextUtils.isEmpty(charsetName)) {
                reader = new BufferedReader(new FileReader(file));
            } else {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), charsetName));
            }
            while ((line = reader.readLine()) != null) {
                if (curLine > end) break;
                if (st <= curLine && curLine <= end) list.add(line);
                ++curLine;
            }
            return list;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 指定编码按行读取文件到字符串中
     *
     * @param filePath    文件路径
     * @param charsetName 编码格式
     * @return 字符串
     */
    public static String readFile2String(String filePath, String charsetName) {
        return readFile2String(getFileByPath(filePath), charsetName);
    }

    /**
     * 指定编码按行读取文件到字符串中
     *
     * @param file        文件
     * @param charsetName 编码格式
     * @return 字符串
     */
    public static String readFile2String(File file, String charsetName) {
        if (file == null) return null;
        BufferedReader reader = null;
        try {
            StringBuilder sb = new StringBuilder();
            if (TextUtils.isEmpty(charsetName)) {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            } else {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), charsetName));
            }
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\r\n");// windows系统换行为\r\n，Linux为\n
            }
            // 要去除最后的换行符
            return sb.delete(sb.length() - 2, sb.length()).toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 读取文件到字符数组中
     *
     * @param filePath 文件路径
     * @return 字符数组
     */
    public static byte[] readFile2Bytes(String filePath) {
        return readFile2Bytes(getFileByPath(filePath));
    }

    /**
     * 读取文件到字符数组中
     *
     * @param file 文件
     * @return 字符数组
     */
    public static byte[] readFile2Bytes(File file) {
        if (file == null) return null;
        try {
            return inputStream2Bytes(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * inputStream转byteArr
     *
     * @param is 输入流
     * @return 字节数组
     */
    public static byte[] inputStream2Bytes(InputStream is) {
        if (is == null) return null;
        return input2OutputStream(is).toByteArray();
    }

    /**
     * inputStream转outputStream
     *
     * @param is 输入流
     * @return outputStream子类
     */
    public static ByteArrayOutputStream input2OutputStream(InputStream is) {
        if (is == null) return null;
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int len;
            while ((len = is.read(b, 0, 1024)) != -1) {
                os.write(b, 0, len);
            }
            return os;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

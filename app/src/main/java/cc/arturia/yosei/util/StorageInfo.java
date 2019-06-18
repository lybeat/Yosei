package cc.arturia.yosei.util;

/**
 * Author: Arturia
 * Date: 2018/6/6
 */
public class StorageInfo {

    public String path;
    public String state;
    public boolean isRemoveable;
    public StorageInfo(String path) {
        this.path = path;
    }
    public boolean isMounted() {
        return "mounted".equals(state);
    }
    @Override
    public String toString() {
        return "StorageInfo [path=" + path + ", state=" + state
                + ", isRemoveable=" + isRemoveable + "]";
    }
}

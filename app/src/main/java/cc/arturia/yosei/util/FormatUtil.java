package cc.arturia.yosei.util;

import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Author: Arturia
 * Date: 2018/11/9
 */
public class FormatUtil {

    public static String format2Int(long value) {
        DecimalFormat format = new DecimalFormat("00");
        format.setRoundingMode(RoundingMode.DOWN);
        return format.format(value);
    }
}

package com.dili.trace.util;

import com.dili.common.exception.TraceBizException;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegUtils {
    public static boolean isPlate(String carNo) {
        Pattern p = Pattern.compile("^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领A-Z]{1}[A-Z]{1}([A-Z0-9]){4,5}[A-Z0-9挂学警港澳]{1}$");
        Matcher m = p.matcher(StringUtils.trimToEmpty(carNo).toUpperCase());
        if (!m.matches()) {
            return false;
        }
        return true;
    }

    public static boolean isValidInput(String input) {

        Pattern emoji = Pattern.compile("[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\ud83e\udd00-\ud83e\udfff]|[\u2600-\u27ff]", Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);
        Matcher emojiMatcher = emoji.matcher(input);
        if (emojiMatcher.find()) {
            return false;
        }
        //String str = "abcDD_-34中";
        String regex = "^(\\w|[\\u4e00-\\u9fa5]|-)+$";
        return Pattern.matches(regex, input);
    }
}

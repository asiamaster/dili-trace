package com.dili.trace.util;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import one.util.streamex.StreamEx;

import java.util.List;
import java.util.function.Function;

public class JsonPathUtil {
    private static Configuration conf = Configuration.defaultConfiguration().addOptions(Option.ALWAYS_RETURN_LIST, Option.DEFAULT_PATH_LEAF_TO_NULL);

    public static <T> List<T> parse(String json, String path, Class<T> clz) {

        List<Object> certTypeObjectList = JsonPath.using(conf).parse(json).read(path);
        List<T> valueList = StreamEx.ofNullable(certTypeObjectList).flatCollection(Function.identity()).nonNull()
                .select(clz).toList();
        return valueList;
    }
}

package com.transsnet.vskit.neptune.components.parser;

import com.transsnet.vskit.neptune.model.QueryResult;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

import static com.transsnet.vskit.neptune.model.QueryResult.Type.*;

/**
 * @author yangwei
 * @date 2020-01-08 18:53
 */
@Slf4j
public class TokenParser {
    private static final String VERTEX_TYPE_PREFIX = "g.V";
    private static final String EDGE_TYPE_PREFIX = "g.E";
    private static final String PATH_TYPE_SUFFIX = ".path()";
    /**
     * 限制执行语句
     */
    private static final List<String> BLACK_LIST = Arrays.asList("drop()", "max()", "min()");

    private TokenParser() {
    }

    public static QueryResult.Type parseType(String code) {
        validateBlack(code);

        QueryResult.Type type = OTHER;
        if (code.indexOf(VERTEX_TYPE_PREFIX) == 0) {
            type = VERTEX;
        }
        if (code.indexOf(EDGE_TYPE_PREFIX) == 0) {
            type = EDGE;
        }
        if (code.lastIndexOf(PATH_TYPE_SUFFIX) == code.length() - 7) {
            type = PATH;
        }

        return type;
    }

    private static void validateBlack(String code) {
        for (String s : BLACK_LIST) {
            if (code.contains(s)) {
                log.error("该语句执行被限制");
            }
        }
    }
}

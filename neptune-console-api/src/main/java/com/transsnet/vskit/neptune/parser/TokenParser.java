package com.transsnet.vskit.neptune.parser;

import com.transsnet.vskit.neptune.model.QueryResult;
import lombok.extern.slf4j.Slf4j;

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
    private static final String LIMIT_CODES = "drop()|max()|min()";

    private TokenParser() {
    }

    public static QueryResult.Type parseType(String code) {
        validate(code);

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

    private static void validate(String code) {
        String[] limitCodes = LIMIT_CODES.split("\\|");
        for (String limitCode : limitCodes) {
            if (code.contains(limitCode)) {
                log.error("该语句执行被限制");
            }
        }
    }
}

package dev.paoding.longan.data.jpa;

import com.github.vertical_blank.sqlformatter.SqlFormatter;
import dev.paoding.longan.util.GsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.util.HashMap;
import java.util.Map;

public class SqlLogger {
    private static final Logger logger = LoggerFactory.getLogger(SqlLogger.class);
    private static boolean showSql;

    public static void showSql(boolean showSql) {
        SqlLogger.showSql = showSql;
    }

    public static void log(String sql) {
        if (showSql) {
            logger.info("statement\n{}", SqlFormatter.format(sql));
        }
    }

    public static void log(Map<String, ?> paramMap) {
        if (showSql) {
            logger.info("parameter\n{}", GsonUtils.toJson(paramMap));
        }
    }

    public static void log(SqlParameterSource paramSource) {
        if (showSql) {
            Map<String, Object> paramMap = new HashMap<>();
            for (String name : paramSource.getParameterNames()) {
                paramMap.put(name, paramSource.getValue(name));
            }
            log(paramMap);
        }
    }


}

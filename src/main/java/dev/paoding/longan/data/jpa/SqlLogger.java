package dev.paoding.longan.data.jpa;

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
            logger.info("\n" + sql);
        }
    }

    public static void info(String sql) {
        logger.info(sql);
    }

    public static void log(SqlParameterSource paramSource) {
        if (showSql) {
            Map<String, Object> paramMap = new HashMap<>();
            for (String name : paramSource.getParameterNames()) {
                paramMap.put(name, paramSource.getValue(name));
            }
            log(paramMap.toString());
        }
    }

}

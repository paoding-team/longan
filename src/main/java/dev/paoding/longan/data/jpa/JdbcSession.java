package dev.paoding.longan.data.jpa;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class JdbcSession {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final JdbcOperations jdbcOperations;

    public JdbcSession(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.jdbcOperations = namedParameterJdbcTemplate.getJdbcOperations();
    }

    public void execute(String sql) {
        log(sql);
        jdbcOperations.execute(sql);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) throws DataAccessException {
        log(sql);
        return jdbcOperations.query(sql, rowMapper, args);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) throws DataAccessException {
        log(sql);
        return jdbcOperations.queryForObject(sql, rowMapper, args);
    }

    public <T> T queryForObject(String sql, Map<String, ?> paramMap, Class<T> requiredType) throws DataAccessException {
        log(sql);
        log(paramMap);
        return namedParameterJdbcTemplate.queryForObject(sql, paramMap, requiredType);
    }

    public <T> T queryForObject(String sql, Map<String, ?> paramMap, RowMapper<T> rowMapper) throws DataAccessException {
        log(sql);
        log(paramMap);
        return namedParameterJdbcTemplate.queryForObject(sql, paramMap, rowMapper);
    }

    public <T> T queryForObject(String sql, Class<T> requiredType, Object... args) throws DataAccessException {
        log(sql);
        return jdbcOperations.queryForObject(sql, requiredType, args);
    }

    public int update(String sql) throws DataAccessException {
        log(sql);
        return jdbcOperations.update(sql);
    }

    public int update(String sql, Object[] args) throws DataAccessException {
        log(sql);
        return jdbcOperations.update(sql, args);
    }

    public int update(String sql, SqlParameterSource paramSource) throws DataAccessException {
        log(sql);
        return namedParameterJdbcTemplate.update(sql, paramSource);
    }

    public int update(String sql, Map<String, ?> paramMap) throws DataAccessException {
        log(sql);
        log(paramMap);
        return namedParameterJdbcTemplate.update(sql, paramMap);
    }

    public int update(String sql, SqlParameterSource paramSource, KeyHolder generatedKeyHolder)
            throws DataAccessException {
        log(sql);
        log(paramSource);
        return namedParameterJdbcTemplate.update(sql, paramSource, generatedKeyHolder);
    }

    public long queryForLong(String sql) throws DataAccessException {
        log(sql);
        return jdbcOperations.queryForObject(sql, Long.class);
    }

    public long queryForLong(String sql, Map<String, ?> paramMap) throws DataAccessException {
        log(sql);
        log(paramMap);
        return namedParameterJdbcTemplate.queryForObject(sql, paramMap, Long.class);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) throws DataAccessException {
        log(sql);
        return namedParameterJdbcTemplate.query(sql, rowMapper);
    }

    public <T> List<T> query(String sql, Map<String, ?> paramMap, RowMapper<T> rowMapper) throws DataAccessException {
        log(sql);
        log(paramMap);
        return namedParameterJdbcTemplate.query(sql, paramMap, rowMapper);
    }

    public <T> List<T> queryForList(String sql, Map<String, ?> paramMap, Class<T> type) throws DataAccessException {
        log(sql);
        log(paramMap);
        return namedParameterJdbcTemplate.queryForList(sql, paramMap, type);
    }

    public List<Map<String, Object>> queryForList(String sql) throws DataAccessException {
        log(sql);
        return jdbcOperations.queryForList(sql);
    }

    public DatabaseMetaData getMetaData() {
        try {
            return Objects.requireNonNull(namedParameterJdbcTemplate.getJdbcTemplate().getDataSource()).getConnection().getMetaData();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Connection getConnection() {
        try {
            return namedParameterJdbcTemplate.getJdbcTemplate().getDataSource().getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void log(String sql) {
        SqlLogger.log(sql);
    }

    private void log(Map<String, ?> paramMap) {
//        String str = Joiner.on(", ").withKeyValueSeparator(" = ").join(paramMap);
        SqlLogger.log(paramMap.toString());
    }

    private void log(SqlParameterSource paramSource) {
        SqlLogger.log(paramSource);
    }
}

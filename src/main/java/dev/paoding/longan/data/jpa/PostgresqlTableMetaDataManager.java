package dev.paoding.longan.data.jpa;

public class PostgresqlTableMetaDataManager extends TableMetaDataManager {
    public PostgresqlTableMetaDataManager(JdbcSession jdbcSession) {
        super(jdbcSession);
    }

    @Override
    protected String decorateIndexName(String prefix, String tableName, String indexName) {
        return prefix + "_" + tableName + "_" + indexName;
    }

}

package dev.paoding.longan.data.jpa;

public class MysqlTableMetaDataManager extends TableMetaDataManager {
    public MysqlTableMetaDataManager(JdbcSession jdbcSession) {
        super(jdbcSession);
    }

    @Override
    protected String decorateIndexName(String prefix, String tableName, String indexName) {
        return prefix + "_" + indexName;
    }

}

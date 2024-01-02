package dev.paoding.longan.data.jpa;

public interface RepositoryPostProcessor {

    void addRepositoryProxy(JpaRepositoryProxy<?, ?> repositoryProxy);

    void postProcessAfterInitialization(JdbcSession jdbcSession);
}

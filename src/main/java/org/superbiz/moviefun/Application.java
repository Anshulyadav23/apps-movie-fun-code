package org.superbiz.moviefun;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.zaxxer.hikari.HikariConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
public class Application {

    public static void main(String... args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ServletRegistrationBean actionServletRegistration(ActionServlet actionServlet) {
        return new ServletRegistrationBean(actionServlet, "/moviefun/*");
    }

    @Bean
    public DatabaseServiceCredentials databaseServiceCredentials() {
        return new DatabaseServiceCredentials(System.getenv("VCAP_SERVICES"));
    }

    @Bean
    public DataSource moviesDataSource(DatabaseServiceCredentials databaseServiceCredentials) {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(databaseServiceCredentials.jdbcUrl("movies-mysql", "p-mysql"));
        HikariConfig config = new HikariConfig();
        config.setDataSource(dataSource);
        HikariDataSource hDataSource = new HikariDataSource(config);
        return hDataSource;
    }

    @Bean
    public DataSource albumsDataSource(DatabaseServiceCredentials serviceCredentials) {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(serviceCredentials.jdbcUrl("albums-mysql", "p-mysql"));

        HikariConfig config = new HikariConfig();
        config.setDataSource(dataSource);
        HikariDataSource hDataSource = new HikariDataSource(config);
        return hDataSource;
    }

    @Bean
    public HibernateJpaVendorAdapter jpaVendorAdapter() {
        HibernateJpaVendorAdapter adaptor = new HibernateJpaVendorAdapter();
        adaptor.setDatabase(Database.MYSQL);
        adaptor.setDatabasePlatform("org.hibernate.dialect.MySQL5Dialect");
        adaptor.setGenerateDdl(true);
        return adaptor;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean albumsLocalContainerEntityManagerFactoryBean(DataSource albumsDataSource, HibernateJpaVendorAdapter jpaVendorAdapter) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(albumsDataSource);
        em.setJpaVendorAdapter(jpaVendorAdapter);
        em.setPackagesToScan("org.superbiz.moviefun.albums");
        em.setPersistenceUnitName("albums");
        return em;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean movieseLocalContainerEntityManagerFactoryBean(DataSource moviesDataSource, HibernateJpaVendorAdapter jpaVendorAdapter) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(moviesDataSource);
        em.setJpaVendorAdapter(jpaVendorAdapter);
        em.setPackagesToScan("org.superbiz.moviefun.movies");
        em.setPersistenceUnitName("movies");
        return em;
    }

    @Bean
    public PlatformTransactionManager moviesPlatformTransactionManager( EntityManagerFactory movieseLocalContainerEntityManagerFactoryBean) {
        return new JpaTransactionManager(movieseLocalContainerEntityManagerFactoryBean);
    }

    @Bean
    public PlatformTransactionManager albumsPlatformTransactionManager(EntityManagerFactory albumsLocalContainerEntityManagerFactoryBean) {
        return new JpaTransactionManager(albumsLocalContainerEntityManagerFactoryBean);
    }

    @Bean
    TransactionTemplate albumsTransactionTemplate(PlatformTransactionManager albumsPlatformTransactionManager) {
        return new TransactionTemplate(albumsPlatformTransactionManager);
    }

    @Bean
    TransactionTemplate moviesTransactionTemplate(PlatformTransactionManager moviesPlatformTransactionManager) {
        return new TransactionTemplate(moviesPlatformTransactionManager);
    }
}

package io.bmatch.gateway.configuration;

import com.ecwid.consul.v1.ConsulClient;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;

import static io.r2dbc.spi.ConnectionFactoryOptions.*;

@Configuration
public class DatabaseConfig extends AbstractR2dbcConfiguration {

    private final DataSourceUrlUpdater dataSourceUrlUpdater;

    @Autowired
    public DatabaseConfig(DataSourceUrlUpdater dataSourceUrlUpdater, ConsulClient client, ConsulClient consulClient) {
        this.dataSourceUrlUpdater = dataSourceUrlUpdater;
    }

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${spring.datasource.database}")
    private String dbScheme;

    @Value("${spring.datasource.host}")
    private String dbHost;


    @Value("${bmatch.isProduction}")
    private boolean isProduction;

    @Override
    @Bean
    public ConnectionFactory connectionFactory() {
        String dbURL = isProduction ? dataSourceUrlUpdater.getHost() : dbHost;

        return ConnectionFactories.get(ConnectionFactoryOptions.builder()
                .option(DRIVER, "postgresql")
                .option(PROTOCOL, "postgresql")
                .option(HOST, dbURL)
                .option(PORT, 5432)
                .option(USER, dbUsername)
                .option(PASSWORD, dbPassword)
                .option(DATABASE, dbScheme)
                .build());
    }
}


package quest.dine.gateway.configuration;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.kv.model.GetValue;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.init.DataSourceScriptDatabaseInitializer;
import org.springframework.boot.r2dbc.init.R2dbcScriptDatabaseInitializer;
import org.springframework.boot.sql.init.DatabaseInitializationSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.r2dbc.connection.init.CompositeDatabasePopulator;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import quest.dine.gateway.services.ConsulKV;

import java.util.Arrays;

import static io.r2dbc.spi.ConnectionFactoryOptions.*;

@Configuration
public class DatabaseConfig extends AbstractR2dbcConfiguration {

    private final DataSourceUrlUpdater dataSourceUrlUpdater;
    private final ConsulKV consulKV;

    @Autowired
    public DatabaseConfig(DataSourceUrlUpdater dataSourceUrlUpdater, ConsulClient client, ConsulClient consulClient, ConsulKV consulKV) {
        this.dataSourceUrlUpdater = dataSourceUrlUpdater;
        this.consulKV = consulKV;
    }

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${spring.datasource.database}")
    private String dbScheme;

    @Value("${dine.quest.isProduction}")
    private boolean isProduction;
    @Override
    @Bean
    public ConnectionFactory connectionFactory() {
        String isConsulProduction = consulKV.getKeyValue("isProduction");
        String dbHost = "";

        if(isConsulProduction == null){
            if(!isProduction){
                consulKV.setKeyValue("isProduction", "false");
            }
        }

        if(isConsulProduction != null){
            if(!Boolean.parseBoolean(isConsulProduction)){
                dbHost = "localhost";
            }
            else {
                dbHost = dataSourceUrlUpdater.getHost();
            }
        }

        return ConnectionFactories.get(ConnectionFactoryOptions.builder()
                .option(DRIVER, "postgresql")
                .option(PROTOCOL, "postgresql")
                .option(HOST, dbHost)
                .option(PORT, dataSourceUrlUpdater.getPort())
                .option(USER, dbUsername)
                .option(PASSWORD, dbPassword)
                .option(DATABASE, dbScheme)
                .build());
    }
}


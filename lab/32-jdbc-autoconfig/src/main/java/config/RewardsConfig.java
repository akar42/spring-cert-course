package config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;

import rewards.RewardNetwork;
import rewards.internal.RewardNetworkImpl;
import rewards.internal.account.AccountRepository;
import rewards.internal.account.JdbcAccountRepository;
import rewards.internal.restaurant.JdbcRestaurantRepository;
import rewards.internal.restaurant.RestaurantRepository;
import rewards.internal.reward.JdbcRewardRepository;
import rewards.internal.reward.RewardRepository;

import javax.sql.DataSource;

@Configuration
public class RewardsConfig {

    private final Logger logger = LoggerFactory.getLogger(getClass());

	// DataSource dataSource;

	// @Autowired  // This @Autowired annotation is optional here
	// public RewardsConfig(DataSource dataSource) {
	// 	this.dataSource = dataSource;
	// }

    // TODO-10 (Optional) : Switch back to explicit `DataSource` configuration
    // (Instead of using auto-configured DataSource, we are going to configure
    //  our own DataSource bean. Normally we want to configure infra-structure
    //  bean such as DataSource bean in a separate configuration class but
    //  here for the sake of simplicity, we configure it along with application
    //  beans.)
    // - Uncomment @Bean method below
    // - Remove the code above that performs DataSource injection
    // - Fix compile errors in this code
    
    @Bean
    public DataSource dataSource() {
        logger.debug("Creating the datasource bean explicitly");

        return
                (new EmbeddedDatabaseBuilder())
                        .addScript("classpath:schema.sql")
                        .addScript("classpath:data.sql")
                        .build();
    }
    

    @Bean
    public RewardNetwork rewardNetwork(DataSource dataSource) {
        return new RewardNetworkImpl(
                accountRepository(dataSource),
                restaurantRepository(dataSource),
                rewardRepository(dataSource));
    }

    @Bean
    public AccountRepository accountRepository(DataSource dataSource) {
        JdbcAccountRepository repository = new JdbcAccountRepository(dataSource);
        return repository;
    }

    @Bean
    public RestaurantRepository restaurantRepository(DataSource dataSource) {
        JdbcRestaurantRepository repository = new JdbcRestaurantRepository(dataSource);
        return repository;
    }

    @Bean
    public RewardRepository rewardRepository(DataSource dataSource) {
        JdbcRewardRepository repository = new JdbcRewardRepository(dataSource);
        return repository;
    }

}

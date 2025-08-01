package rewards.internal.restaurant;

import common.money.Percentage;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import rewards.Dining;
import rewards.internal.account.Account;

import javax.sql.DataSource;
import javax.swing.tree.RowMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Loads restaurants from a data source using the JDBC API.
 */

// TODO-09 (Optional) : Inject JdbcTemplate directly to this repository class
// - Refactor the constructor to get the JdbcTemplate injected directly
//   (instead of DataSource getting injected)
// - Refactor RewardsConfig accordingly
// - Refactor JdbcRestaurantRepositoryTests accordingly
// - Run JdbcRestaurantRepositoryTests and verity it passes

// TODO-04: Refactor the cumbersome low-level JDBC code to use JdbcTemplate.
// - Run JdbcRestaurantRepositoryTests and verity it passes
// - Add a field of type JdbcTemplate
// - Refactor the code in the constructor to instantiate JdbcTemplate object
//   from the given DataSource object
// - Refactor findByMerchantNumber(..) to use the JdbcTemplate and a RowMapper
//
//   #1: Create a RowMapper object and pass it to the
//       jdbcTemplate.queryForObject(..) method as an argument
//	 #2: The mapRestaurant(..) method provided in this class contains
//	     logic, which the RowMapper may wish to use
//
// - Run JdbcRestaurantRepositoryTests again and verity it passes

public class JdbcRestaurantRepository implements RestaurantRepository {

	private JdbcTemplate jdbcTemplate;

	public JdbcRestaurantRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public Restaurant findByMerchantNumber(String merchantNumber) {
		String sql = "select MERCHANT_NUMBER, NAME, BENEFIT_PERCENTAGE, BENEFIT_AVAILABILITY_POLICY"
				+ " from T_RESTAURANT where MERCHANT_NUMBER = ?";

		return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> this.mapRestaurant(rs), merchantNumber);
	}

	/**
	 * Maps a row returned from a query of T_RESTAURANT to a Restaurant object.
	 * @param rs the result set with its cursor positioned at the current row
	 */
	private Restaurant mapRestaurant(ResultSet rs) throws SQLException {
		// Get the row column data
		String name = rs.getString("NAME");
		String number = rs.getString("MERCHANT_NUMBER");
		Percentage benefitPercentage = Percentage.valueOf(rs.getString("BENEFIT_PERCENTAGE"));

		// Map to the object
		Restaurant restaurant = new Restaurant(number, name);
		restaurant.setBenefitPercentage(benefitPercentage);
		restaurant.setBenefitAvailabilityPolicy(mapBenefitAvailabilityPolicy(rs));
		return restaurant;
	}

	/**
	 * Helper method that maps benefit availability policy data in the ResultSet to a fully-configured
	 * {@link BenefitAvailabilityPolicy} object. The key column is 'BENEFIT_AVAILABILITY_POLICY', which is a
	 * discriminator column containing a string code that identifies the type of policy. Currently supported types are:
	 * 'A' for 'always available' and 'N' for 'never available'.
	 *
	 * More types could be added easily by enhancing this method. For example, 'W' for 'Weekdays only' or 'M' for 'Max
	 * Rewards per Month'. Some of these types might require additional database column values to be configured, for
	 * example a 'MAX_REWARDS_PER_MONTH' data column.
	 * 
	 * @param rs the result set used to map the policy object from database column values
	 * @return the matching benefit availability policy
	 * @throws IllegalArgumentException if the mapping could not be performed
	 */
	private BenefitAvailabilityPolicy mapBenefitAvailabilityPolicy(ResultSet rs) throws SQLException {
		String policyCode = rs.getString("BENEFIT_AVAILABILITY_POLICY");
		if ("A".equals(policyCode)) {
			return AlwaysAvailable.INSTANCE;
		} else if ("N".equals(policyCode)) {
			return NeverAvailable.INSTANCE;
		} else {
			throw new IllegalArgumentException("Not a supported policy code " + policyCode);
		}
	}

	/**
	 * Returns true indicating benefit is always available.
	 */
	static class AlwaysAvailable implements BenefitAvailabilityPolicy {
		static final BenefitAvailabilityPolicy INSTANCE = new AlwaysAvailable();

		public boolean isBenefitAvailableFor(Account account, Dining dining) {
			return true;
		}

		public String toString() {
			return "alwaysAvailable";
		}
	}

	/**
	 * Returns false indicating benefit is never available.
	 */
	static class NeverAvailable implements BenefitAvailabilityPolicy {
		static final BenefitAvailabilityPolicy INSTANCE = new NeverAvailable();

		public boolean isBenefitAvailableFor(Account account, Dining dining) {
			return false;
		}

		public String toString() {
			return "neverAvailable";
		}
	}
}
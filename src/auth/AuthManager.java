package src.auth;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Optional;

import src.auth.Account.AccountType;

/**
 * Data-access helper responsible for account creation, lookup and credential verification.
 */
public class AuthManager {

	private final Connection connection;

	public AuthManager(Connection connection) {
		this.connection = connection;
		ensureAuthSchema();
	}

	private void ensureAuthSchema() {
		try (Statement stmt = connection.createStatement()) {
			stmt.execute("CREATE TABLE IF NOT EXISTS accounts ("
				+ "id INT AUTO_INCREMENT PRIMARY KEY,"
				+ "account_name VARCHAR(100),"
				+ "account_type VARCHAR(20),"
				+ "email VARCHAR(255) NOT NULL UNIQUE,"
				+ "phone VARCHAR(20),"
				+ "password_hash VARCHAR(512) NOT NULL,"
				+ "password_salt VARCHAR(256) NOT NULL,"
				+ "security_question VARCHAR(255),"
				+ "security_answer VARCHAR(255),"
				+ "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
				+ "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
				+ "last_login_at TIMESTAMP NULL"
				+ ")");

			stmt.execute("CREATE TABLE IF NOT EXISTS login_audit ("
				+ "id INT AUTO_INCREMENT PRIMARY KEY,"
				+ "account_id INT NOT NULL,"
				+ "event_type VARCHAR(50),"
				+ "ip_address VARCHAR(64),"
				+ "user_agent VARCHAR(255),"
				+ "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
				+ "FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE"
				+ ")");
		} catch (SQLException ex) {
			System.err.println("Failed to ensure authentication schema: " + ex.getMessage());
		}
	}

	public Optional<Account> findByEmail(String email) throws SQLException {
		String sql = "SELECT * FROM accounts WHERE LOWER(email) = LOWER(?) LIMIT 1";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, email);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return Optional.of(mapAccount(rs));
				}
			}
		}
		return Optional.empty();
	}

	public Optional<Account> findById(int id) throws SQLException {
		String sql = "SELECT * FROM accounts WHERE id = ?";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setInt(1, id);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return Optional.of(mapAccount(rs));
				}
			}
		}
		return Optional.empty();
	}

	public Account createAccount(
			String accountName,
			AccountType accountType,
			String email,
			String phone,
			char[] password,
			String securityQuestion,
			String securityAnswer
	) throws SQLException {
		if (findByEmail(email).isPresent()) {
			throw new IllegalArgumentException("An account with this email already exists.");
		}

		String salt = PasswordHasher.generateSalt();
		String hash = PasswordHasher.hashPassword(password, salt);
		Arrays.fill(password, '\0');

		String insertSql = "INSERT INTO accounts (account_name, account_type, email, phone, password_hash, password_salt, security_question, security_answer) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

		try (PreparedStatement stmt = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
			stmt.setString(1, accountName);
			stmt.setString(2, accountType != null ? accountType.name() : null);
			stmt.setString(3, email);
			stmt.setString(4, phone);
			stmt.setString(5, hash);
			stmt.setString(6, salt);
			stmt.setString(7, securityQuestion);
			stmt.setString(8, securityAnswer);
			stmt.executeUpdate();

			try (ResultSet keys = stmt.getGeneratedKeys()) {
				if (keys.next()) {
					int newId = keys.getInt(1);
					return findById(newId)
							.orElseThrow(() -> new SQLException("Unable to load account after creation."));
				}
			}
		}

		throw new SQLException("Account creation failed: no generated key returned.");
	}

	public Account verifyCredentials(String email, char[] password) throws SQLException {
		Optional<Account> accountOpt = findByEmail(email);
		if (accountOpt.isEmpty()) {
			Arrays.fill(password, '\0');
			throw new IllegalArgumentException("Invalid email or password.");
		}

		Account account = accountOpt.get();
		boolean match = PasswordHasher.verifyPassword(password, account.getPasswordSalt(), account.getPasswordHash());
		Arrays.fill(password, '\0');

		if (!match) {
			throw new IllegalArgumentException("Invalid email or password.");
		}

		SessionContext.setCurrentAccount(account);
		updateLastLogin(account.getId());
		return account;
	}

	public void updatePassword(int accountId, char[] newPassword) throws SQLException {
		String salt = PasswordHasher.generateSalt();
		String hash = PasswordHasher.hashPassword(newPassword, salt);
		Arrays.fill(newPassword, '\0');

		String sql = "UPDATE accounts SET password_hash = ?, password_salt = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, hash);
			stmt.setString(2, salt);
			stmt.setInt(3, accountId);
			stmt.executeUpdate();
		}
	}

	public void recordLoginEvent(int accountId, String eventType, String ipAddress, String userAgent) {
		String sql = "INSERT INTO login_audit (account_id, event_type, ip_address, user_agent) VALUES (?, ?, ?, ?)";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setInt(1, accountId);
			stmt.setString(2, eventType);
			stmt.setString(3, ipAddress);
			stmt.setString(4, userAgent);
			stmt.executeUpdate();
		} catch (SQLException ex) {
			// Audit logging should not break authentication flows; log and continue.
			System.err.println("Failed to record login event: " + ex.getMessage());
		}
	}

	private void updateLastLogin(int accountId) {
		String sql = "UPDATE accounts SET last_login_at = CURRENT_TIMESTAMP WHERE id = ?";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setInt(1, accountId);
			stmt.executeUpdate();
		} catch (SQLException ex) {
			System.err.println("Failed to update last login timestamp: " + ex.getMessage());
		}
	}

	private Account mapAccount(ResultSet rs) throws SQLException {
		Account account = new Account();
		account.setId(rs.getInt("id"));
		account.setAccountName(rs.getString("account_name"));

		String type = rs.getString("account_type");
		if (type != null && !type.isBlank()) {
			try {
				account.setAccountType(AccountType.valueOf(type.toUpperCase()));
			} catch (IllegalArgumentException ex) {
				account.setAccountType(null);
			}
		}

		account.setEmail(rs.getString("email"));
		account.setPhone(rs.getString("phone"));
		account.setPasswordHash(rs.getString("password_hash"));
		account.setPasswordSalt(rs.getString("password_salt"));
		account.setSecurityQuestion(rs.getString("security_question"));
		account.setSecurityAnswer(rs.getString("security_answer"));

		Timestamp createdTs = rs.getTimestamp("created_at");
		Timestamp updatedTs = rs.getTimestamp("updated_at");
		if (createdTs != null) {
			account.setCreatedAt(createdTs.toLocalDateTime());
		}
		if (updatedTs != null) {
			account.setUpdatedAt(updatedTs.toLocalDateTime());
		}

		return account;
	}

	/**
	 * Resets the password for an account identified by email.
	 * 
	 * @param email the account email
	 * @param newPassword the new password
	 * @throws SQLException if database error occurs
	 * @throws IllegalArgumentException if account not found
	 */
	public void resetPassword(String email, char[] newPassword) throws SQLException {
		Optional<Account> accountOpt = findByEmail(email);
		if (!accountOpt.isPresent()) {
			throw new IllegalArgumentException("No account found with this email address.");
		}
		
		Account account = accountOpt.get();
		String salt = PasswordHasher.generateSalt();
		String hash = PasswordHasher.hashPassword(newPassword, salt);
		Arrays.fill(newPassword, '\0');
		
		String sql = "UPDATE accounts SET password_hash = ?, password_salt = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, hash);
			stmt.setString(2, salt);
			stmt.setInt(3, account.getId());
			stmt.executeUpdate();
		}
		
		recordLoginEvent(account.getId(), "PASSWORD_RESET", null, null);
	}
	
	/**
	 * Clears both the database and in-memory session state, typically used during logout.
	 */
	public void logout() {
		SessionContext.clear();
	}
}

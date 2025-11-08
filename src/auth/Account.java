package src.auth;

import java.time.LocalDateTime;

public class Account {

    private int id;
    private String accountName;
    private AccountType accountType;
    private String email;
    private String phone;
    private String passwordHash;
    private String passwordSalt;
    private String securityQuestion;
    private String securityAnswer;
    private String panCard;
    private String profilePicturePath;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum AccountType {
        PERSONAL,
        BUSINESS
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPasswordSalt() {
        return passwordSalt;
    }

    public void setPasswordSalt(String passwordSalt) {
        this.passwordSalt = passwordSalt;
    }

    public String getSecurityQuestion() {
        return securityQuestion;
    }

    public void setSecurityQuestion(String securityQuestion) {
        this.securityQuestion = securityQuestion;
    }

    public String getSecurityAnswer() {
        return securityAnswer;
    }

    public void setSecurityAnswer(String securityAnswer) {
        this.securityAnswer = securityAnswer;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getPanCard() {
    return panCard != null ? panCard : "";
    }

    public void setPanCard(String panCard) {
    this.panCard = panCard != null ? panCard : "";
    }

    public String getProfilePicturePath() {
    return profilePicturePath != null ? profilePicturePath : "";
    }

    public void setProfilePicturePath(String profilePicturePath) {
    this.profilePicturePath = profilePicturePath != null ? profilePicturePath : "";
    }
}

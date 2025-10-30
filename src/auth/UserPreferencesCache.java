package src.auth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

/**
 * Cache system for storing user input history (emails, names, etc.)
 * Uses Java Preferences API - completely free and built into Java
 * Does NOT store passwords for security reasons
 */
public class UserPreferencesCache {
    
    private static final Preferences prefs = Preferences.userNodeForPackage(UserPreferencesCache.class);
    private static final String DELIMITER = "|||";
    private static final int MAX_CACHE_SIZE = 10;
    
    // Cache keys
    private static final String KEY_EMAILS = "cached_emails";
    private static final String KEY_ACCOUNT_NAMES = "cached_account_names";
    private static final String KEY_PHONE_NUMBERS = "cached_phone_numbers";
    private static final String KEY_COMPANY_NAMES = "cached_company_names";
    private static final String KEY_DESIGNATIONS = "cached_designations";
    private static final String KEY_HOLDERS = "cached_holders";
    private static final String KEY_LAST_EMAIL = "last_email";
    
    /**
     * Adds an email to the cache (most recent first)
     */
    public static void cacheEmail(String email) {
        if (email == null || email.trim().isEmpty()) return;
        email = email.trim().toLowerCase();
        
        List<String> emails = getCachedEmails();
        emails.remove(email); // Remove if exists
        emails.add(0, email); // Add to front
        
        // Keep only MAX_CACHE_SIZE items
        if (emails.size() > MAX_CACHE_SIZE) {
            emails = emails.subList(0, MAX_CACHE_SIZE);
        }
        
        saveList(KEY_EMAILS, emails);
        prefs.put(KEY_LAST_EMAIL, email);
    }
    
    /**
     * Gets list of cached emails
     */
    public static List<String> getCachedEmails() {
        return getList(KEY_EMAILS);
    }
    
    /**
     * Gets the last used email
     */
    public static String getLastEmail() {
        return prefs.get(KEY_LAST_EMAIL, "");
    }
    
    /**
     * Adds an account name to the cache
     */
    public static void cacheAccountName(String name) {
        if (name == null || name.trim().isEmpty()) return;
        name = name.trim();
        
        List<String> names = getCachedAccountNames();
        names.remove(name);
        names.add(0, name);
        
        if (names.size() > MAX_CACHE_SIZE) {
            names = names.subList(0, MAX_CACHE_SIZE);
        }
        
        saveList(KEY_ACCOUNT_NAMES, names);
    }
    
    /**
     * Gets list of cached account names
     */
    public static List<String> getCachedAccountNames() {
        return getList(KEY_ACCOUNT_NAMES);
    }
    
    /**
     * Adds a phone number to the cache
     */
    public static void cachePhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) return;
        phone = phone.trim();
        
        List<String> phones = getCachedPhoneNumbers();
        phones.remove(phone);
        phones.add(0, phone);
        
        if (phones.size() > MAX_CACHE_SIZE) {
            phones = phones.subList(0, MAX_CACHE_SIZE);
        }
        
        saveList(KEY_PHONE_NUMBERS, phones);
    }
    
    /**
     * Gets list of cached phone numbers
     */
    public static List<String> getCachedPhoneNumbers() {
        return getList(KEY_PHONE_NUMBERS);
    }
    
    /**
     * Adds a company name to the cache
     */
    public static void cacheCompanyName(String company) {
        if (company == null || company.trim().isEmpty()) return;
        company = company.trim();
        
        List<String> companies = getCachedCompanyNames();
        companies.remove(company);
        companies.add(0, company);
        
        if (companies.size() > MAX_CACHE_SIZE) {
            companies = companies.subList(0, MAX_CACHE_SIZE);
        }
        
        saveList(KEY_COMPANY_NAMES, companies);
    }
    
    /**
     * Gets list of cached company names
     */
    public static List<String> getCachedCompanyNames() {
        return getList(KEY_COMPANY_NAMES);
    }
    
    /**
     * Adds a designation to the cache
     */
    public static void cacheDesignation(String designation) {
        if (designation == null || designation.trim().isEmpty()) return;
        designation = designation.trim();
        
        List<String> designations = getCachedDesignations();
        designations.remove(designation);
        designations.add(0, designation);
        
        if (designations.size() > MAX_CACHE_SIZE) {
            designations = designations.subList(0, MAX_CACHE_SIZE);
        }
        
        saveList(KEY_DESIGNATIONS, designations);
    }
    
    /**
     * Gets list of cached designations
     */
    public static List<String> getCachedDesignations() {
        return getList(KEY_DESIGNATIONS);
    }
    
    /**
     * Adds a holder name to the cache
     */
    public static void cacheHolderName(String holder) {
        if (holder == null || holder.trim().isEmpty()) return;
        holder = holder.trim();
        
        List<String> holders = getCachedHolders();
        holders.remove(holder);
        holders.add(0, holder);
        
        if (holders.size() > MAX_CACHE_SIZE) {
            holders = holders.subList(0, MAX_CACHE_SIZE);
        }
        
        saveList(KEY_HOLDERS, holders);
    }
    
    /**
     * Gets list of cached holder names
     */
    public static List<String> getCachedHolders() {
        return getList(KEY_HOLDERS);
    }
    
    /**
     * Clears all cached data
     */
    public static void clearAllCache() {
        try {
            prefs.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Helper methods
    
    private static void saveList(String key, List<String> list) {
        String joined = String.join(DELIMITER, list);
        prefs.put(key, joined);
    }
    
    private static List<String> getList(String key) {
        String stored = prefs.get(key, "");
        if (stored.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(stored.split(DELIMITER))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}

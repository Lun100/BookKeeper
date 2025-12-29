package model;

/**
 * 实体: 用户配置 (UserConfiguration) [cite: 77]
 */
public class UserConfiguration {
    private boolean localBackupEnabled = false;
    private boolean pinLockEnabled = false; // (COULD) [cite: 79]

    // Getters and Setters...
    public boolean isLocalBackupEnabled() { return localBackupEnabled; }
    public void setLocalBackupEnabled(boolean localBackupEnabled) {
        this.localBackupEnabled = localBackupEnabled;
    }
    public boolean isPinLockEnabled() { return pinLockEnabled; }
    public void setPinLockEnabled(boolean pinLockEnabled) {
        this.pinLockEnabled = pinLockEnabled;
    }
}
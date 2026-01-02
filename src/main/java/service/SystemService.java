package service;

import model.*;
import repository.IDataRepository;
import util.DataNotFoundException;
import util.TransactionType;
import util.ValidationException;
import java.util.List;

/**
 * 服务: 系统服务 (SystemService) [cite: 65]
 */
public class SystemService {
    
    private final IDataRepository<Category, String> categoryRepo;
    private final IDataRepository<Transaction, String> transactionRepo;
    private final UserConfiguration userConfiguration;

    public SystemService(
            IDataRepository<Category, String> categoryRepo,
            IDataRepository<Transaction, String> transactionRepo,
            UserConfiguration userConfiguration) {
        this.categoryRepo = categoryRepo;
        this.transactionRepo = transactionRepo;
        this.userConfiguration = userConfiguration;
    }
    
    /**
     * 对应UML中的 manageCategories (crud) (SHOULD) [cite: 66]
     * 对应UML用例: 管理类别 [cite: 150]
     */
    public Category createCategory(String name, TransactionType type) {
        Category category = new Category(name, type);
        return categoryRepo.save(category);
    }
    
    public Category updateCategory(String categoryId, String newName) {
        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new DataNotFoundException("类别不存在: " + categoryId));
        category.setName(newName);
        return categoryRepo.save(category);
    }
    
    public void deleteCategory(String categoryId) {
        categoryRepo.deleteById(categoryId);
    }
    
    public List<Category> getAllCategories() {
        return categoryRepo.findAll();
    }
    
    /**
     * 对应UML中的 exportData(format) (SHOULD) [cite: 68]
     * 对应UML用例: 导出数据 [cite: 152]
     */
    public void exportData(String format) {
        if (!format.equals("CSV") && !format.equals("Excel")) {
            throw new ValidationException("不支持的导出格式: " + format);
        }
        System.out.println("正在将 " + transactionRepo.findAll().size() + " 条交易导出为 " + format + "...");
    }

    /**
     * 对应UML中的 backupData() (SHOULD) [cite: 69]
     * 对应UML用例: 备份与恢复数据 [cite: 153]
     */
    public void backupData() {
        if (userConfiguration.isLocalBackupEnabled()) { // [cite: 78]
            System.out.println("正在执行本地备份...");
        } else {
            System.out.println("本地备份已禁用，跳过。");
        }
    }
    
    /**
     * 对应UML中的 restoreData() (SHOULD) [cite: 70]
     */
    public void restoreData() {
        System.out.println("正在从备份恢复数据...");
    }
    
    /**
     * 对应UML中的 deleteUserData() [cite: 71]
     * 对应UML用例: 删除个人数据 [cite: 154]
     */
    public void deleteUserData() {
        System.out.println("警告: 正在删除所有用户数据...");
    }
}
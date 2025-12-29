package repository;

import model.*;
import util.ValidationException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * IDataRepository 的内存实现。
 * 为了演示，这个类将充当所有类型实体的“通用”存储库。
 */
public class InMemoryDataRepository {
    
    /**
     * 内部泛型实现
     */
    static class GenericInMemoryRepository<T, ID> implements IDataRepository<T, ID> {
        private final Map<ID, T> storage = new ConcurrentHashMap<>();
        private final java.util.function.Function<T, ID> idExtractor;

        public GenericInMemoryRepository(java.util.function.Function<T, ID> idExtractor) {
            this.idExtractor = idExtractor;
        }

        @Override
        public T save(T entity) {
            ID id = idExtractor.apply(entity);
            if (id == null) {
                throw new ValidationException("实体ID不能为空");
            }
            storage.put(id, entity);
            return entity;
        }

        @Override
        public Optional<T> findById(ID id) {
            return Optional.ofNullable(storage.get(id));
        }

        @Override
        public void deleteById(ID id) {
            storage.remove(id);
        }

        @Override
        public List<T> query(Predicate<T> predicate) {
            return storage.values().stream()
                    .filter(predicate)
                    .collect(Collectors.toList());
        }
        
        @Override
        public List<T> findAll() {
            return List.copyOf(storage.values());
        }
    }

    // 为UML中需要持久化的每个实体创建专用的存储库实例
    private final IDataRepository<Transaction, String> transactionRepository =
            new GenericInMemoryRepository<>(Transaction::getTransactionId);
            
    private final IDataRepository<Account, String> accountRepository =
            new GenericInMemoryRepository<>(Account::getAccountId);

    private final IDataRepository<Category, String> categoryRepository =
            new GenericInMemoryRepository<>(Category::getCategoryId);
            
    private final IDataRepository<Budget, String> budgetRepository =
            new GenericInMemoryRepository<>(Budget::getBudgetId);
            
    private final IDataRepository<Attachment, String> attachmentRepository =
            new GenericInMemoryRepository<>(Attachment::getAttachmentId);
            
    // 提供 Getters 以便 "依赖注入" 到服务中
    public IDataRepository<Transaction, String> getTransactionRepository() { return transactionRepository; }
    public IDataRepository<Account, String> getAccountRepository() { return accountRepository; }
    public IDataRepository<Category, String> getCategoryRepository() { return categoryRepository; }
    public IDataRepository<Budget, String> getBudgetRepository() { return budgetRepository; }
    public IDataRepository<Attachment, String> getAttachmentRepository() { return attachmentRepository; }
}
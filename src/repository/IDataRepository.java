package repository;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * 接口: 数据持久化 (IDataRepository) 
 * 抽象了底层的数据存储操作 (CRUD) 
 *
 * @param <T>  实体类型 (e.g., Transaction)
 * @param <ID> 实体ID类型 (e.g., String)
 */
public interface IDataRepository<T, ID> {
    
    /**
     * 对应 save(entity)
     */
    T save(T entity);

    /**
     * 对应 findById(id)
     */
    Optional<T> findById(ID id);
    
    /**
     * 对应 delete(id)
     */
    void deleteById(ID id);

    /**
     * 对应 query(conditions) (支持2万条数据流畅查询)
     */
    List<T> query(Predicate<T> predicate);

    /**
     * 辅助方法，获取所有
     */
    List<T> findAll();
}
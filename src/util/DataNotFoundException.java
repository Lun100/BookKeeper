package util;
/**
 * 自定义数据未找到异常
 * 当根据ID查找不到实体时抛出
 */
public class DataNotFoundException extends RuntimeException {
    public DataNotFoundException(String message) {
        super(message);
    }
}

package com.chatapp.exception;

/**
 * リソースが見つからない場合の例外
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s が見つかりません: %s = %s", resourceName, fieldName, fieldValue));
    }

    public ResourceNotFoundException(String resourceName, Long id) {
        super(String.format("%s が見つかりません: ID = %d", resourceName, id));
    }
}

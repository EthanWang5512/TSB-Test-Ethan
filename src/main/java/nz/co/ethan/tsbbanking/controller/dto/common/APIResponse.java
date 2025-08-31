package nz.co.ethan.tsbbanking.controller.dto.common;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class APIResponse<T> {
    private boolean ok;
    private T data;
    private String message;

    public static <T> APIResponse<T> success(T data) {
        return new APIResponse<>(true, data, null);
    }

    public static <T> APIResponse<T> fail(String message) {
        return new APIResponse<>(false, null, message);
    }

    public static <T> APIResponse<T> success(String message) {
        return new APIResponse<>(true, null, message);
    }

}

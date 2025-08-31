package nz.co.ethan.tsbbanking.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    private int page;
    private int size;
    private long total;
    private List<T> items;

    public static <T> PageResult<T> of(int page, int size, long total, List<T> items) {
        return new PageResult<>(page, size, total, items);
    }
}

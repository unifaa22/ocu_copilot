package com.example.diagnoseillusion.common;

import com.example.diagnoseillusion.dto.common.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PageUtils {

    private PageUtils() {
    }

    public static Pageable pageable(Integer page, Integer size) {
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 10 : Math.min(size, 100);
        return PageRequest.of(p - 1, s, Sort.by(Sort.Direction.DESC, "createTime"));
    }

    public static <T> PageResult<T> toPageResult(Page<T> page) {
        return PageResult.of(
                page.getContent(),
                page.getTotalElements(),
                page.getNumber() + 1,
                page.getSize());
    }
}

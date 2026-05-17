package com.reything.subsmentbe.service;

import com.reything.subsmentbe.domain.enums.CategoryType;
import com.reything.subsmentbe.dto.category.CategoryDto;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class CategoryService {

    public List<CategoryDto> list() {
        return Arrays.stream(CategoryType.values())
                .map(c -> new CategoryDto(c.name(), c.getDisplayName(), c.getEmoji(), c.getColor()))
                .toList();
    }
}

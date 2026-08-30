package br.com.calendar.category;

import br.com.calendar.category.dto.CategoryRequestDTO;
import br.com.calendar.category.dto.CategoryResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public Category toEntity(CategoryRequestDTO request) {
        Category category = new Category();
        category.setTitle(request.title());
        category.setColor(request.color());
        category.setIcon(request.icon());
        return category;
    }

    public CategoryResponseDTO toResponse(Category category) {
        return new CategoryResponseDTO(
                category.getId(),
                category.getTitle(),
                category.getColor(),
                category.getIcon());
    }
}

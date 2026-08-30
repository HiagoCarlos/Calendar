package br.com.calendar.category;

import br.com.calendar.category.dto.CategoryRequestDTO;
import br.com.calendar.category.dto.CategoryResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(summary = "Create a category for the authenticated user")
    @PostMapping
    public ResponseEntity<CategoryResponseDTO> createCategory(
            @Valid @RequestBody CategoryRequestDTO request, Authentication authentication) {
        String userId = authenticatedUserId(authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.createCategory(request, userId));
    }

    @Operation(summary = "Get the authenticated user's categories")
    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> getCategories(Authentication authentication) {
        return ResponseEntity.ok(categoryService.getCategories(authenticatedUserId(authentication)));
    }

    private String authenticatedUserId(Authentication authentication) {
        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        return authentication.getName();
    }
}

package br.com.calendar.category;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, String> {

    List<Category> findAllByUser_IdAndDeletedAtIsNull(String userId);

    Optional<Category> findByIdAndUser_IdAndDeletedAtIsNull(String categoryId, String userId);

}

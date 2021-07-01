package org.npathai.kata.application.domain.tag.persistence;

import org.npathai.kata.application.domain.tag.dto.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TagRepository extends JpaRepository<Tag, String> {
    @Query("select t from Tag t where t.name in ?1")
    List<Tag> findAllByName(List<String> tagNames);
}

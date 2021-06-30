package org.npathai.kata.application.domain.user.persistence;

import org.npathai.kata.application.domain.user.dto.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

}

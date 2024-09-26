package com.jobapplication.org.repository;

import com.jobapplication.org.entities.AppRole;
import com.jobapplication.org.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUserName(String username);


    Boolean existsByUserName(String username);

    @Query("SELECT u FROM User u WHERE u.email <> ?1 AND u.role.roleName = ?2")
    List<User> findAllExceptEmailWithRole(String email, AppRole role);

    @Query("SELECT u FROM User u WHERE u.role.roleName = :roleName")
    List<User> findByRole(@Param("roleName") AppRole roleName);
}

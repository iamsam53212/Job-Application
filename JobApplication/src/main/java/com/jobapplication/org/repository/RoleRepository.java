package com.jobapplication.org.repository;

import com.jobapplication.org.entities.AppRole;
import com.jobapplication.org.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
   Role findByRoleName(AppRole roleName);
}


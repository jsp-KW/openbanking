
package com.fintech.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fintech.api.domain.Account;
import com.fintech.api.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {

}

package com.suyos.authservice.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.suyos.authservice.model.Account;

import jakarta.persistence.criteria.Predicate;

public class AccountSpecification {

    /**
     * Filters accounts by search text (email or username).
     *
     * @param searchText Text to filter by (case-insensitive, partial match)
     * @return Specification for filtering accounts
     */
    public static Specification<Account> filter(String searchText) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Apply email/username search if provided
            if (searchText != null && !searchText.trim().isEmpty()) {
                String pattern = "%" + searchText.trim().toLowerCase() + "%";

                Predicate emailPredicate = cb.like(cb.lower(root.get("email")), pattern);
                Predicate usernamePredicate = cb.like(cb.lower(root.get("username")), pattern);

                predicates.add(cb.or(emailPredicate, usernamePredicate));
            }

            // Return combined predicates (no filter if empty)
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
    
}
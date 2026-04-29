package com.suyos.userservice.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.suyos.userservice.model.User;

import jakarta.persistence.criteria.Predicate;

public class UserSpecification {

    /**
     * Filters users by search text: email, username, first name or last name.
     *
     * @param searchText Text to filter by (case-insensitive, partial match)
     * @return Specification for filtering users
     */
    public static Specification<User> filter(String searchText) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Apply search if provided
            if (searchText != null && !searchText.trim().isEmpty()) {
                String pattern = "%" + searchText.trim().toLowerCase() + "%";

                Predicate emailPredicate = cb.like(cb.lower(root.get("email")), pattern);
                Predicate usernamePredicate = cb.like(cb.lower(root.get("username")), pattern);
                Predicate firstNamePredicate = cb.like(cb.lower(root.get("firstName")), pattern);
                Predicate lastNamePredicate = cb.like(cb.lower(root.get("lastName")), pattern);

                predicates.add(cb.or(
                    emailPredicate,
                    usernamePredicate,
                    firstNamePredicate,
                    lastNamePredicate
                ));
            }

            // Return combined predicates (no filter if empty)
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
    
}
package com.suyos.authservice.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.suyos.authservice.model.Account;

import jakarta.persistence.criteria.Predicate;

public class AccountSpecification {

    public static Specification<Account> filter(
        String searchText
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Username or email search
            if (searchText != null && !searchText.trim().isEmpty()) {
                String pattern = "%" + searchText.trim().toLowerCase() + "%";

                Predicate emailPredicate = cb.like(cb.lower(root.get("email")), pattern);
                Predicate usernamePredicate = cb.like(cb.lower(root.get("username")), pattern);
                Predicate searchPredicates = cb.or(
                    emailPredicate, 
                    usernamePredicate
                );
                predicates.add(searchPredicates);
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

}
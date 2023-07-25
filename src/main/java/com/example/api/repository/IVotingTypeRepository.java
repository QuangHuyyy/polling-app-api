package com.example.api.repository;

import com.example.api.model.VotingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IVotingTypeRepository extends JpaRepository<VotingType, Long> {
    Optional<VotingType> findByValue(String value);
}

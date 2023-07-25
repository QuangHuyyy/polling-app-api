package com.example.api.repository;

import com.example.api.model.Poll;
import com.example.api.model.VotingToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IVotingTokenRepository extends JpaRepository<VotingToken, Long> {
    @Modifying
    @Query(value = "update VotingToken vt set vt.isUsed = true where vt.token = ?1 and vt.isUsed = false")
    void setUsedToken(String token);

    @Query(value = "select count(vt.id) = 1 from VotingToken vt where vt.token = ?1 and vt.isUsed = false")
    boolean checkTokenExist(String token);

    boolean existsByEmailAndPoll(String email, Poll poll);

    List<VotingToken> findAllByPoll(Poll poll);

    @Modifying
    @Query(value = "delete from voting_token where id = ?1", nativeQuery = true)
    void deleteToken(Long id);
}

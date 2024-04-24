package com.example.consumermicroservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.consumermicroservice.entity.ConsumerDomainEntity;

@Repository
public interface ConsumerDomainRepository extends JpaRepository<ConsumerDomainEntity, Long> {

}

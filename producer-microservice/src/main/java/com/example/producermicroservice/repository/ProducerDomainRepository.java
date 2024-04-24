package com.example.producermicroservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.producermicroservice.entity.ProducerDomainEntity;

@Repository
public interface ProducerDomainRepository extends JpaRepository<ProducerDomainEntity, Long> {

}

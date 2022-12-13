package com.ecom.dao;

import com.ecom.dto.DEFileDTO;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public interface FileRepoInf extends JpaRepository<DEFileDTO, String> {

}

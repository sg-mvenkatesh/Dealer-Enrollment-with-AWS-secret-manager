package com.ecom.controller;

import com.ecom.dao.FileRepoInf;
import com.ecom.dto.DEFileDTO;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.sql.DataSource;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import java.util.Set;

@ApplicationScoped
@Path("/api/v2")
public class FileControllerSQLDB {

    @Inject
    FileRepoInf fileRepository;

    @Inject
    Validator validator;

    @Inject
    DataSource dataSource;

    @POST
    @Consumes("application/json")
    @Path("/fileupload")
    public Response fileUploadToDB(DEFileDTO deFileDTO){
        Set<ConstraintViolation<DEFileDTO>> validate =  validator.validate(deFileDTO);
        if (validate.isEmpty()){
            DEFileDTO save = fileRepository.save(deFileDTO);
            if (save != null)
                return Response.status(Response.Status.ACCEPTED).entity("File Data Uploded successfully..!! to DB "+save.getFileId()).build();
            else
                return Response.status(Response.Status.BAD_REQUEST).entity("Unable to save the data to DB..! ").build();
        }else {
            String errorMessage = validate.iterator().next().getMessage();
            return Response.status(Response.Status.BAD_REQUEST).entity(errorMessage).build();
        }
    }
}

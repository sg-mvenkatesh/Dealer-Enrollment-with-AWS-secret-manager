package com.ecom.controller;

import com.ecom.dto.DEFileDTO;
import com.ecom.dto.RequestBody;
import com.ecom.service.FileService;
import com.ecom.dto.ResponseDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.quarkus.logging.Log;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.ws.rs.*;

import javax.ws.rs.core.Response;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static javax.ws.rs.core.Response.Status.*;

@ApplicationScoped
@Path("/api/v1")
public class FileController {
    @Inject
    FileService fileService;
    @Inject
    Validator validator;


    @POST
    @Consumes("application/json")
    @Path("/fileupload")
    public Response saveFileForByte( DEFileDTO filedetials) throws JsonProcessingException {
        Set<ConstraintViolation<DEFileDTO>> errors = validator.validate(filedetials);
        if (errors.isEmpty()) {
            Log.info("-------------Upload File to Local Ftp ControllerStarted-----------------------");

//            return fileService.uploadFileToLocalFTP(filedetials);
            return fileService.uploadTOS3(filedetials);
        }else {
            Log.error("---------------Upload File to Local Ftp field validation Error -----------");
            String errorMsg=errors.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(", "));
            return Response.status(BAD_REQUEST).entity(ResponseDTO.Bad_Request(null,errorMsg+"")).build();
        }

    }
    @GET()
    @Path("/retrievefiles")
    public Response getAll(@QueryParam("sourceType") String sourceType) throws IOException {
        Log.info("-------------retrieveFileControllerStarted-----------------------");
//        List<DEFileDTO> deFileDTOS =fileService.getAll(sourceType);
        List<DEFileDTO> deFileDTOS =  fileService.getAllS3(sourceType);
        if(deFileDTOS !=null && deFileDTOS.size()>0){
            return Response.status(OK).entity(ResponseDTO.ok(deFileDTOS, "File retrieved")).build();
        }else {
            return Response.status(NOT_FOUND).entity(ResponseDTO.notFound(deFileDTOS, "FileNotFound")).build();
        }


    }
    @POST()
    @Consumes("application/json")
    @Path("/byteStreamByFileID")
    public Response getById(RequestBody fileId) throws JsonProcessingException {
        Log.info("-------------retrieveFileBYIdControllerStarted-----------------------");
     //  List<DEFileDTO> files=fileService.getById(fileId.getFileIds());
        List<DEFileDTO> files=fileService.getByIdBFromS3(fileId.getFileIds());
       if(files!=null&& files.size()>0){
           return Response.status(OK).entity(ResponseDTO.ok(files, "File retrieved")).build();
       }else{
           return Response.status(NOT_FOUND).entity(ResponseDTO.notFound(files, "FileNotFound")).build();
       }
    }

    @GET()
    @Path("/filestatus")
    public Response fileStatus(){
        return null;
    }
}

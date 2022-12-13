package com.ecom.FileTesting;

import com.ecom.controller.FileController;
import com.ecom.dto.DEFileDTO;
import com.ecom.dto.RequestBody;
import com.ecom.dto.ResponseDTO;
import com.ecom.service.FileService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static io.smallrye.common.constraint.Assert.assertNotNull;
import static javax.ws.rs.core.Response.Status.CREATED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@QuarkusTest
public class FileTesting {
    @InjectMock
    FileService fileService;
    List<DEFileDTO> fileDTOS;
    DEFileDTO deFileDTO;
    List<String> ids=new ArrayList<>();
    RequestBody requestBoody;
    @Inject
    FileController fileController;
    @BeforeEach
    public void setup() throws IOException{
        fileDTOS=new ArrayList<>();
        deFileDTO=new DEFileDTO();
        deFileDTO.setFileId("Dealer Contract_2022-10-28 05.10.12.323");
        deFileDTO.setFileName("Dealer Contract");
        deFileDTO.setFileCreated("2022-10-28 05.10.12.323");
        deFileDTO.setSize("12kb");
        deFileDTO.setChannel("web");
        deFileDTO.setVendorName("FRG");
        deFileDTO.setMimeType("xlsx");
        fileDTOS.add(deFileDTO);
        fileDTOS.add(deFileDTO);
        requestBoody=new RequestBody();
        requestBoody.setFileIds(ids);
        when(fileService.uploadFileToLocalFTP(deFileDTO)).thenReturn(Response.status(CREATED).entity(ResponseDTO.success(null, deFileDTO.getFileName() + " file uploaded successfully")).build());
        when(fileService.getAll("inputfolder")).thenReturn(fileDTOS);
        when(fileService.getById(ids)).thenReturn(fileDTOS);
    }
    @Test
    public void successRateCase()  throws IOException{

        ids.add("Dealer Contract_2022-10-28 05.10.12.323");


        Response postResponse=fileController.saveFileForByte(deFileDTO);
        assertEquals(Response.Status.CREATED.getStatusCode(),postResponse.getStatus());
        assertNotNull(postResponse.getEntity());
        Response getAllResponse=fileController.getAll("inputfolder");
        assertEquals(Response.Status.OK.getStatusCode(),getAllResponse.getStatus());
        assertNotNull(postResponse.getEntity());
        Response getByIdResponse=fileController.getById(requestBoody);
        assertEquals(Response.Status.OK.getStatusCode(),getByIdResponse.getStatus());
        assertNotNull(postResponse.getEntity());
    }
    @Test
    public  void notFoundTestCase()throws IOException{
        deFileDTO.setFileName(null);
        Response postResponse=fileController.saveFileForByte(deFileDTO);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(),postResponse.getStatus());
        assertNotNull(postResponse.getEntity());
        Response getAllResponse= fileController.getAll("inputfolder1");
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(),getAllResponse.getStatus());
        assertNotNull(postResponse.getEntity());
        ids=null;
        Response getByIdResponse=fileController.getById(requestBoody);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(),getByIdResponse.getStatus());
        assertNotNull(postResponse.getEntity());
    }
}

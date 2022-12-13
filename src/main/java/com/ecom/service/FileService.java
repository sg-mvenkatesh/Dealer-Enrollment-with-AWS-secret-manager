package com.ecom.service;


import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import com.ecom.dto.DEFileDTO;
import com.ecom.dto.ResponseDTO;
import com.ecom.utils.sortItems;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.Log;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.Response;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

import static javax.ws.rs.core.Response.Status.*;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

@ApplicationScoped
public class FileService {
    @ConfigProperty(name = "fileProcess.InputFiles.path")
    String fileInPut;
    @ConfigProperty(name = "fileProcess.OutputFiles.path")
    String fileOutPut;
//    @ConfigProperty(name = "aws.s3.ACCESS_SEC_KEY")
//    private String secKey;
//    @ConfigProperty(name = "aws.s3.ACCESS_KEY_ID")
//    private String accessKey;
    @ConfigProperty(name = "aws.s3.BUCKET_NAME")
    private String bucketName;
    @ConfigProperty(name = "aws.s3.FOLDER_NAME_INPUT")
    private String inputFolder;
    @ConfigProperty(name = "aws.s3.FOLDER_NAME_OUTPUT")
    private String outputFolder;


    public List<DEFileDTO> getAll(String sourceType) throws IOException {
        Log.info("-----------retrieve All FileDetails ServiceStarted------------");
        File folder;
        List<DEFileDTO> deFileDTOS = new ArrayList<>();
        if (sourceType.equalsIgnoreCase("InputFolder")) {
            if (!Files.exists(Paths.get(fileInPut))) {
                Files.createDirectories(Paths.get(fileInPut));
            }
            folder = new File(fileInPut);
        } else if (sourceType.equalsIgnoreCase("OutputFolder")) {
            if (!Files.exists(Paths.get(fileOutPut))) {
                Files.createDirectories(Paths.get(fileOutPut));
            }
            folder = new File(fileOutPut);
        } else {
            Log.error("---------retrieveFile Details in Service Error unknown folder type------- ");
            folder = null;
        }
        try {
            if (folder != null) {
                File[] listOfFiles = folder.listFiles();

                for (File file : listOfFiles) {
                    if (file.isFile()) {

                        DEFileDTO DEFileDTO = new DEFileDTO();
                        String fileName = file.getName();
                        DEFileDTO.setFileId(fileName.substring(0, fileName.length() - 5));
                        String[] split = fileName.split("_");
                        DEFileDTO.setFileCreated(split[1].substring(0, split[1].length() - 5));
                        DEFileDTO.setFileName(split[0]);
                        File fileForSize = new File(fileInPut + fileName);
                        DEFileDTO.setSize(fileForSize.length() / 1024 + "kb");
                        deFileDTOS.add(DEFileDTO);
                        Collections.sort(deFileDTOS, new sortItems());
                    }
                }
            }


        } catch (ArrayIndexOutOfBoundsException e) {
            Log.error("ArrayIndexOutOfBoundsException");
            e.printStackTrace();
        }


        return deFileDTOS;
    }

    public Response uploadFileToLocalFTP(DEFileDTO file) {
        Log.info("-----------Upload File to Local Ftp ServiceStarted------------");
        try {
            if (!Files.exists(Paths.get(fileInPut))) {
                Files.createDirectories(Paths.get(fileInPut));
            }
            String replace = file.getFileCreated().replace(':', '.');
            Log.info("------------file uploaded to Local Ftp---------");
            Files.write(Paths.get(fileInPut + file.getFileName() + "_" + replace + "." + file.getMimeType()), file.getFileData(), StandardOpenOption.CREATE_NEW);
            uploadTOS3(file);
            return Response.status(CREATED).entity(ResponseDTO.success(null, file.getFileName() + " file uploaded successfully")).build();

        } catch (FileAlreadyExistsException e) {
            Log.error("------------Upload File to Local Ftp Error because File Already Exists");

            return Response.status(FORBIDDEN).entity(ResponseDTO.forbidden(null, "FileAlreadyExists " + e.getMessage())).build();

        } catch (IOException e) {
            Log.error("NoSuchFileException:" + e);
            return Response.status(INTERNAL_SERVER_ERROR).entity(ResponseDTO.internalServerError(null, e.getMessage())).build();
        }

    }


    public List<DEFileDTO> getById(List<String> fileId) {
        Log.info("----------retrieve File By FileId ServiceStarted--------");
        byte[] getBytes;

        List<DEFileDTO> fileList = new ArrayList<>();

        for (String id : fileId) {
            DEFileDTO filedto = new DEFileDTO();
            try {
                String path = fileOutPut + id + ".xlsx";
                File file = new File(path);
                getBytes = new byte[(int) file.length()];
                InputStream is = new FileInputStream(file);
                is.read(getBytes);
                is.close();
                Log.info("File retrieved");
                filedto.setFileName(id);
                filedto.setFileData(getBytes);
                fileList.add(filedto);
                Log.info("----------retrieved File By FileId--------");
            } catch (FileNotFoundException e) {
                Log.error("----------retrieve File By FileId Service FileNotFound------");
                e.printStackTrace();
            } catch (IOException e) {
                Log.error("-----------retrieve File By FileId Service Path NOt Found------");
                e.printStackTrace();
            }
        }
        return fileList;

    }

    public AmazonS3 getConnection() throws JsonProcessingException {
        Map<String, String> secret = getSecret();
//        AWSCredentials credentials1 = new BasicAWSCredentials( accessKey, secKey);
        AWSCredentials credentials = new BasicAWSCredentials(secret.get("AWS_ACCESS_KEY_ID"), secret.get("AWS_SECRET_ACCESS_KEY"));
        return AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.AP_SOUTH_1)
                .build();
    }

    public Response uploadTOS3(DEFileDTO deFileDTO) throws JsonProcessingException {
        Log.info("--------File Upload To S3 Bucket Service Started---------");
        AmazonS3 s3client = getConnection();
        String replace = deFileDTO.getFileCreated().replace(':', '.');
        String fileName = inputFolder + "/" + deFileDTO.getFileName() + "_" + replace + "." + deFileDTO.getMimeType();
        ObjectMetadata omd = new ObjectMetadata();
        omd.setContentType(deFileDTO.getMimeType());
        ByteArrayInputStream bio = new ByteArrayInputStream(deFileDTO.getFileData());
        S3Object s3Object = new S3Object();
        s3Object.setObjectContent(bio);
        s3client.putObject(
                new PutObjectRequest(bucketName, fileName, bio, omd));
        Log.info("--------File Uploaded To S3 Bucket---------");

        return Response.status(CREATED).entity(ResponseDTO.success(null, deFileDTO.getFileName() + "_" + replace)).build();
    }

    public List<DEFileDTO> getAllS3(String sourceType) throws JsonProcessingException {
        Log.info("-----------retrieve All FileDetails ServiceStarted------------");
        List<DEFileDTO> deFileDTOS = new ArrayList<>();
        AmazonS3 s3client = getConnection();
        String folderType;
        if (sourceType.equalsIgnoreCase("InputFolder")) {
            folderType = inputFolder;
        } else if (sourceType.equalsIgnoreCase("OutputFolder")) {
            folderType = outputFolder;
        } else {
            Log.error("---------retrieveFile Details in Service Error unknown folder type------- ");
            folderType = null;
        }
        if (folderType != null) {
            ObjectListing objectListing = s3client.listObjects(bucketName, folderType);


            if (objectListing != null) {
                List<S3ObjectSummary> s3ObjectSummariesList = objectListing.getObjectSummaries();
                s3ObjectSummariesList.remove(0);
                if (!s3ObjectSummariesList.isEmpty()) {
                    for (S3ObjectSummary objectSummary : s3ObjectSummariesList) {

                        String fileName = objectSummary.getKey();

                        DEFileDTO DEFileDTO = new DEFileDTO();
                        DEFileDTO.setFileId(fileName);
                        String[] split = fileName.split("[/_]");

                        DEFileDTO.setFileCreated(split[2].substring(0, split[2].length() - 5));
                        DEFileDTO.setFileName(split[1]);
                        DEFileDTO.setSize(objectSummary.getSize() / 1024 + "kb");
                        deFileDTOS.add(DEFileDTO);
                        deFileDTOS.sort(new sortItems());
                    }
                }
            }


        }

        return deFileDTOS;
    }

    public List<DEFileDTO> getByIdBFromS3(List<String> fileId) throws JsonProcessingException {
        Log.info("----------retrieve S3 File By FileId ServiceStarted--------");

        AmazonS3 s3client = getConnection();


        List<DEFileDTO> fileList = new ArrayList<>();

        for (String fileKey : fileId) {
            DEFileDTO filedto = new DEFileDTO();
            try {
                S3Object s3Object = s3client.getObject(bucketName, fileKey);
                S3ObjectInputStream s3is = s3Object.getObjectContent();
                byte[] bytes = IOUtils.toByteArray(s3is);
                s3is.close();
                Log.info("File retrieved");
                String[] split = fileKey.split("[/_]");
                filedto.setFileName(split[1]);
                filedto.setFileData(bytes);
                fileList.add(filedto);
                Log.info("----------retrieved File By FileId From S3--------");
            } catch (AmazonServiceException e) {
                Log.error("----------retrieve File By FileId Service FileNotFound In S3------");
                e.printStackTrace();
            } catch (IOException e) {
                Log.error("-----------retrieve File By FileId Service Path NOt Found------");
                e.printStackTrace();
            }
        }
        return fileList;

    }

    public static Map<String, String> getSecret() throws JsonProcessingException {
        String secretName = "/secret/dealer-enrollment";
        software.amazon.awssdk.regions.Region region = Region.of("ap-south-1");
        // Create a Secrets Manager client
        SecretsManagerClient client = SecretsManagerClient.builder()
                .region(region)
                .build();
        GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder()
                .secretId(secretName)
                .build();
        GetSecretValueResponse getSecretValueResponse;
        try {
            getSecretValueResponse = client.getSecretValue(getSecretValueRequest);
        } catch (Exception e) {
            // For a list of exceptions thrown, see
            // https://docs.aws.amazon.com/secretsmanager/latest/apireference/API_GetSecretValue.html
            throw e;
        }

        String secret = getSecretValueResponse.secretString();

        ObjectMapper mapper = new ObjectMapper();
        String json = secret;
        Map<String, String> map = mapper.readValue(json, Map.class);

        return map;

    }


}


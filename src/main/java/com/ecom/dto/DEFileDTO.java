package com.ecom.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

import static com.ecom.utils.ApplicationUtils.NOT_BLANK;

@Data
@ToString
@JsonInclude(JsonInclude.Include. NON_NULL)
@Entity
@Table(name = "dbo.input")
public class DEFileDTO {
    private String fileId;
    @NotBlank(message="fileName" + NOT_BLANK)
    private String fileName;
    @NotBlank(message="fileCreated" + NOT_BLANK)
    private String fileCreated;
    @NotBlank(message="mimeType" + NOT_BLANK)
    private String mimeType;
    @NotBlank(message="size" + NOT_BLANK)
    private String size;
    private String vendorName;
    private String channel;
    private  byte[] fileData;
}

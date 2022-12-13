package com.ecom.utils;

import lombok.Data;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;

@Data
@RequestScoped
public class Correlation {

    private String correlationId;
}

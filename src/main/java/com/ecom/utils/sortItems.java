package com.ecom.utils;

import com.ecom.dto.DEFileDTO;

import java.util.Comparator;

public class sortItems implements Comparator<DEFileDTO> {

    @Override
    public int compare(DEFileDTO o1, DEFileDTO o2) {
        return o2.getFileCreated().compareTo(o1.getFileCreated());
    }
}
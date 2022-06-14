package com.rudiak.vcfapi.controller;

import com.rudiak.vcfapi.entity.FileRegistrationRequest;
import com.rudiak.vcfapi.entity.VcfFileDescriptor;
import com.rudiak.vcfapi.service.VcfFileService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@Getter
@Setter
public class VcfFileController {

    private static final String INTERNAL_ERROR = "Internal error";
    private final VcfFileService vcfFileService;

    @Autowired
    public VcfFileController(final VcfFileService vcfFileService) {
        this.vcfFileService = vcfFileService;
    }

    @ApiOperation(value = "Register VC file in DB", notes =
            "Register VCF file in the database using file information passed in the request")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "VCF file successfully registered in the DB"),
            @ApiResponse(code = 400, message = "Missing or invalid request body"),
            @ApiResponse(code = 404, message = "VCF file not found at specified path"),
            @ApiResponse(code = 409, message = "Such VCF file descriptor already exists"),
            @ApiResponse(code = 500, message = INTERNAL_ERROR)
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/files")
    public VcfFileDescriptor registerFile(@RequestBody FileRegistrationRequest fileRegistrationRequest) {
        return vcfFileService.registerVcfFile(fileRegistrationRequest);
    }





}

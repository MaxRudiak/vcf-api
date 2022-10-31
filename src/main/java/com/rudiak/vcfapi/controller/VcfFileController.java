package com.rudiak.vcfapi.controller;

import com.rudiak.vcfapi.entity.FileRegistrationRequest;
import com.rudiak.vcfapi.entity.VariationRequest;
import com.rudiak.vcfapi.entity.VcfFileDescriptor;
import com.rudiak.vcfapi.entity.VcfFileSearchResult;
import com.rudiak.vcfapi.service.VcfFileService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Getter
@Setter
public class VcfFileController {

    private final VcfFileService vcfFileService;
    private static final String INTERNAL_ERROR = "Internal error";

    @Autowired
    public VcfFileController(VcfFileService vcfFileService) {
        this.vcfFileService = vcfFileService;
    }

    @ApiOperation(value = "Register VCF file in DB", notes =
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

    @ApiOperation(value = "Get list of registered VCF files", notes =
            "Get list of all registered in the database VCF files")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "List of files successfully received"),
            @ApiResponse(code = 500, message = INTERNAL_ERROR)
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/files")
    public List<VcfFileDescriptor> listFile() {
        return vcfFileService.loadAll();
    }

    @ApiOperation(value = "Load variations from VCF file", notes =
            "Load variations from a VCF file using genomic coordinates passed in the request")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Information successfully received"),
            @ApiResponse(code = 404, message = "VCF file not found"),
            @ApiResponse(code = 400, message = "Missing or invalid request body"),
            @ApiResponse(code = 500, message = INTERNAL_ERROR)
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/files/search")
    public VcfFileSearchResult readInformation(@RequestBody VariationRequest variationRequest) {
        return vcfFileService.readInformation(variationRequest);
    }

    @ApiOperation(value = "Delete VCF file from DB", notes =
            "Delete VCF file from database using fileID passed the request")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "File successfully deleted"),
            @ApiResponse(code = 404, message = "VCF file not found"),
            @ApiResponse(code = 500, message = INTERNAL_ERROR)
    })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/files/{id}")
    public VcfFileDescriptor deleteFile(@PathVariable("id") int id) {
        return vcfFileService.deleteById(id);
    }
}


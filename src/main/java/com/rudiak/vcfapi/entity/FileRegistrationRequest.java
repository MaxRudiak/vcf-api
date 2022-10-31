package com.rudiak.vcfapi.entity;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class FileRegistrationRequest {

    private String fileName;
    private String filePath;
    private String indexFilePath;

}

package com.rudiak.vcfapi.entity;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class FileRegistrationRequest {
    private String fileName;
    private String filePath;
    private String indexFilePath;
}

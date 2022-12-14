package com.rudiak.vcfapi.entity;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class VcfFileDescriptor {

    private int id;
    private Author author;
    private String name;
    private String filePath;
    private String indexFilePath;
    private long byteFileSize;

}

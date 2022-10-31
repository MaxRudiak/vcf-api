package com.rudiak.vcfapi.entity;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class VcfFileInfoHeader {

    private int id;
    private int idVcfFileDescriptor;
    private String idInfo;
    private String number;
    private String type;
    private String description;
    private String source;
    private String version;

}

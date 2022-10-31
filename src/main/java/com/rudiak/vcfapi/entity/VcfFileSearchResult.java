package com.rudiak.vcfapi.entity;

import lombok.*;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class VcfFileSearchResult {

    private VcfFileDescriptor vcfFileDescriptor;
    private List<VcfFileInfoHeader> headersList;
    private List<Variation> variationsList;

}

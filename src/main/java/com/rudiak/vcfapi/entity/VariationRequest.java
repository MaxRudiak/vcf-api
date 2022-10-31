package com.rudiak.vcfapi.entity;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class VariationRequest {

    private Integer fileId;
    private String chr;
    private Integer start;
    private Integer end;

}

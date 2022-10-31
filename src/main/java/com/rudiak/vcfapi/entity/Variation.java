package com.rudiak.vcfapi.entity;

import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class Variation {

    private String chrom;
    private int start;
    private int end;
    private String id;
    private String ref;
    private List<String> alt;
    private double qual;
    private Set<String> filter;
    private Map<String, Object> info;
    private Set<String> samples;

}

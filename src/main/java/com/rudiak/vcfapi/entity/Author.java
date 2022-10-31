package com.rudiak.vcfapi.entity;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class Author {

    private int id;
    private String name;
    private String email;

}

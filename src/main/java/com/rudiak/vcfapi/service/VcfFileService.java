package com.rudiak.vcfapi.service;

import com.rudiak.vcfapi.dao.AuthorDao;
import com.rudiak.vcfapi.dao.VcfFileDao;
import com.rudiak.vcfapi.entity.FileRegistrationRequest;
import com.rudiak.vcfapi.entity.VcfFileDescriptor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Getter
@Setter
public class VcfFileService {

    private final VcfFileDao vcfFileDao;
    private final AuthorDao authorDao;

    @Autowired
    public VcfFileService (final VcfFileDao vcfFileDao, final AuthorDao autorDao)  {
        this.vcfFileDao = vcfFileDao;
        this.authorDao = autorDao;
    }

    public VcfFileDescriptor registerVcfFile (final FileRegistrationRequest fileRegistrationRequest) {
        return  null;
    }


}

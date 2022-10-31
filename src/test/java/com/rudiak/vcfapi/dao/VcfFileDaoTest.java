package com.rudiak.vcfapi.dao;

import com.rudiak.vcfapi.entity.Author;
import com.rudiak.vcfapi.entity.VcfFileDescriptor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
@SpringBootTest
class VcfFileDaoTest {

    private final static int AUTHOR_ID = 1;
    private final static String AUTHOR_NAME = "Maksim";
    private final static String AUTHOR_EMAIL = "maksim@mail.ru";
    private final static int FILE_ID = 0;
    private final static String FILE_NAME = "descriptor 1";
    private final static String FILE_FILE_PATH = "someFilePath";
    private final static String FILE_INDEX_FILE_PATH = "someIndexFilePath";
    private final static int  FILE_BYTE_FILE_SIZE = 33333;
    private final Author author = new Author(AUTHOR_ID, AUTHOR_NAME, AUTHOR_EMAIL);
    private final VcfFileDescriptor descriptor = new VcfFileDescriptor(FILE_ID, author,
            FILE_NAME, FILE_FILE_PATH, FILE_INDEX_FILE_PATH, FILE_BYTE_FILE_SIZE);

    @Autowired
    private VcfFileDao vcfFileDao;

    @Test
    void testCrud() {
        final VcfFileDescriptor newDescriptor = vcfFileDao.saveVcfFileDescriptor(descriptor);
        Assertions.assertNotNull(vcfFileDao.loadVcfFileDescriptorById(newDescriptor.getId()));

        final Optional<VcfFileDescriptor> descriptorByName = vcfFileDao.getDescriptorByName(FILE_NAME);
        Assertions.assertTrue(descriptorByName.isPresent());
        Assertions.assertEquals(FILE_NAME, descriptorByName.get().getName());

        final Optional<VcfFileDescriptor> descriptorById = vcfFileDao.loadVcfFileDescriptorById(newDescriptor.getId());
        Assertions.assertTrue(descriptorById.isPresent());
        Assertions.assertEquals(1, descriptorById.get().getId());

        List<VcfFileDescriptor> descriptors = vcfFileDao.loadAll();
        Assertions.assertEquals(1, descriptors.size());

        vcfFileDao.deleteFileById(newDescriptor.getId());
        descriptors = vcfFileDao.loadAll();
        Assertions.assertEquals(0, descriptors.size());
    }
}




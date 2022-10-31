package com.rudiak.vcfapi.service;

import com.rudiak.vcfapi.entity.*;
import com.rudiak.vcfapi.exception.FileNotRegisteredException;
import com.rudiak.vcfapi.exception.SuchDescriptorAlreadyExistsException;
import com.rudiak.vcfapi.exception.VcfFileNotFoundException;
import htsjdk.tribble.TribbleException;
import htsjdk.variant.vcf.VCFFileReader;
import lombok.Setter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@Transactional
@Setter
class VcfFileServiceTest {

    private static final String FILE_NAME = "sample_1-lumpy.vcf.gz";
    private static final String FILE_PATH = "src/test/resources/files/sample_1-lumpy.vcf.gz";
    private static final String FILE_INDEX_PATH = "src/test/resources/files/sample_1-lumpy.vcf.gz.tbi";
    private static final String TYPE_STRING = "String";
    private static final String TYPE_INTEGER = "Integer";

    @Autowired
    private VcfFileService vcfFileService;

    @Test
    void shouldCreateNewDescriptor() {
        final FileRegistrationRequest fileRegistrationRequest = new FileRegistrationRequest(
                FILE_NAME,
                FILE_PATH,
                FILE_INDEX_PATH);
        final VcfFileDescriptor descriptor = vcfFileService.registerVcfFile(fileRegistrationRequest);
        Assertions.assertTrue(descriptor.getId() > 0);
        List<VcfFileDescriptor> descriptors = vcfFileService.loadAll();
        Assertions.assertTrue(descriptors.stream().anyMatch(d -> d.getId() == descriptor.getId()));
    }

    @Test
    void shouldNotCreateNewDescriptorBecauseFileFormatIsIncorrect() {
        Throwable thrown = Assertions.assertThrows(TribbleException.class, () ->
                vcfFileService.registerVcfFile(
                        new FileRegistrationRequest(
                                "invalid_file.txt",
                                "src/test/resources/files/invalid_file.txt",
                                "")));
        Assertions.assertNotNull(thrown.getMessage());
    }

    @Test
    void shouldNotCreateNewDescriptorBecauseNoFile() {
        Throwable thrown = Assertions.assertThrows(VcfFileNotFoundException.class, () ->
                vcfFileService.registerVcfFile(
                        new FileRegistrationRequest(
                                "111.txt",
                                "src/test/resources/files/111.txt",
                                "")));
        Assertions.assertNotNull(thrown.getMessage());
    }

    @Test
    void shouldNotCreateNewDescriptorBecauseItAlreadyExists() {
        final FileRegistrationRequest fileRegistrationRequest = new FileRegistrationRequest(
                FILE_NAME,
                FILE_PATH,
                FILE_INDEX_PATH);
        vcfFileService.registerVcfFile(fileRegistrationRequest);
        Throwable thrown = Assertions.assertThrows(SuchDescriptorAlreadyExistsException.class, () ->
                vcfFileService.registerVcfFile(fileRegistrationRequest));
        Assertions.assertNotNull(thrown.getMessage());
    }

    @Test
    void shouldGetEmptyList() {
        Assertions.assertTrue(vcfFileService.loadAll().isEmpty());
    }

    @Test
    void shouldGetListWithSizeOne() {
        final FileRegistrationRequest fileRegistrationRequest = new FileRegistrationRequest(
                FILE_NAME,
                FILE_PATH,
                FILE_INDEX_PATH);
        vcfFileService.registerVcfFile(fileRegistrationRequest);
        Assertions.assertEquals(1, vcfFileService.loadAll().size());
    }

    @Test
    void shouldNotDeleteBecauseNoDescriptorExists() {
        Throwable thrown = Assertions.assertThrows(FileNotRegisteredException.class, () ->
                vcfFileService.deleteById(1));
        Assertions.assertNotNull(thrown.getMessage());
    }

    @Test
    void shouldDeleteDescriptorById() {
        final FileRegistrationRequest fileRegistrationRequest = new FileRegistrationRequest(
                FILE_NAME,
                FILE_PATH,
                FILE_INDEX_PATH);
        final VcfFileDescriptor descriptor = vcfFileService.registerVcfFile(fileRegistrationRequest);
        vcfFileService.deleteById(descriptor.getId());
        Assertions.assertTrue(vcfFileService.loadAll().isEmpty());
    }

    @Test
    void shouldReadInformation() {
        final FileRegistrationRequest fileRegistrationRequest = new FileRegistrationRequest(
                FILE_NAME,
                FILE_PATH,
                FILE_INDEX_PATH);
        final VcfFileDescriptor registeredDescriptor = vcfFileService.registerVcfFile(fileRegistrationRequest);
        final VariationRequest variationRequest = new VariationRequest(registeredDescriptor.getId(),
                "14370", 20, 100);
        final VcfFileDescriptor descriptor = vcfFileService.getDescriptorById(registeredDescriptor.getId());
        List<VcfFileInfoHeader> headersList = getInfoHeadersList(descriptor.getId());
        VcfFileSearchResult result;
        try (VCFFileReader reader = getVcfReaderByDescriptor(descriptor)) {
            List<Variation> variations = vcfFileService.getVariationsList(variationRequest, reader);
            result = new VcfFileSearchResult(descriptor, headersList, variations);
        }
        Assertions.assertEquals(result.getVariationsList(),
                vcfFileService.readInformation(variationRequest).getVariationsList());
    }

    public VCFFileReader getVcfReaderByDescriptor(final VcfFileDescriptor descriptor) {
        return getFileReader(Paths.get(descriptor.getFilePath()), descriptor.getIndexFilePath());
    }

    private VCFFileReader getFileReader(final Path vcfFilePath, final String indexFilePath) {
        final File indexVcfFile = new File(indexFilePath);
        if (indexVcfFile.exists() && indexVcfFile.isFile()) {
            return new VCFFileReader(vcfFilePath, indexVcfFile.toPath());
        } else {
            return new VCFFileReader(vcfFilePath, false);
        }
    }

    private List<VcfFileInfoHeader> getInfoHeadersList(int descriptorId) {
        List<VcfFileInfoHeader> headers = new ArrayList<>();
        headers.add(new VcfFileInfoHeader(1, descriptorId, "SVTYPE", "1", TYPE_STRING,
                "Type of structural variant", "", ""));
        headers.add(new VcfFileInfoHeader(2, descriptorId, "SVLEN", "1", TYPE_INTEGER,
                "Difference in length between REF and ALT alleles", "", ""));
        headers.add(new VcfFileInfoHeader(3, descriptorId, "END", "1", TYPE_INTEGER,
                "End position of the variant described in this record", "", ""));
        headers.add(new VcfFileInfoHeader(4, descriptorId, "STRANDS", ".", TYPE_STRING,
                "Strand orientation of the adjacency in BEDPE format (DEL:+-, DUP:-+, INV:++/--)", "", ""));
        headers.add(new VcfFileInfoHeader(5, descriptorId, "IMPRECISE", "0", "Flag",
                "Imprecise structural variation", "", ""));
        headers.add(new VcfFileInfoHeader(6, descriptorId, "CIPOS", "2", TYPE_INTEGER,
                "Confidence interval around POS for imprecise variants", "", ""));
        headers.add(new VcfFileInfoHeader(7, descriptorId, "CIEND", "2", TYPE_INTEGER,
                "Confidence interval around END for imprecise variants", "", ""));
        headers.add(new VcfFileInfoHeader(8, descriptorId, "CIPOS95", "2", TYPE_INTEGER,
                "Confidence interval (95%) around POS for imprecise variants", "", ""));
        headers.add(new VcfFileInfoHeader(9, descriptorId, "CIEND95", "2", TYPE_INTEGER,
                "Confidence interval (95%) around END for imprecise variants", "", ""));
        headers.add(new VcfFileInfoHeader(10, descriptorId, "MATEID", ".", TYPE_STRING,
                "ID of mate breakends", "", ""));
        headers.add(new VcfFileInfoHeader(11, descriptorId, "EVENT", "1", TYPE_STRING,
                "ID of event associated to breakend", "", ""));
        headers.add(new VcfFileInfoHeader(12, descriptorId, "SECONDARY", "0", "Flag",
                "Secondary breakend in a multi-line variants", "", ""));
        headers.add(new VcfFileInfoHeader(13, descriptorId, "SU", ".", TYPE_INTEGER,
                "Number of pieces of evidence supporting the variant across all samples", "", ""));
        headers.add(new VcfFileInfoHeader(14, descriptorId, "PE", ".", TYPE_INTEGER,
                "Number of paired-end reads supporting the variant across all samples", "", ""));
        headers.add(new VcfFileInfoHeader(15, descriptorId, "SR", ".", TYPE_INTEGER,
                "Number of split reads supporting the variant across all samples", "", ""));
        headers.add(new VcfFileInfoHeader(16, descriptorId, "EV", ".", TYPE_STRING,
                "Type of LUMPY evidence contributing to the variant call", "", ""));
        headers.add(new VcfFileInfoHeader(17, descriptorId, "PRPOS", ".", TYPE_STRING,
                "LUMPY probability curve of the POS breakend", "", ""));
        headers.add(new VcfFileInfoHeader(18, descriptorId, "PREND", ".", TYPE_STRING,
                "LUMPY probability curve of the END breakend", "", ""));
        headers.add(new VcfFileInfoHeader(19, descriptorId, "AC", "A", TYPE_INTEGER,
                "Allele count in genotypes", "", ""));
        headers.add(new VcfFileInfoHeader(20, descriptorId, "AN", "1", TYPE_INTEGER,
                "Total number of alleles in called genotypes", "", ""));
        return headers;
    }

    @Test
    void shouldNotReadInformation() {
        Throwable thrown = Assertions.assertThrows(FileNotRegisteredException.class, () ->
                vcfFileService.readInformation(new VariationRequest(1, "", 0, 100)));
        Assertions.assertNotNull(thrown.getMessage());
    }
}

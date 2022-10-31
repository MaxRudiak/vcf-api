package com.rudiak.vcfapi.service;

import com.rudiak.vcfapi.dao.AuthorDao;
import com.rudiak.vcfapi.dao.VcfFileDao;
import com.rudiak.vcfapi.entity.*;
import com.rudiak.vcfapi.exception.FileNotRegisteredException;
import com.rudiak.vcfapi.exception.InvalidRequestException;
import com.rudiak.vcfapi.exception.SuchDescriptorAlreadyExistsException;
import com.rudiak.vcfapi.exception.VcfFileNotFoundException;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderVersion;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Setter
@Getter
public class VcfFileService {

    private final VcfFileDao vcfFileDao;
    private final AuthorDao authorDao;

    @Autowired
    public VcfFileService(final VcfFileDao vcfFileDao, final AuthorDao authorDao) {
        this.vcfFileDao = vcfFileDao;
        this.authorDao = authorDao;
    }

    public VcfFileDescriptor getDescriptorById(final int id) {
        return vcfFileDao.loadAll().stream().filter(e -> e.getId() == id).findAny().orElse(new VcfFileDescriptor());
    }

    public List<Variation> getVariationsList(VariationRequest variationRequest, VCFFileReader vcfFileReader) {
        return getVariationList(variationRequest, vcfFileReader);
    }

    public List<VcfFileDescriptor> loadAll() {
        return vcfFileDao.loadAll();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public VcfFileDescriptor deleteById(int id) {
        final VcfFileDescriptor vcfFileDescriptor = vcfFileDao.loadVcfFileDescriptorById(id)
                .orElseThrow(() ->
                        new FileNotRegisteredException(String.format("File with id = %d is not found!", id)));
        vcfFileDao.deleteFileById(vcfFileDescriptor.getId());
        return vcfFileDescriptor;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public VcfFileDescriptor registerVcfFile(final FileRegistrationRequest fileRegistrationRequest) {
        final VCFHeader fileHeader = validateRequest(fileRegistrationRequest);
        final String fileName = fileRegistrationRequest.getFileName();
        final String filePath = fileRegistrationRequest.getFilePath();
        final String indexFilePath = fileRegistrationRequest.getIndexFilePath();
        final long fileSize = new File(filePath).length();
        final VcfFileDescriptor descriptor = createNewDescriptor(fileName, filePath, indexFilePath, fileSize);
        writeInfoHeadersIntoBase(fileHeader, descriptor.getId());
        return descriptor;
    }

    public VcfFileSearchResult readInformation(final VariationRequest variationRequest) {
        validateRequest(variationRequest);
        final Optional<VcfFileDescriptor> optionalVcfFileDescriptor =
                vcfFileDao.loadVcfFileDescriptorById(variationRequest.getFileId());
        final VcfFileDescriptor vcfFileDescriptor =
                validateAndGetDescriptor(optionalVcfFileDescriptor, variationRequest);
        final List<VcfFileInfoHeader> vcfFileInfoHeaderList =
                vcfFileDao.loadVcfFileInfoHeaderByFileId(variationRequest.getFileId());
        try (VCFFileReader vcfFileReader = getFileReader(
                Paths.get(vcfFileDescriptor.getFilePath()),
                vcfFileDescriptor.getIndexFilePath())) {
            List<Variation> variationList = getVariationList(variationRequest, vcfFileReader);
            return new VcfFileSearchResult(vcfFileDescriptor, vcfFileInfoHeaderList, variationList);
        }
    }

    private VCFHeader validateRequest(final FileRegistrationRequest fileRegistrationRequest) {
        final String fileName = fileRegistrationRequest.getFileName();
        if (checkDescriptorByName(fileName)) {
            throw new SuchDescriptorAlreadyExistsException(String.format("Descriptor with the "
                            + "name %s exists in the database already",
                    fileRegistrationRequest.getFileName()));
        }
        final String filePath = fileRegistrationRequest.getFilePath();
        final File vcfFile = new File(filePath);
        if (!(vcfFile.exists() && vcfFile.isFile())) {
            throw new VcfFileNotFoundException(String.format("VCF file at path = %s does not "
                    + "exists", fileRegistrationRequest.getFilePath()));
        }
        final String indexFilePath = fileRegistrationRequest.getIndexFilePath();
        final VCFHeader fileHeader = getFileHeader(vcfFile, indexFilePath);
        if (!checkVCFHeaderVersion(fileHeader)) {
            throw new InvalidRequestException(String.format("Invalid file type. Please chek your "
                            + "file path %s}",
                    fileRegistrationRequest.getFilePath()));
        }
        return fileHeader;
    }

    private void validateRequest(final VariationRequest variationRequest) {
        if (isRequestInvalid(variationRequest)) {
            throw new InvalidRequestException(
                    String.format("Invalid request. Please check parameters of your request: {"
                                    + "fileId = %d, "
                                    + "chr = %s, "
                                    + "start = %d, "
                                    + "end = %d;}",
                            variationRequest.getFileId(), variationRequest.getChr(),
                            variationRequest.getStart(), variationRequest.getEnd()));
        }
    }

    private VCFFileReader getFileReader(final Path vcfFilePath, final String indexFilePath) {
        final File indexVcfFile = new File(indexFilePath);
        if (indexVcfFile.exists() && indexVcfFile.isFile()) {
            return new VCFFileReader(vcfFilePath, indexVcfFile.toPath());
        } else {
            return new VCFFileReader(vcfFilePath, false);
        }
    }

    private VCFHeader getFileHeader(final File vcfFile, final String indexFilePath) {
        return getFileReader(vcfFile.toPath(), indexFilePath).getFileHeader();
    }

    private VcfFileDescriptor createNewDescriptor(final String fileName, final String filePath,
                                                  final String indexFilePath, final long fileLength) {
        final Author author = authorDao.getDefaultAuthor();
        final VcfFileDescriptor descriptor = new VcfFileDescriptor();
        descriptor.setName(fileName);
        descriptor.setFilePath(filePath);
        descriptor.setIndexFilePath(indexFilePath);
        descriptor.setByteFileSize(fileLength);
        descriptor.setAuthor(author);
        return vcfFileDao.saveVcfFileDescriptor(descriptor);
    }

    private boolean checkDescriptorByName(final String fileName) {
        return vcfFileDao.getDescriptorByName(fileName).isPresent();
    }

    private boolean checkVCFHeaderVersion(final VCFHeader fileHeader) {
        return fileHeader.getVCFHeaderVersion() != null
                || hasVCFHeaderVersionFromMetaData(fileHeader);
    }

    private boolean hasVCFHeaderVersionFromMetaData(final VCFHeader fileHeader) {
        return fileHeader.getMetaDataInInputOrder().stream()
                .filter(line -> line.getKey().equals("fileformat")
                        || line.getKey().equals("format"))
                .anyMatch(line -> VCFHeaderVersion.isFormatString(line.getKey()));
    }

    private void writeInfoHeadersIntoBase(final VCFHeader fileHeader, final int descriptorId) {
        fileHeader.getInfoHeaderLines().forEach(line ->
                vcfFileDao.saveVcfInfoHeader(line, descriptorId));
    }

    private VcfFileDescriptor validateAndGetDescriptor(final Optional<VcfFileDescriptor> optionalVcfFileDescriptor,
                                                       final VariationRequest variationRequest) {
        if (!optionalVcfFileDescriptor.isPresent()) {
            throw new FileNotRegisteredException(String.format("File with id = %d not registered "
                    + "in the database", variationRequest.getFileId()));
        }
        final VcfFileDescriptor vcfFileDescriptor = optionalVcfFileDescriptor.get();
        if (isFileDoesNotExists(Paths.get(vcfFileDescriptor.getFilePath()))) {
            throw new VcfFileNotFoundException(String.format("File with id = %d not found at path %s",
                    variationRequest.getFileId(), vcfFileDescriptor.getFilePath()));
        }
        return vcfFileDescriptor;
    }

    private List<Variation> getVariationList(final VariationRequest variationRequest,
                                             final VCFFileReader vcfFileReader) {
        return vcfFileReader.query(variationRequest.getChr(),
                variationRequest.getStart(),
                variationRequest.getEnd()).stream()
                .map(this::mapContextToVariation)
                .collect(Collectors.toList());
    }

    private List<String> getAltAsStringList(final VariantContext variantContext) {
        return variantContext.getAlternateAlleles().stream().map(Allele::toString).collect(Collectors.toList());
    }

    private Variation mapContextToVariation(final VariantContext variantContext) {
        final Variation variation = new Variation();
        variation.setChrom(variantContext.getContig());
        variation.setStart(variantContext.getStart());
        variation.setEnd(variantContext.getEnd());
        variation.setId(variantContext.getID());
        variation.setRef(variantContext.getReference().toString());
        variation.setAlt(getAltAsStringList(variantContext));
        variation.setQual(variantContext.getPhredScaledQual());
        variation.setFilter(variantContext.getFilters());
        variation.setInfo(variantContext.getAttributes());
        variation.setSamples(variantContext.getGenotypes()
                .stream()
                .map(Genotype::toString)
                .collect(Collectors.toSet()));
        return variation;
    }

    private boolean isFileDoesNotExists(final Path path) {
        return Files.notExists(path) || Files.isDirectory(path);
    }

    private boolean isRequestInvalid(final VariationRequest variationRequest) {
        return variationRequest.getFileId() == null
                || variationRequest.getChr() == null
                || variationRequest.getStart() == null
                || variationRequest.getEnd() == null;
    }
}




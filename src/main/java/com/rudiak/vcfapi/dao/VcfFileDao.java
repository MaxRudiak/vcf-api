package com.rudiak.vcfapi.dao;

import com.rudiak.vcfapi.entity.Author;
import com.rudiak.vcfapi.entity.VcfFileDescriptor;
import com.rudiak.vcfapi.entity.VcfFileInfoHeader;
import htsjdk.variant.vcf.VCFInfoHeaderLine;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Setter
@Getter
public class VcfFileDao extends NamedParameterJdbcDaoSupport {

    private static final String FIELD_NUMBER = "Number=";
    private String getNewIdForDescriptorQuery;
    private String createDescriptorQuery;
    private String loadVcfFileDescriptorByNameQuery;
    private String createInfoHeaderQuery;
    private String getAllFilesQuery;
    private String deleteFileByIdQuery;
    private String loadVcfFileDescriptorByIdQuery;
    private String loadVcfFileInfoHeaderByFileIdQuery;

    @Autowired
    public VcfFileDao(final JdbcTemplate jdbcTemplate) {
        setJdbcTemplate(jdbcTemplate);
    }

    public void setGetAllFilesQuery(final String getAllFilesQuery) {
        this.getAllFilesQuery = getAllFilesQuery;
    }

    public void setDeleteFileByIdQuery(final String deleteFileByIdQuery) {
        this.deleteFileByIdQuery = deleteFileByIdQuery;
    }

    public void setGetNewIdForDescriptorQuery(final String getNewIdForDescriptorQuery) {
        this.getNewIdForDescriptorQuery = getNewIdForDescriptorQuery;
    }

    public void setCreateDescriptorQuery(final String createDescriptorQuery) {
        this.createDescriptorQuery = createDescriptorQuery;
    }

    public void setCreateInfoHeaderQuery(final String createInfoHeaderQuery) {
        this.createInfoHeaderQuery = createInfoHeaderQuery;
    }

    public void setLoadVcfFileDescriptorByNameQuery(final String loadVcfFileDescriptorByNameQuery) {
        this.loadVcfFileDescriptorByNameQuery = loadVcfFileDescriptorByNameQuery;
    }

    public void setLoadVcfFileDescriptorByIdQuery(final String loadVcfFileDescriptorByIdQuery) {
        this.loadVcfFileDescriptorByIdQuery = loadVcfFileDescriptorByIdQuery;
    }

    public void setLoadVcfFileInfoHeaderByFileIdQuery(final String loadVcfFileInfoHeaderByFileIdQuery) {
        this.loadVcfFileInfoHeaderByFileIdQuery = loadVcfFileInfoHeaderByFileIdQuery;
    }

    public List<VcfFileDescriptor> loadAll() {
        return getNamedParameterJdbcTemplate().query(getAllFilesQuery, getVcfFileDescriptorMapper());
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void deleteFileById(final int id) {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue(VcfFileDescriptorParams.FILE_ID.name(), id);
        getNamedParameterJdbcTemplate().update(deleteFileByIdQuery, params);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public VcfFileDescriptor saveVcfFileDescriptor(final VcfFileDescriptor descriptor) {
        final Integer newDescriptorId = getIdForNewDescriptor();
        final SqlParameterSource namedParameters = new MapSqlParameterSource().
                addValue(VcfFileDescriptorParams.FILE_ID.name(), newDescriptorId).
                addValue(VcfFileDescriptorParams.ID_AUTHOR.name(), descriptor.getAuthor().getId()).
                addValue(VcfFileDescriptorParams.NAME.name(), descriptor.getName()).
                addValue(VcfFileDescriptorParams.FILE_PATH.name(), descriptor.getFilePath()).
                addValue(VcfFileDescriptorParams.INDEX_FILE_PATH.name(), descriptor.getIndexFilePath()).
                addValue(VcfFileDescriptorParams.BYTE_FILE_SIZE.name(), descriptor.getByteFileSize());
        saveDescriptorIntoDataBase(namedParameters);
        descriptor.setId(newDescriptorId);
        return descriptor;
    }

    public Optional<VcfFileDescriptor> getDescriptorByName(final String fileName) {
        final SqlParameterSource namedParameters = new MapSqlParameterSource(
                VcfFileDescriptorParams.NAME.name(), fileName);
        return getNamedParameterJdbcTemplate().query(
                loadVcfFileDescriptorByNameQuery, namedParameters, getVcfFileDescriptorMapper()).stream().findAny();
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void saveVcfInfoHeader(final VCFInfoHeaderLine line, final int descriptorId) {
        final SqlParameterSource namedParameters = new MapSqlParameterSource().
                addValue(HeaderParams.HEADER_ID_VCF_FILE_DESCRIPTOR.name(), descriptorId).
                addValue(HeaderParams.HEADER_ID_INFO.name(), line.getID()).
                addValue(HeaderParams.HEADER_NUMBER.name(), getNumberFromHeaderLine(line)).
                addValue(HeaderParams.HEADER_TYPE.name(), line.getType().toString()).
                addValue(HeaderParams.HEADER_DESCRIPTION.name(), line.getDescription()).
                addValue(HeaderParams.HEADER_SOURCE.name(), line.getSource()).
                addValue(HeaderParams.HEADER_VERSION.name(), line.getVersion());
        getNamedParameterJdbcTemplate().update(createInfoHeaderQuery, namedParameters);
    }

    public Optional<VcfFileDescriptor> loadVcfFileDescriptorById(final int id) {
        final Map<String, Integer> paramMap =
                Collections.singletonMap(VcfFileDescriptorParams.FILE_ID.name(), id);
        return getNamedParameterJdbcTemplate().query(
                loadVcfFileDescriptorByIdQuery, paramMap, getVcfFileDescriptorMapper()).stream().findAny();
    }

    public List<VcfFileInfoHeader> loadVcfFileInfoHeaderByFileId(final int id) {
        Map<String, Integer> paramMap =
                Collections.singletonMap(VcfFileDescriptorParams.FILE_ID.name(), id);
        return getNamedParameterJdbcTemplate().query(loadVcfFileInfoHeaderByFileIdQuery,
                paramMap, getVcfFileInfoHeaderMapper());
    }

    private Integer getIdForNewDescriptor() {
        return getNamedParameterJdbcTemplate().queryForObject(getNewIdForDescriptorQuery,
                new MapSqlParameterSource(), Integer.class);
    }

    private void saveDescriptorIntoDataBase(final SqlParameterSource namedParameters) {
        getNamedParameterJdbcTemplate().update(createDescriptorQuery, namedParameters);
    }

    private String getNumberFromHeaderLine(final VCFInfoHeaderLine line) {
        final StringBuilder numberValue = new StringBuilder();
        final String headerLineString = line.toString();
        if (headerLineString.contains(FIELD_NUMBER)) {
            int startIndex = headerLineString.indexOf(FIELD_NUMBER);
            int endIndex = headerLineString.indexOf(",", startIndex);
            numberValue.append(headerLineString, startIndex + 7, endIndex);
        }
        return numberValue.toString();
    }

    private RowMapper<VcfFileDescriptor> getVcfFileDescriptorMapper() {
        return (resultSet, rowNum) -> {
            final Author author = new Author();
            author.setId(resultSet.getInt(AuthorParams.AUTHOR_ID.name()));
            author.setName(resultSet.getString(AuthorParams.AUTHOR_NAME.name()));
            author.setEmail(resultSet.getString(AuthorParams.AUTHOR_EMAIL.name()));
            final VcfFileDescriptor vcfFileDescriptor = new VcfFileDescriptor();
            vcfFileDescriptor.setId(resultSet.getInt(VcfFileDescriptorParams.FILE_ID.name()));
            vcfFileDescriptor.setAuthor(author);
            vcfFileDescriptor.setName(resultSet.getString(VcfFileDescriptorParams.NAME.name()));
            vcfFileDescriptor.setFilePath(resultSet.getString(VcfFileDescriptorParams.FILE_PATH.name()));
            vcfFileDescriptor.setIndexFilePath(resultSet.getString(VcfFileDescriptorParams.INDEX_FILE_PATH.name()));
            vcfFileDescriptor.setByteFileSize(resultSet.getLong(VcfFileDescriptorParams.BYTE_FILE_SIZE.name()));
            return vcfFileDescriptor;
        };
    }

    private RowMapper<VcfFileInfoHeader> getVcfFileInfoHeaderMapper() {
        return (resultSet, rowNum) -> {
            final VcfFileInfoHeader vcfFileInfoHeader = new VcfFileInfoHeader();
            vcfFileInfoHeader.setId(resultSet.getInt(HeaderParams.HEADER_ID.name()));
            vcfFileInfoHeader.setIdVcfFileDescriptor(
                    resultSet.getInt(HeaderParams.HEADER_ID_VCF_FILE_DESCRIPTOR.name()));
            vcfFileInfoHeader.setIdInfo(resultSet.getString(HeaderParams.HEADER_ID_INFO.name()));
            vcfFileInfoHeader.setNumber(resultSet.getString(HeaderParams.HEADER_NUMBER.name()));
            vcfFileInfoHeader.setType(resultSet.getString(HeaderParams.HEADER_TYPE.name()));
            vcfFileInfoHeader.setDescription(resultSet.getString(HeaderParams.HEADER_DESCRIPTION.name()));
            vcfFileInfoHeader.setSource(resultSet.getString(HeaderParams.HEADER_SOURCE.name()));
            vcfFileInfoHeader.setVersion(resultSet.getString(HeaderParams.HEADER_VERSION.name()));
            return vcfFileInfoHeader;
        };
    }

    private enum AuthorParams {
        AUTHOR_ID,
        AUTHOR_NAME,
        AUTHOR_EMAIL
    }

    private enum VcfFileDescriptorParams {
        FILE_ID,
        ID_AUTHOR,
        BYTE_FILE_SIZE,
        NAME,
        FILE_PATH,
        INDEX_FILE_PATH
    }

    private enum HeaderParams {
        HEADER_ID,
        HEADER_ID_VCF_FILE_DESCRIPTOR,
        HEADER_ID_INFO,
        HEADER_NUMBER,
        HEADER_TYPE,
        HEADER_DESCRIPTION,
        HEADER_SOURCE,
        HEADER_VERSION
    }
}

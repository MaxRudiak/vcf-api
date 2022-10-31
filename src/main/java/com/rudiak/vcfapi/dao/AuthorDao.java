package com.rudiak.vcfapi.dao;

import com.rudiak.vcfapi.entity.Author;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

@Setter
@Getter
public class AuthorDao extends NamedParameterJdbcDaoSupport {

    private String getAuthorInfoQuery;

    @Autowired
    public AuthorDao(final JdbcTemplate jdbcTemplate) {
        setJdbcTemplate(jdbcTemplate);
    }

    public void setGetAuthorInfoQuery(final String getAuthorInfoQuery) {
        this.getAuthorInfoQuery = getAuthorInfoQuery;
    }

    public Author getDefaultAuthor() {
        final SqlParameterSource namedParameters = new MapSqlParameterSource(AuthorParams.ID.name(), 1);
        return getNamedParameterJdbcTemplate().queryForObject(
                getAuthorInfoQuery, namedParameters, getAuthorMapper());
    }

    private RowMapper<Author> getAuthorMapper() {
        return (resultSet, rowNum) -> {
            final Author author = new Author();
            author.setId(resultSet.getInt(AuthorParams.ID.name()));
            author.setName(resultSet.getString(AuthorParams.NAME.name()));
            author.setEmail(resultSet.getString(AuthorParams.EMAIL.name()));
            return author;
        };
    }

    private enum AuthorParams {
        ID,
        NAME,
        EMAIL
    }
}

package com.example.poadevice.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import com.example.poadevice.domain.Poa;
import com.example.poadevice.exceptions.InternalServerErrorException;

@Repository
public class PoaRepository {

    private static final String SQL_INSERT =
            "INSERT INTO poa(id, destination_network_id) VALUES(NEXTVAL('poa_seq'), ? )";
    private static final String SQL_GET_LATEST = "SELECT * FROM poa ORDER BY id DESC LIMIT 1";

    @Autowired
    JdbcTemplate jdbcTemplate;

    public Integer write(final String destinationNetworkId) {
        try {
            final KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps =
                        connection.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, destinationNetworkId);
                return ps;
            }, keyHolder);
            return (Integer) keyHolder.getKeys().get("id");
        } catch (Exception e) {
            throw new InternalServerErrorException("Failed to store PoA");
        }
    }

    public Poa readLatest() {
        try {
            return jdbcTemplate.queryForObject(SQL_GET_LATEST, userRowMapper);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private RowMapper<Poa> userRowMapper = ((rs, rowNum) -> {
        return new Poa(rs.getInt("id"), rs.getString("destination_network_id"));
    });
}

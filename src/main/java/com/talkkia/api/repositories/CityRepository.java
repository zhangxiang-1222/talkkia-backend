package com.talkkia.api.repositories;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.talkkia.api.entity.City;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CityRepository {

    private final JdbcTemplate jdbcTemplate;

    public CityRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public City resolveCity(String countryCode, String cityCode, String cityName) {
        Long cityId = jdbcTemplate.queryForObject(
                "SELECT resolve_city(?, ?, ?)",
                Long.class,
                countryCode,
                cityCode,
                cityName
        );

        if (cityId == null) {
            throw new IllegalStateException("Unable to resolve city");
        }

        return jdbcTemplate.queryForObject(
                "SELECT id, country_code, city_code, city_name FROM cities WHERE id = ?",
                (resultSet, rowNum) -> mapCity(resultSet),
                cityId
        );
    }

    public boolean existsById(Long cityId) {
        Boolean exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM cities WHERE id = ?)",
                Boolean.class,
                cityId
        );
        return Boolean.TRUE.equals(exists);
    }

    public City findById(Long cityId) {
        return jdbcTemplate.query(
                "SELECT id, country_code, city_code, city_name FROM cities WHERE id = ?",
                (resultSet, rowNum) -> mapCity(resultSet),
                cityId
        ).stream().findFirst().orElse(null);
    }

    public City findByCityCode(String cityCode) {
        return jdbcTemplate.query(
                "SELECT id, country_code, city_code, city_name FROM cities "
                        + "WHERE city_code = btrim(?) ORDER BY id LIMIT 1",
                (resultSet, rowNum) -> mapCity(resultSet),
                cityCode
        ).stream().findFirst().orElse(null);
    }

    public List<City> findCityOptions(Long selectedCityId, int limit) {
        return jdbcTemplate.query(
                "SELECT id, country_code, city_code, city_name FROM cities "
                        + "ORDER BY CASE WHEN id = ? THEN 0 ELSE 1 END, id LIMIT ?",
                (resultSet, rowNum) -> mapCity(resultSet),
                selectedCityId,
                limit
        );
    }

    public Long incrementListen(Long cityId) {
        return jdbcTemplate.query(
                "UPDATE cities SET listen = listen + 1 WHERE id = ? RETURNING listen",
                (resultSet, rowNum) -> resultSet.getLong("listen"),
                cityId
        ).stream().findFirst().orElse(null);
    }

    private City mapCity(ResultSet resultSet) throws SQLException {
        return new City(
                resultSet.getLong("id"),
                resultSet.getString("country_code"),
                resultSet.getString("city_code"),
                resultSet.getString("city_name")
        );
    }
}

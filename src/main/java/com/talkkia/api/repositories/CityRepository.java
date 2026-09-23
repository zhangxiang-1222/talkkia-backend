package com.talkkia.api.repositories;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.talkkia.api.entity.City;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CityRepository {

    private static final String CITY_COLUMNS =
            "id, country_code, city_code, city_name, latitude, longitude";
    private static final String CITY_COLUMNS_WITH_ALIAS =
            "c.id, c.country_code, c.city_code, c.city_name, c.latitude, c.longitude";

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
                "SELECT " + CITY_COLUMNS + " FROM cities WHERE id = ?",
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
                "SELECT " + CITY_COLUMNS + " FROM cities WHERE id = ?",
                (resultSet, rowNum) -> mapCity(resultSet),
                cityId
        ).stream().findFirst().orElse(null);
    }

    public City findByCityCode(String cityCode) {
        return jdbcTemplate.query(
                "SELECT " + CITY_COLUMNS + " FROM cities "
                        + "WHERE city_code = btrim(?) ORDER BY id LIMIT 1",
                (resultSet, rowNum) -> mapCity(resultSet),
                cityCode
        ).stream().findFirst().orElse(null);
    }

    public List<City> findCityOptions(Long selectedCityId, int limit) {
        List<City> nearestCities = jdbcTemplate.query(
                "SELECT " + CITY_COLUMNS_WITH_ALIAS + " FROM cities c "
                        + "JOIN cities selected ON selected.id = ? "
                        + "WHERE selected.latitude IS NOT NULL "
                        + "AND selected.longitude IS NOT NULL "
                        + "AND c.latitude IS NOT NULL "
                        + "AND c.longitude IS NOT NULL "
                        + "ORDER BY CASE WHEN c.id = selected.id THEN 0 ELSE 1 END, "
                        + "6371.0088 * 2 * ASIN(SQRT("
                        + "POWER(SIN(RADIANS((c.latitude - selected.latitude)::double precision) / 2), 2) "
                        + "+ COS(RADIANS(selected.latitude::double precision)) "
                        + "* COS(RADIANS(c.latitude::double precision)) "
                        + "* POWER(SIN(RADIANS((c.longitude - selected.longitude)::double precision) / 2), 2)"
                        + ")), c.id LIMIT ?",
                (resultSet, rowNum) -> mapCity(resultSet),
                selectedCityId,
                limit
        );
        if (!nearestCities.isEmpty()) {
            return nearestCities;
        }

        return jdbcTemplate.query(
                "SELECT " + CITY_COLUMNS + " FROM cities "
                        + "ORDER BY CASE WHEN id = ? THEN 0 ELSE 1 END, id LIMIT ?",
                (resultSet, rowNum) -> mapCity(resultSet),
                selectedCityId,
                limit
        );
    }

    public List<City> findCitiesMissingCoordinates() {
        return jdbcTemplate.query(
                "SELECT " + CITY_COLUMNS + " FROM cities "
                        + "WHERE latitude IS NULL OR longitude IS NULL "
                        + "ORDER BY id",
                (resultSet, rowNum) -> mapCity(resultSet)
        );
    }

    public int updateCoordinatesIfMissing(
            Long cityId,
            BigDecimal latitude,
            BigDecimal longitude
    ) {
        return jdbcTemplate.update(
                "UPDATE cities SET latitude = ?, longitude = ? "
                        + "WHERE id = ? AND (latitude IS NULL OR longitude IS NULL)",
                latitude,
                longitude,
                cityId
        );
    }

    private City mapCity(ResultSet resultSet) throws SQLException {
        return new City(
                resultSet.getLong("id"),
                resultSet.getString("country_code"),
                resultSet.getString("city_code"),
                resultSet.getString("city_name"),
                resultSet.getBigDecimal("latitude"),
                resultSet.getBigDecimal("longitude")
        );
    }
}

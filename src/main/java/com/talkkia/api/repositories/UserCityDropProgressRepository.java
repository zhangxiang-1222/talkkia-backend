package com.talkkia.api.repositories;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserCityDropProgressRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserCityDropProgressRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void ensureProgressRecords(Long userId, List<Long> cityIds) {
        if (cityIds.isEmpty()) {
            return;
        }

        List<Object[]> batchArgs = cityIds.stream()
                .map(cityId -> new Object[]{userId, cityId})
                .collect(Collectors.toList());
        jdbcTemplate.batchUpdate(
                "INSERT INTO user_city_drop_progress (user_id, city_id) VALUES (?, ?) "
                        + "ON CONFLICT (user_id, city_id) DO NOTHING",
                batchArgs
        );
    }

    public Map<Long, Long> findLastListenedDropIds(Long userId, List<Long> cityIds) {
        if (cityIds.isEmpty()) {
            return new LinkedHashMap<>();
        }

        String placeholders = cityIds.stream()
                .map(cityId -> "?")
                .collect(Collectors.joining(", "));
        List<Object> parameters = new ArrayList<>();
        parameters.add(userId);
        parameters.addAll(cityIds);

        Map<Long, Long> progressByCityId = new LinkedHashMap<>();
        jdbcTemplate.query(
                "SELECT city_id, last_listened_drop_id FROM user_city_drop_progress "
                        + "WHERE user_id = ? AND city_id IN (" + placeholders + ")",
                resultSet -> {
                    progressByCityId.put(
                            resultSet.getLong("city_id"),
                            resultSet.getLong("last_listened_drop_id")
                    );
                },
                parameters.toArray()
        );
        return progressByCityId;
    }
}

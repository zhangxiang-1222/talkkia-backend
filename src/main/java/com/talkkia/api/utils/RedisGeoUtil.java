package com.talkkia.api.utils;

import com.talkkia.api.constants.NearbyConstant;
import com.talkkia.api.model.SearchResultModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: Ethan
 * @Date: 2024/6/17
 * @Description: This is a utility class for operating GEO in Redis.
 */
@Component
@Slf4j
public class RedisGeoUtil {

    @Resource
    private RedisTemplate redisTemplate;

    /**
     * Adds a point with the given longitude, latitude, and name to the specified key in Redis GEO.
     * <p>
     * GEOADD key longitude latitude member [longitude latitude member ...]
     *
     * @param key       the key under which the point is to be added
     * @param longitude the longitude of the point
     * @param latitude  the latitude of the point
     * @param userId    the name of the point,here we use UserId
     */
    public Boolean addGeoPoint(String key, double longitude, double latitude, Long userId) {
        Point point = new Point(longitude, latitude);
        Long result = redisTemplate.opsForGeo().add(key, point, userId);
        System.out.println("save geo result = " + result);
        return true;
    }

    /**
     * Finds nearby people within the given radius from the specified location.
     * <p>
     * GEORADIUS key longitude latitude radius m|km|ft|mi [WITHCOORD] [WITHDIST] [WITHHASH] [ASC|DESC] [COUNT count] [STORE key] [STORedisT key]
     * Range units: m | km | ft | mi --> meters | kilometers | feet | miles
     * <p>
     * Additional parameters:
     * <p>
     * WITHDIST: Returns the distance between the returned location objects and the center, along with the location objects. The distance unit will match the unit given by the user for the range.
     * WITHCOORD: Returns the longitude and latitude of the location objects.
     * WITHHASH: Returns the geohash-encoded score of the location objects as a 52-bit signed integer. This option is mainly used for underlying applications or debugging and is not commonly used in practice.
     * ASC|DESC: Returns the location objects in order from nearest to farthest | Returns the location objects in order from farthest to nearest.
     * COUNT count: Returns the first N matching location objects. (If not set, returns all matching objects)
     * STORE key: Stores the geographical location information of the return results to the specified key.
     * STOREDIST key: Stores the distance of the return results from the center to the specified key.
     *
     * @param key    the key under which the points are stored
     * @param userId user ID
     * @param radius the radius within which to search for people
     * @return a list of nearby user IDs
     */
    public List<SearchResultModel> findNearbyPeople(
            String key,
            Long userId,
            double radius) {
        // Create the arguments for the geoRadius command
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs();

        // Include the distance in the results
        args = args.includeDistance();
        // Sort from near to far
        args = args.sortAscending();

        // Execute the geoRadius command
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = redisTemplate.opsForGeo()
                .radius(key, userId, new Distance(radius, Metrics.KILOMETERS), args);

//        log.info("Nearby Geo Results:{}", results);

        List<SearchResultModel> locations = new ArrayList<>();
        for (GeoResult geoResult : results.getContent()) {
            Long itemUserId = (Long) ((RedisGeoCommands.GeoLocation) geoResult.getContent()).getName();
            if (!userId.equals(itemUserId)) {
                SearchResultModel searchResultModel = new SearchResultModel();
                searchResultModel.setUserId(itemUserId);
                searchResultModel.setDistance(geoResult.getDistance().getValue());
                locations.add(searchResultModel);
            }
        }
        // Return the results
        return locations;
    }

    /**
     * Deletes a user or entity location from the geo collection in Redis.
     *
     * @param key    the Redis key where the geolocation data is stored
     * @param userId the unique identifier of the user or entity whose data needs to be deleted
     */
    public Boolean deleteGeoLocation(String key, Long userId) {
        redisTemplate.opsForGeo().remove(key, userId);
        return true;
    }

}

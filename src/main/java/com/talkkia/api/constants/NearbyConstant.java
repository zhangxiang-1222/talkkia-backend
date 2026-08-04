package com.talkkia.api.constants;

/**
 * @Author: Ethan
 * @Date: 2024/6/17
 * @Description: Nearby Feature constant class
 */
public class NearbyConstant {

    /**
     * With the given position object as the center, return all position objects whose distance from it does not exceed the given maximum distance.
     * <p>
     * This constant value represents the default given maximum distance,in kilometers
     */
    public static final Integer MAXIMUM_DISTANCE = 25000;

    /**
     * Redis key value
     */
    public static final String REDIS_NEARBY_GEO_KEY = "TALKKIA:NEARBY";

    /**
     * Redis gender key
     */
    public static final String REDIS_NEARBY_GENDER_KEY = "TALKKIA:NEARBY:GENDER:";

    /**
     * 每日申请好友计数 Key 前缀
     * 格式: TALKKIA:NEARBY:REQUEST:COUNT:{userId}:{date}
     * 例如: TALKKIA:NEARBY:REQUEST:COUNT:1234567890:20250109
     */
    public static final String REDIS_NEARBY_REQUEST_COUNT_PREFIX = "TALKKIA:NEARBY:REQUEST:COUNT:";

    /**
     * 每日已申请用户 Set Key 前缀（用于避免重复计数）
     * 格式: TALKKIA:NEARBY:REQUEST:USERS:{userId}:{date}
     * 例如: TALKKIA:NEARBY:REQUEST:USERS:1234567890:20250109
     */
    public static final String REDIS_NEARBY_REQUEST_USERS_PREFIX = "TALKKIA:NEARBY:REQUEST:USERS:";

    /**
     * 每日免费申请次数限制
     */
    public static final Integer DAILY_FREE_REQUEST_LIMIT = 30;

    /**
     * Redis 过期时间（秒）- 48小时
     * 设置为 48 小时以避免时区边界问题
     */
    public static final Long REDIS_NEARBY_EXPIRE_SECONDS = 48 * 60 * 60L;

}

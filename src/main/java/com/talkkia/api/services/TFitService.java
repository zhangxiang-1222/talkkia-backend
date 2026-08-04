package com.talkkia.api.services;

import com.talkkia.api.dto.tfit.TFitIdentifierDTO;
import com.talkkia.api.dto.tfit.TFitStoreDTO;
import com.talkkia.api.dto.tfit.TFitTimeZoneDTO;
import com.talkkia.api.dto.tfit.TFitUserLikesDTO;
import com.talkkia.api.entity.TFit;
import com.talkkia.api.entity.TFitIdentifier;
import com.talkkia.api.entity.TFitUserLikes;
import com.talkkia.api.entity.UserTimeZone;
import com.talkkia.api.enums.TFitIdentifierEnum;
import com.talkkia.api.repositories.*;
import com.talkkia.api.utils.TimeZoneUtil;
import com.talkkia.api.view.UserDTO;
import com.talkkia.api.vo.UserTFitVO;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author: Ethan
 * @Date: 2024/8/9
 * @Description: TFit Service
 */
public interface TFitService {

    /**
     * save Or update user's Time Zone
     *
     * @param tFitTimeZoneDTO
     * @return
     */
    Boolean saveOrUpdateUserTimeZone(TFitTimeZoneDTO tFitTimeZoneDTO);

    /**
     * save Or update TFit
     * @param tFitStoreDTO
     * @return
     */
    Boolean saveOrUpdateTFit(TFitStoreDTO tFitStoreDTO);

    /**
     * store current user's contacts rank to redis
     * @param userId
     */
    void storeContactsTFitsToRedis(Long userId);

    /**
     * save Or Update Identifier
     * if users close TFit,system will clear their data on today
     * @param tFitIdentifierDTO
     * @return
     */
    Boolean saveOrUpdateIdentifier(TFitIdentifierDTO tFitIdentifierDTO);

    /**
     * determine if users open TFit
     * @param userId
     * @return
     */
    Boolean isOpenTFit(Long userId);

    /**
     * like or cancel like steps of someone
     * @param tFitUserLikesDTO
     * @return
     */
    UserTFitVO likeOrCancelLike(TFitUserLikesDTO tFitUserLikesDTO);

    /**
     * get my step log within 30 days
     * @param userId
     * @return
     * @throws ParseException
     */
    List<TFit> getMyStepLog(Long userId) throws ParseException;

    /**
     * get today's rank
     * @param userId
     * @return
     */
    List<UserTFitVO> getTodayStepsRank(Long userId);

    /**
     * If user closes TFit,program will reset(to 0) his steps today.
     * @param userId
     * @return
     */
    Boolean resetTodayStep(Long userId);
}

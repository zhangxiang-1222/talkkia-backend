package com.talkkia.api.services;

import com.talkkia.api.constants.TFitConstant;
import com.talkkia.api.dto.tfit.TFitIdentifierDTO;
import com.talkkia.api.dto.tfit.TFitStoreDTO;
import com.talkkia.api.dto.tfit.TFitTimeZoneDTO;
import com.talkkia.api.dto.tfit.TFitUserLikesDTO;
import com.talkkia.api.entity.*;
import com.talkkia.api.enums.TFitIdentifierEnum;
import com.talkkia.api.repositories.*;
import com.talkkia.api.utils.RedisZSetUtil;
import com.talkkia.api.utils.TimeZoneUtil;
import com.talkkia.api.view.UserDTO;
import com.talkkia.api.view.UserView;
import com.talkkia.api.vo.UserTFitVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: Ethan
 * @Date: 2024/8/8
 * @Description: TFit Service Impl
 */
@Slf4j
@Service
public class TFitServiceImpl implements TFitService {

    @Resource
    private UserTimeZoneRepository userTimeZoneRepository;
    @Resource
    private TFitRepository tFitRepository;
    @Resource
    private TFitIdentifierRepository tFitIdentifierRepository;
    @Resource
    private TFitUserLikesRepository tFitUserLikesRepository;
    @Resource
    private UserRepository userRepository;
    @Resource
    private RedisZSetUtil redisZSetUtil;
    /**
     * must use @Lazy,otherwise an infinite loop injection will occur
     */
    @Lazy
    @Resource
    private TFitService tFitService;

    /**
     * save Or update user's Time Zone
     *
     * @param tFitTimeZoneDTO
     * @return
     */
    @Override
    public Boolean saveOrUpdateUserTimeZone(TFitTimeZoneDTO tFitTimeZoneDTO) {
        Boolean isSuccess = false;
        if (!isOpenTFit(tFitTimeZoneDTO.getUserId())) {
            // false
            return isSuccess;
        }
        String offset = TimeZoneUtil.latLngToOffset(tFitTimeZoneDTO.getLatitude(), tFitTimeZoneDTO.getLongitude());
        UserTimeZone userTimeZone = userTimeZoneRepository.findUserTimeZoneByUserId(tFitTimeZoneDTO.getUserId());
        if (userTimeZone == null) {
            // save ,and if userTimeZone != null execute update
            userTimeZone = new UserTimeZone();
            userTimeZone.setUserId(tFitTimeZoneDTO.getUserId());
        }
        userTimeZone.setOffset(offset);
        UserTimeZone resultUserTimeZone = userTimeZoneRepository.save(userTimeZone);
        if (resultUserTimeZone != null) {
            isSuccess = true;
        }
        log.info("saveOrUpdateTFit:{}",tFitTimeZoneDTO.getUserId());
        return isSuccess;
    }

    /**
     * save Or update TFit
     *
     * @param tFitStoreDTO
     * @return
     */
    @Override
    public Boolean saveOrUpdateTFit(TFitStoreDTO tFitStoreDTO) {
        Boolean isSuccess = false;
        if (!isOpenTFit(tFitStoreDTO.getUserId())) {
            // false
            return isSuccess;
        }
        if (!timeVerify(tFitStoreDTO.getUserId(), tFitStoreDTO.getStepDate())) {
            // false
            return isSuccess;
        }
        // Through time verification, the client time is consistent with the time calculated according to the user's time zone
        TFit tFit = tFitRepository.findTFitByUserIdAndStepDate(tFitStoreDTO.getUserId(), tFitStoreDTO.getStepDate());
        if (tFit == null) {
            // save
            tFit = new TFit();
            tFit.setUserId(tFitStoreDTO.getUserId());
            tFit.setStepDate(tFitStoreDTO.getStepDate());
            tFit.setLikes(0);
            tFit.setSteps(0);
        } else {
            // update steps
            // 0-All 1-Increment
            if(tFitStoreDTO.getAllOrIncrement() == 0){
                tFit.setSteps(tFitStoreDTO.getSteps());
            }else if(tFitStoreDTO.getAllOrIncrement() == 1){
                Integer steps = tFit.getSteps();
                steps = steps + tFitStoreDTO.getSteps();
                tFit.setSteps(steps);
            }
        }
        tFitRepository.save(tFit);
        log.info("saveOrUpdateTFit:{}",tFitStoreDTO.getUserId());
        // must be async,so use a proxy object
        tFitService.storeContactsTFitsToRedis(tFitStoreDTO.getUserId());
        isSuccess = true;
        return isSuccess;
    }

    /**
     * store contacts TFit list to Redis(based on current user's Time Zone)
     *
     * @param userId
     * @throws ParseException
     */
    @Override
    @Async
    public void storeContactsTFitsToRedis(Long userId) {
        List<Long> userContactList = userRepository.findAllByContactsUserId(userId).stream().map(userDTO -> userDTO.getId()).collect(Collectors.toList());
        userContactList.addAll(userRepository.findById(userId).get().getContacts().stream().map(user -> user.getUser().getId()).collect(Collectors.toList()));

        List<TFit> contactsUsersTFit = new ArrayList<>();
        for (Long userContactId : userContactList) {
            // Contact must open TFit
            if (!isOpenTFit(userContactId)) {
                continue;
            }
            // Notice:we should get contactsTFitList by current user's Time Zone
            TFit tFit = tFitRepository.findTFitByUserIdAndStepDate(userContactId, getServerDateByUserId(userId));
            if (tFit != null) {
                contactsUsersTFit.add(tFit);
            }
        }
        // define the key of redis
        String zSetKey = TFitConstant.REDIS_TFIT_RANK_KEY + userId;
        // clear current user's redis data
        redisZSetUtil.deleteZSet(zSetKey);
        // reset current user's redis data
        for (TFit contactTFit : contactsUsersTFit) {
            redisZSetUtil.add(zSetKey, String.valueOf(contactTFit.getUserId()), contactTFit.getSteps());
        }
        // if opened TFit,must include yourself
        if (isOpenTFit(userId)) {
            TFit yourselfTFit = tFitRepository.findTFitByUserIdAndStepDate(userId, getServerDateByUserId(userId));
            redisZSetUtil.add(zSetKey, String.valueOf(yourselfTFit.getUserId()), yourselfTFit.getSteps());
        }
    }

    /**
     * determine if client's time is different from the server time(calculate based on current user's Time Zone)
     *
     * @return
     */
    public Boolean timeVerify(Long userId, String stepDate) {
        Boolean isTheSameDay = false;
        // Users should open Location Permission on their phone
        UserTimeZone userTimeZone = userTimeZoneRepository.findUserTimeZoneByUserId(userId);
        if (userTimeZone == null) {
            return isTheSameDay;
        }
        // calculate Date
        ZoneOffset offset = ZoneOffset.of(userTimeZone.getOffset());
        // Get the current date and time in that offset
        OffsetDateTime offsetDateTime = OffsetDateTime.now(offset);
        // Extract year, month, and day
        int year = offsetDateTime.getYear();
        int month = offsetDateTime.getMonthValue();
        String monthStr = String.valueOf(month);
        if (month < 10) {
            monthStr = "0" + month;
        }
        int day = offsetDateTime.getDayOfMonth();
        String dayStr = String.valueOf(day);
        if (day < 10) {
            dayStr = "0" + day;
        }
        // format:2024-08-09
        String serverDate = year + "-" + monthStr + "-" + dayStr;
        // format:2024-08-09
        String clientDate = stepDate;
        log.info("serverDate:{}", serverDate);
        log.info("clientDate:{}", clientDate);
        if (serverDate != null && clientDate != null) {
            if (serverDate.equals(clientDate)) {
                isTheSameDay = true;
            }
        }
        return isTheSameDay;
    }

    /**
     * get current date on Current User's Time Zone
     *
     * @param userId
     * @return
     */
    public String getServerDateByUserId(Long userId) {
        UserTimeZone userTimeZone = userTimeZoneRepository.findUserTimeZoneByUserId(userId);
        if (userTimeZone == null) {
            throw new NullPointerException("userTimeZone is null!");
        }
        // calculate Date
        ZoneOffset offset = ZoneOffset.of(userTimeZone.getOffset());
        // Get the current date and time in that offset
        OffsetDateTime offsetDateTime = OffsetDateTime.now(offset);
        // Extract year, month, and day
        int year = offsetDateTime.getYear();
        int month = offsetDateTime.getMonthValue();
        String monthStr = String.valueOf(month);
        if (month < 10) {
            monthStr = "0" + month;
        }
        int day = offsetDateTime.getDayOfMonth();
        String dayStr = String.valueOf(day);
        if (day < 10) {
            dayStr = "0" + day;
        }
        // format:2024-08-09
        String serverDate = year + "-" + monthStr + "-" + dayStr;
        return serverDate;
    }

    /**
     * save Or Update Identifier
     * if users close TFit,system will clear their data on today
     *
     * @param tFitIdentifierDTO
     * @return
     */
    @Override
    public Boolean saveOrUpdateIdentifier(TFitIdentifierDTO tFitIdentifierDTO) {
        Boolean isSuccess = false;
        TFitIdentifier tFitIdentifier = tFitIdentifierRepository.findTFitIdentifierByUserId(tFitIdentifierDTO.getUserId());
        if (tFitIdentifier == null) {
            // save
            tFitIdentifier = new TFitIdentifier();
            tFitIdentifier.setUserId(tFitIdentifierDTO.getUserId());
            tFitIdentifier.setIsOpen(TFitIdentifierEnum.OPEN.getCode());
        } else {
            // update
            tFitIdentifier.setUserId(tFitIdentifierDTO.getUserId());
            Integer openNumber = -1;
            if (tFitIdentifierDTO.getIsOpen().equals(TFitIdentifierEnum.OPEN.getCode())) {
                openNumber = TFitIdentifierEnum.OPEN.getCode();
            } else if (tFitIdentifierDTO.getIsOpen().equals(TFitIdentifierEnum.CLOSED.getCode())) {
                openNumber = TFitIdentifierEnum.CLOSED.getCode();
            }
            tFitIdentifier.setIsOpen(openNumber);
        }
        tFitIdentifierRepository.save(tFitIdentifier);
        isSuccess = true;
        log.info("saveOrUpdateIdentifier:{},tFitIdentifier isOpen:{}",tFitIdentifierDTO.getUserId(),tFitIdentifier.getIsOpen());
        return isSuccess;
    }

    /**
     * determine if users open TFit
     *
     * @param userId
     * @return
     */
    @Override
    public Boolean isOpenTFit(Long userId) {
        Boolean isOpen = false;
        TFitIdentifier tFitIdentifier = tFitIdentifierRepository.findTFitIdentifierByUserId(userId);
        if (tFitIdentifier == null) {
            return isOpen;
        }
        if (tFitIdentifier.getIsOpen().equals(TFitIdentifierEnum.OPEN.getCode())) {
            isOpen = true;
        } else if (tFitIdentifier.getIsOpen().equals(TFitIdentifierEnum.CLOSED.getCode())) {
            isOpen = false;
        }
        log.info("isOpenTFit:{},isOpen:{}",userId,isOpen);
        return isOpen;
    }

    /**
     * like or cancel like steps of someone
     *
     * @param tFitUserLikesDTO
     * @return
     */
    @Transactional
    @Override
    public UserTFitVO likeOrCancelLike(TFitUserLikesDTO tFitUserLikesDTO) {
        Long currentUserId = tFitUserLikesDTO.getCurrentUserId();
        Long beLikedUserId = tFitUserLikesDTO.getUserIdLikedByCurrentUserId();
        String stepDate = getServerDateByUserId(tFitUserLikesDTO.getCurrentUserId());
        TFitUserLikes tFitUserLikes = tFitUserLikesRepository.findTFitUserLikesByUserIdAndBeLikedUserIdAndStepDate(currentUserId, beLikedUserId, stepDate);
        UserTFitVO userTFitVO = new UserTFitVO();
        if (tFitUserLikes == null) {
            // like
            tFitUserLikes = new TFitUserLikes();
            tFitUserLikes.setUserId(currentUserId);
            tFitUserLikes.setBeLikedUserId(beLikedUserId);
            tFitUserLikes.setStepDate(stepDate);
            tFitUserLikesRepository.save(tFitUserLikes);
            TFit tFit = tFitRepository.findTFitByUserIdAndStepDate(beLikedUserId, stepDate);
            tFit.setLikes(tFit.getLikes() + 1);
            tFitRepository.save(tFit);
            userTFitVO.setIsLike(true);
            userTFitVO.setLikes(tFit.getLikes());
        } else {
            // cancel like
            tFitUserLikesRepository.delete(tFitUserLikes);
            TFit tFit = tFitRepository.findTFitByUserIdAndStepDate(beLikedUserId, stepDate);
            tFit.setLikes(tFit.getLikes() - 1);
            tFitRepository.save(tFit);
            userTFitVO.setIsLike(false);
            userTFitVO.setLikes(tFit.getLikes());
        }
        log.info("likeOrCancelLike:{}",tFitUserLikesDTO.getCurrentUserId());
        return userTFitVO;
    }

    /**
     * get my step log within 30 days
     *
     * @param userId
     * @return
     * @throws ParseException
     */
    @Override
    public List<TFit> getMyStepLog(Long userId) throws ParseException {
        List<TFit> myStepsLog = tFitRepository.getMyTFitLogWithin30Days(userId);
        List<TFit> dateList = new ArrayList<>(30);

        UserTimeZone userTimeZone = userTimeZoneRepository.findUserTimeZoneByUserId(userId);
        if (userTimeZone == null) {
            throw new NullPointerException("userTimeZone is null!");
        }
        // Specify the time zone offset (e.g., "+08:00")
        ZoneOffset offset = ZoneOffset.of(userTimeZone.getOffset());
        // Get the current date and time in the specified offset
        OffsetDateTime offsetDateTime = OffsetDateTime.now(offset);
        // Iterate through the days from today to one month later
        for (int i = 0; i < 30; i++) {
            OffsetDateTime date = offsetDateTime.minusDays(i);
            // Store the year, month, and day in the list
            int year = date.getYear();
            int month = date.getMonthValue();
            String monthStr = String.valueOf(month);
            if (month < 10) {
                monthStr = "0" + month;
            }
            int day = date.getDayOfMonth();
            String dayStr = String.valueOf(day);
            if (day < 10) {
                dayStr = "0" + day;
            }
            String logDate = year + "-" + monthStr + "-" + dayStr;
            // store TFit
            TFit tFit = new TFit();
            tFit.setUserId(userId);
            tFit.setSteps(0);
            tFit.setLikes(0);
            tFit.setStepDate(logDate);
            dateList.add(tFit);
        }
        // Replace the data in dateList with the data found in the database, and do not change the data not found in the database.
        for (int i = 0; i < dateList.size(); i++) {
            TFit dateTit = dateList.get(i);
            String stepDateStr = dateTit.getStepDate();
            TFit replaceTFit = getTFitByDate(stepDateStr, myStepsLog);
            if (replaceTFit != null) {
                dateList.set(i, replaceTFit);
            }
        }
        log.info("getMyStepLog:{}",userId);
        return dateList;
    }

    public TFit getTFitByDate(String stepDateStr, List<TFit> myStepsLog) {
        TFit result = null;
        for (TFit myStepLog : myStepsLog) {
            String myLogStepDateStr = myStepLog.getStepDate();
            if (myLogStepDateStr != null && myLogStepDateStr.equals(stepDateStr)) {
                result = myStepLog;
                break;
            }
        }
        return result;
    }

    /**
     * get today's rank
     *
     * @param userId
     * @return
     */
    @Override
    public List<UserTFitVO> getTodayStepsRank(Long userId) {
        // delete extra User Time Zone
        List<UserTimeZone> userTimeZoneList = userTimeZoneRepository.findUserTimeZonesByUserId(userId);
        for(int i = 0 ; i < userTimeZoneList.size() - 1 ; i++){
            userTimeZoneRepository.delete(userTimeZoneList.get(i));
        }
        UserTimeZone userTimeZone = userTimeZoneRepository.findUserTimeZoneByUserId(userId);
        if (userTimeZone == null) {
            throw new NullPointerException("userTimeZone is null!");
        }
        // result
        List<UserTFitVO> userTFitVOList = new ArrayList<>();
        // define the key of redis
        String zSetKey = TFitConstant.REDIS_TFIT_RANK_KEY + userId;
        // userId set,order by score desc
        Set<String> userIdSet = redisZSetUtil.revRange(zSetKey, 0, -1);
        // set into list
        for (String contactUserId : userIdSet) {
            Optional<UserView> userById = userRepository.findOneById(Long.valueOf(contactUserId));
            String currentUserDate = getServerDateByUserId(userId);
            TFit tFit = tFitRepository.findTFitByUserIdAndStepDate(Long.valueOf(contactUserId), currentUserDate);
            UserTFitVO userTFitVO = new UserTFitVO();

            TFitUserLikes tFitUserLikes = tFitUserLikesRepository.findTFitUserLikesByUserIdAndBeLikedUserIdAndStepDate(Long.valueOf(userId), Long.valueOf(contactUserId), currentUserDate);
            if (tFit != null) {
                userById.ifPresent(user -> {
                    userTFitVO.setUserId(user.getId());
                    userTFitVO.setUserName(user.getName());
                    userTFitVO.setPicture(user.getPicture());
                    userTFitVO.setSteps(redisZSetUtil.score(zSetKey, String.valueOf(user.getId())).intValue());
                    userTFitVO.setLikes(tFit.getLikes());
                    if (tFitUserLikes == null) {
                        userTFitVO.setIsLike(false);
                    } else {
                        userTFitVO.setIsLike(true);
                    }
                    userTFitVOList.add(userTFitVO);
                });
            }
        }
        log.info("getTodayStepsRank:{}",userId);
        return userTFitVOList;
    }

    /**
     * If user closes TFit,program will reset(to 0) his steps today.
     *
     * @param userId
     * @return
     */
    @Override
    public Boolean resetTodayStep(Long userId) {
        Boolean isSuccess = false;
        UserTimeZone userTimeZone = userTimeZoneRepository.findUserTimeZoneByUserId(userId);
        if (userTimeZone == null) {
            throw new NullPointerException("userTimeZone is null!");
        }
        // get current user's TFit Record by User Time Zone
        TFit tFit = tFitRepository.findTFitByUserIdAndStepDate(userId, getServerDateByUserId(userId));
        if (tFit == null) {
            return isSuccess;
        }
        tFit.setSteps(0);
        tFit.setLikes(0);
        tFitRepository.save(tFit);
        // delete beLiked msg
        List<TFitUserLikes> likedUserIdAndStepDateList = tFitUserLikesRepository.findTFitUserLikesByBeLikedUserIdAndStepDate(userId, getServerDateByUserId(userId));
        log.info("likedUserIdAndStepDateList:{}", likedUserIdAndStepDateList);
        likedUserIdAndStepDateList.forEach(tFitUserLikes -> tFitUserLikesRepository.delete(tFitUserLikes));
        // must be async,so use a proxy object
        tFitService.storeContactsTFitsToRedis(userId);
        isSuccess = true;
        return isSuccess;
    }
}

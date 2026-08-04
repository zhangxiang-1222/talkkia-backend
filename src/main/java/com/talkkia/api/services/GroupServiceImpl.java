package com.talkkia.api.services;

import com.talkkia.api.config.DateTimeConverter;
import com.talkkia.api.entity.Group;
import com.talkkia.api.entity.User;
import com.talkkia.api.relationship.Contact;
import com.talkkia.api.relationship.GroupMember;
import com.talkkia.api.relationship.Type;
import com.talkkia.api.repositories.GroupRepository;
import com.talkkia.api.repositories.UserRepository;
import com.talkkia.api.view.GroupView;
import com.talkkia.api.view.GroupViewShort;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import com.talkkia.api.view.UserView;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.internal.shaded.io.netty.util.internal.ThreadLocalRandom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GroupServiceImpl implements GroupService {

    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    SimpMessagingTemplate simpTemplate;

    Map<String, Object> headers = new HashMap<>();

    @Override
    public GroupView createGroup(Long ownerId, String title, List invitation) {
        try {
            Group group = new Group();
            group.setTitle(title);
            group.setId(setGroupId());
            while (groupRepository.existsById(group.getId())) {
                group.setId(setGroupId());
            }
            System.out.println(invitation);
            User owner = userRepository.findById(ownerId).orElseThrow();
            group.getMembers().add(new GroupMember(Type.OWNER, owner));
            for(Object invitationObj : invitation){
                Long invitationId = Long.valueOf((String)invitationObj);
                User invitationUser = userRepository.findById(invitationId).orElseThrow();
                group.getMembers().add(new GroupMember(Type.MEMBER, invitationUser));
                log.info("Member:",invitationId);
            }

            group.setProvenance("Owner: " + owner.getId() + " : "
                    + new DateTimeConverter().formatDateTime() + ";");
            group.setApproval(Boolean.FALSE);

            Files.createDirectory(Paths.get("upload-dir").toAbsolutePath().resolve(Paths.get(group.getId() + "/")).normalize().toAbsolutePath());
            Files.createDirectory(Paths.get("upload-dir").toAbsolutePath().resolve(Paths.get(group.getId() + "/images")).normalize().toAbsolutePath());
            Files.createDirectory(Paths.get("upload-dir").toAbsolutePath().resolve(Paths.get(group.getId() + "/videos")).normalize().toAbsolutePath());
            Files.createDirectory(Paths.get("upload-dir").toAbsolutePath().resolve(Paths.get(group.getId() + "/audios")).normalize().toAbsolutePath());
            Files.createDirectory(Paths.get("upload-dir").toAbsolutePath().resolve(Paths.get(group.getId() + "/docs")).normalize().toAbsolutePath());

            Map<String, Object> response = new HashMap<>();
            Map<String, Object> groupInvitation = new HashMap<>();
            groupInvitation.put("inviter", ownerId);
            groupInvitation.put("groupid", group.getId());
            groupInvitation.put("title", group.getTitle());
            response.put("success", true);
            response.put("status", "gp02");
            response.put("type", "group");
            response.put("data", groupInvitation);
            invitation.forEach(id -> {
                simpTemplate.convertAndSend("/queue/" + id, response);
            });
            // update by:Ethan date:20240910 desc:If a user has many concact,it will cause infinite loop --start
            Set<GroupMember> groupMembersSet = group.getMembers();
            for (GroupMember member : groupMembersSet) {
                User user = member.getUser();
                user.setContacts(null);
            }

            Set<User> usersSet = group.getJoin_requests();
            for (User user : usersSet) {
                List<Contact> userContacts = user.getContacts();
                for (Contact userContact : userContacts) {
                    User itemUser = userContact.getUser();
                    itemUser.setContacts(null);
                }
            }
            // update by:Ethan date:20240910 desc:If a user has many concact,it will cause infinite loop --end
            groupRepository.save(group);
            log.info("add group:{}", group);
            return groupRepository.findOneById(group.getId()).orElseThrow();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }

    @Override
    public Map joinGroup(Long userId, Long groupId) {
        Map<String, Object> response = new HashMap<>();
        Group group = groupRepository.findById(groupId).orElseThrow();
//        if (group.getMembers().contains(userRepository.findById(userId).get())) {
//            response.put("success", true);
//            response.put("status", "gp05");//already member of a group
//            response.put("type", "group");
//            response.put("message", "You are already member of this group.");
//        }
        if (group.getApproval()) {
            group.getJoin_requests().add(userRepository.findById(userId).get());
            groupRepository.save(group);
            response.put("success", true);
            response.put("status", "gp04");//when group approval is true
            response.put("type", "group");
            response.put("message", "Please wait for admin approval.");
            return response;
        }
        group.getMembers().add(new GroupMember(Type.MEMBER, userRepository.findById(userId).orElseThrow()));
        groupRepository.save(group);
        response.put("success", true);
        response.put("status", "gp03");//group has been joined
        response.put("type", "group");
        response.put("data", groupRepository.findOneById(groupId).orElseThrow());
        return response;
    }

    @Override
    public Boolean leaveGroup(Long memberId, Long groupId) {
//        Group group = groupRepository.findById(groupId).get();
//        System.out.println(group.getMembers().remove(userRepository.findById(memberId).get()));
//        System.out.println(group.getMembers());
        try {
            groupRepository.leaveGroup(memberId, groupId);
            return true;
        } catch (Exception e) {
            return false;
        }
//        groupRepository.save(group);
    }

    @Override
    public String addGroupAdmin(Long ownerId, Long adminId, Long groupId) {
        /*if (groupRepository.findById(Long.parseLong(groupId)).get().getOwner().equals(userRepository.findOneByMobile(mobile1).get())) {
            if (groupRepository.findById(Long.parseLong(groupId)).get().getMembers().contains(userRepository.findOneByMobile(mobile1).get())) {
                groupRepository.findById(Long.parseLong(groupId)).get().getMembers().add(userRepository.findOneByMobile(mobile2).get());
                return userRepository.findOneByMobile(mobile2).get().getName() + " has beed added to admin.";
            }
        }*/
        Group group = groupRepository.findOneByIdAndMembersTypeAndMembersUserId(groupId, Type.OWNER, ownerId).get();
        group.getMembers().stream().filter(gm -> Objects.equals(gm.getUser().getId(), adminId)).findFirst().get().setType(Type.ADMIN);
        System.out.println(group.getMembers());
//group.getAdmins().add(userRepository.findById(adminId).get());
        groupRepository.save(group);
        return "admin has been added";
        //return userRepository.findOneByMobile(mobile2).get().getName() + " can not be added to admin.";
    }

    @Override
    public String removeGroupAdmin(Long ownerId, Long adminId, Long groupId) {
        /*if (groupRepository.findById(Long.parseLong(groupId)).get().getOwner().equals(userRepository.findOneByMobile(mobile1).get())) {
            if (groupRepository.findById(Long.parseLong(groupId)).get().getMembers().contains(userRepository.findOneByMobile(mobile1).get())) {
                groupRepository.findById(Long.parseLong(groupId)).get().getMembers().add(userRepository.findOneByMobile(mobile2).get());
                return userRepository.findOneByMobile(mobile2).get().getName() + " has beed added to admin.";
            }
        }*/
        //Group group = groupRepository.findOneByIdAndOwnerId(groupId, ownerId).get();
        //group.getAdmins().remove(userRepository.findById(adminId).get());
        groupRepository.removeGroupAdmin(adminId, groupId);
        return "admin has been removed.";
        //return userRepository.findOneByMobile(mobile2).get().getName() + " can not be added to admin.";
    }

    @Override
    public String removeGroupMember(Long adminId, Long memberId, Long groupId) {
        groupRepository.leaveGroup(adminId, groupId);
        return "member has been removed.";
    }

    @Override
    public String changeGroupApproval(Long adminId, Long groupId) {
        /*we can also check if the requester is owner or admin of the group
        but since we are using mobile app and we will don't show the changing
        button to other members except admins and owner. So, no need to check 
        this condition here again.*/
        Group group = groupRepository.findById(groupId).get();
        group.setApproval(!group.getApproval());
        groupRepository.save(group);
        return "Group approval has been changed.";
    }


    @Override
    public GroupViewShort getGroup(Long groupId) {
        return groupRepository.findGroupById(groupId).orElseThrow();
    }

    @Override
    public List<GroupView> getMyGroups(Long userId) {
        return groupRepository.findAllByMembersUserId(userId);
    }

    private Long setGroupId() {
        return ThreadLocalRandom.current().nextLong(10000000, 99999999L);
    }

    @Override
    public List<Long> getGroupMembers(Long groupId) {
        log.info("groupId:",groupId);
        List<Long> userIds = new ArrayList<>();
        GroupView groupView = groupRepository.findOneById(groupId).orElseThrow();
        List<com.talkkia.api.view.GroupView.GroupMember> members = groupView.getMembers();
        for(com.talkkia.api.view.GroupView.GroupMember member : members){
            userIds.add(member.getUser().getId());
        }
        return userIds;
    }

    @Override
    public List<User> getGroupMemberUsers(Long groupId) {
        log.info("groupId:",groupId);
        List<User> users = new ArrayList<>();
        GroupView groupView = groupRepository.findOneById(groupId).orElseThrow();
        List<com.talkkia.api.view.GroupView.GroupMember> members = groupView.getMembers();
        for(com.talkkia.api.view.GroupView.GroupMember member : members){
            UserView userView = member.getUser();
            User user = userRepository.findById(userView.getId()).get();
            users.add(user);
        }
        return users;
    }
}

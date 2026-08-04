package com.talkkia.api.services;

import com.talkkia.api.entity.User;
import com.talkkia.api.view.GroupView;
import com.talkkia.api.view.GroupViewShort;
import java.util.List;
import java.util.Map;

public interface GroupService {
    public GroupView createGroup(Long ownerId, String title, List invitation);

    public Map joinGroup(Long userId, Long groupID);

    public Boolean leaveGroup(Long userId, Long groupID);

    public String addGroupAdmin(Long ownerId, Long adminId, Long groupId);

    public String removeGroupAdmin(Long ownerId, Long adminId, Long groupId);
    
    public String removeGroupMember(Long adminId, Long memberId, Long groupId);
    
    public String changeGroupApproval(Long adminId, Long groupId);

    public GroupViewShort getGroup(Long groupId);
    
    public List<GroupView> getMyGroups(Long userId);

    public List<Long> getGroupMembers(Long groupId);

    public List<User> getGroupMemberUsers(Long groupId);
}

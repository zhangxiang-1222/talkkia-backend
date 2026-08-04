package com.talkkia.api.view;

import com.talkkia.api.relationship.Type;
import java.util.List;

public interface GroupView {

Long getId();

String getTitle();

String getPicture();

String getNotice();

Boolean getApproval();

List<GroupMember> getMembers();

interface GroupMember {

        Type getType();

        UserView getUser();
    }
}

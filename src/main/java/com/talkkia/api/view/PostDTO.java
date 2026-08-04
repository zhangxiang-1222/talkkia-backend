package com.talkkia.api.view;

import lombok.Value;

@Value
public class PostDTO {

Long id;
Long postCreator;
String postText;
String[] postPicVid;
String dateTime;
int totalLikes;
int totalDislikes;
int totalComments;
}

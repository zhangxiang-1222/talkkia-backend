 package com.talkkia.api.entity;

import com.talkkia.api.relationship.Contact;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

/**
 *
 * @author noor
 */
@Node
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class User {

    @Id
    private Long id;
    private String name;
    private String dob;
    private String gender;
    private String mobile;
    private String email;
    private String firebaseUid;
    private String socialProvider;
    private String providerUserId;
    private String region;
    private String picture;
    private String registrationDate;
    @Relationship(type = "CONTACT")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<Contact> contacts = new ArrayList<>();
//    @Relationship(type = "HIDEFROM")
//    @ToString.Exclude
//    @EqualsAndHashCode.Exclude
//    List<User> hidefrom = new ArrayList<>();
//    @Relationship(type = "HIDETHEIR")
//    @ToString.Exclude
//    @EqualsAndHashCode.Exclude
//    List<User> hidetheir = new ArrayList<>();
}

package com.pitayafruits.pojo.bo;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;


@Data
@ToString
@AllArgsConstructor
public class NewFriendRequestBo {

    @NotBlank
    private String myId;

    @NotBlank
    private String friendId;

    @NotBlank
    private String verifyMessage;

    private String friendRemark;

}

package com.pitayafruits.pojo.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
public class RegistLoginBo {

    private String mobile;

    private String smsCode;
}

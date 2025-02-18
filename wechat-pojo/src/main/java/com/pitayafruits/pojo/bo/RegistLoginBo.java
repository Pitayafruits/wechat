package com.pitayafruits.pojo.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

@Data
@ToString
@AllArgsConstructor
public class RegistLoginBo {

    @NotBlank(message = "手机号不能为空")
    @Length(min = 11, max = 11, message = "手机号长度必须为11位")
    private String mobile;

    @NotBlank(message = "验证码不能为空")
    private String smsCode;

    private String nickname;
}

package cn.cetron.vote.entity;

import lombok.Data;

@Data
public class AccessToken {
    // 获取到的凭证
    private String access_token;

    // 凭证有效时间，单位：秒
    private int expires_in;
}

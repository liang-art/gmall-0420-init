package com.atguigu.gmall.auth;

import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.common.utils.RsaUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

public class JwtTest {

    // 别忘了创建D:\\project\rsa目录
    //D:\\AtGuiGuNeiNet\\峰哥主讲2\\project-0420
	private static final String pubKeyPath = "D:\\AtGuiGuNeiNet\\峰哥主讲2\\rsa\\rsa.pub";
    private static final String priKeyPath = "D:\\AtGuiGuNeiNet\\峰哥主讲2\\rsa\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "#$#332#$sfsdSDSD%");
    }

    @BeforeEach //:生成密钥前，把它注释掉。等到生成密钥后再解开
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("id", "11");
        map.put("username", "liuyan");
        // 生成token
        String token = JwtUtils.generateToken(map, privateKey, 1);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6IjExIiwidXNlcm5hbWUiOiJsaXV5YW4iLCJleHAiOjE2MDI2NDcwNjB9.hl-IKZ6UQUUe684U2E4_gDXd3H2PHRMmUsIVOXCzC2nPTtSKydzU-3IE62vvzxgiaoAcsU8uTQ1yPkWb-gd6AeQEF3jS0xa3RBxu-6DmNlHAKFIyWKxxBu69Dl2XvqQza3DUN9YZe8LguamCeymgQ9UyUGVaBCkv83VY0RzDyz6xZpQuNZ0yHGy8s4FUBpk0NSqH0ucZ_kpj8cn-mB68_J3ty6RpHMye_olbBex9CROHy0oKlHVx2oSrKpnNIlPZVKQFnpvS7ruurpGp7UpLdFG9l8c2Y8B9h11DirlFiqeCypunFrtXhyeeeEJdvYVqlNuDeZJ7CH-5i6OfzLNC8Q";

        // 解析token
        Map<String, Object> map = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + map.get("id"));
        System.out.println("userName: " + map.get("username"));
    }
}

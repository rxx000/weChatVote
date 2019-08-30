package cn.cetron.vote.utils;

import lombok.extern.java.Log;
import org.springframework.stereotype.Component;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.util.Base64;

@Component
@Log
public class DesUtil {

    /** 十六进制下数字到字符的映射数组 */
    private final static String[] hexDigits = { "0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };

    private static  Cipher cipher = null;
    private static  Key key = null;


    public static void getCipher(){
        if(key!=null||cipher!=null){
            return;
        }
        try {
            //1.生成KEY
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(new SecureRandom());
            SecretKey secretKey = keyGenerator.generateKey();
            byte[] byteKey = secretKey.getEncoded();
            //2.转换KEY
             key = new SecretKeySpec(byteKey,"AES");
            //3.加密
            cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        } catch (NoSuchAlgorithmException e) {
            log.info("解码失败");
        } catch (NoSuchPaddingException e) {
            log.info("解码失败");
        }
    }


    public static String enCode(String code)throws Exception{
        getCipher();
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] result = cipher.doFinal(code.getBytes());
        return Base64.getEncoder().encodeToString(result);
    }

    public static String deCode(String code)throws Exception{
        getCipher();
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] result = cipher.doFinal(Base64.getDecoder().decode(code));
        return new String(result);
    }
}

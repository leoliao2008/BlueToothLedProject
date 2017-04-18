package csh.tiro.cc;

/**
 * Created by 64217 on 2017/4/10.
 */

public class aes {
    static{
        System.loadLibrary("AES");
    }
    //使用内置密码
    public static native void keyExpansionDefault();
    //带密约初始化
    public static native void keyExpansion(byte[] key);
    //encryption
    public static native void cipher(byte[] in, byte[] out);
    //decryption
    public static native void invCipher(byte[] in, byte[] out);
}

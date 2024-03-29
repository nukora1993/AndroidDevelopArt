package com.example.chapter2.binderpool;

import android.os.RemoteException;

public class SecurityCenterImpl extends ISecurityCenter.Stub{
    private static final char SERET_CODE='^';

    @Override
    public String encrypt(String content) throws RemoteException {
        char[] chars=content.toCharArray();
        for (int i = 0; i <chars.length ; i++) {
            chars[i]^=SERET_CODE;
        }
        return new String(chars);
    }

    @Override
    public String decrypt(String password) throws RemoteException {
        return encrypt(password);
    }


}

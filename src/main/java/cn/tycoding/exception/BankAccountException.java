package cn.tycoding.exception;

/**
 * 2 * @Author: qlXie
 * 3 * @Date: 2019-07-02 11:09
 * 4
 */
public class BankAccountException extends RuntimeException{
    public BankAccountException(String message){
        super(message);
    }
}

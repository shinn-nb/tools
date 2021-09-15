package com.snn.tools.pomconfig.exception;

/**
 * @Author: shinn
 * @Date: 2021/9/15 上午9:19 （日期和时间）
 */
public class MavenException extends Exception{
    public MavenException(String msg){
        super(msg);
    }
    public MavenException(Exception e){
        super(e);
    }
}

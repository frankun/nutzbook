package com.frankun.nutzbook.module;

import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.lang.random.R;

import com.frankun.nutzbook.service.EmailService;
import com.sun.org.apache.regexp.internal.recompile;

public abstract class BaseModule {

	/** 注入同名的一个ioc对象 */
    @Inject 
    protected Dao dao;
    
    @Inject 
    protected EmailService emailService;
    
    protected byte[] emailKEY = R.sg(24).next().getBytes();
}

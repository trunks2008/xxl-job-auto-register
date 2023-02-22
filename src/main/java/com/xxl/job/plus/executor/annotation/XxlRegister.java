package com.xxl.job.plus.executor.annotation;

import com.xxl.job.core.glue.GlueTypeEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface XxlRegister {

    String cron();

    String jobDesc() default "default jobDesc";

    String alarmEmail() default "";

    String author() default "default Author";

    /**
     * com.xxl.job.core.glue.GlueTypeEnum.BEAN.name()
     *
     * @return
     */
    String glueType() default "BEAN";

    /**
     * GLUE源代码
     */
    String glueSource() default "";

    /**
     * GLUE源代码文件路径
     */
    String glueSourceFilePath() default "";

    /**
     * GLUE备注，前端要求，大于4位
     */
    String glueRemark() default "";

    String executorParam() default "";

    /*
     * 默认为 ROUND 轮询方式
     * 可选： FIRST 第一个
     *       SHARDING_BROADCAST 分片广播
     * */
    String executorRouteStrategy() default "ROUND";

    int triggerStatus() default 0;
}

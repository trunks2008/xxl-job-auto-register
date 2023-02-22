package com.xxl.job.plus.executor.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.xxl.job.core.glue.GlueTypeEnum;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.plus.executor.annotation.XxlRegister;
import com.xxl.job.plus.executor.model.XxlJobGroup;
import com.xxl.job.plus.executor.model.XxlJobInfo;
import com.xxl.job.plus.executor.service.JobGroupService;
import com.xxl.job.plus.executor.service.JobInfoService;
import com.xxl.job.plus.executor.service.impl.RegisterSwitch;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author : Hydra
 * @date: 2022/9/20 9:57
 * @version: 1.0
 */
@Component
public class XxlJobAutoRegister implements ApplicationListener<ApplicationReadyEvent>,
        ApplicationContextAware {

    private static final Log log =LogFactory.get();

    private ApplicationContext applicationContext;

    @Autowired
    private JobGroupService jobGroupService;

    @Autowired
    private JobInfoService jobInfoService;

    @Autowired
    private RegisterSwitch registerSwitch;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext=applicationContext;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (registerSwitch.isAutoRegisterOpen()) {
            //注册执行器
            addJobGroup();
            //注册任务
            addJobInfo();
        }
    }

    //自动注册执行器
    private void addJobGroup() {
        if (jobGroupService.preciselyCheck())
            return;

        if(jobGroupService.autoRegisterGroup())
            log.info("auto register xxl-job group success!");
    }

    private void addJobInfo() {
        List<XxlJobGroup> jobGroups = jobGroupService.getJobGroup();
        XxlJobGroup xxlJobGroup = jobGroups.get(0);

        String[] beanDefinitionNames = applicationContext.getBeanNamesForType(Object.class, false, true);
        for (String beanDefinitionName : beanDefinitionNames) {
            Object bean = applicationContext.getBean(beanDefinitionName);

            Map<Method, XxlJob> annotatedMethods  = MethodIntrospector.selectMethods(bean.getClass(),
                    new MethodIntrospector.MetadataLookup<XxlJob>() {
                        @Override
                        public XxlJob inspect(Method method) {
                            return AnnotatedElementUtils.findMergedAnnotation(method, XxlJob.class);
                        }
                    });
            for (Map.Entry<Method, XxlJob> methodXxlJobEntry : annotatedMethods.entrySet()) {
                Method executeMethod = methodXxlJobEntry.getKey();
                XxlJob xxlJob = methodXxlJobEntry.getValue();

                //自动注册
                if (executeMethod.isAnnotationPresent(XxlRegister.class)) {
                    XxlRegister xxlRegister = executeMethod.getAnnotation(XxlRegister.class);
                    List<XxlJobInfo> jobInfo = jobInfoService.getJobInfo(xxlJobGroup.getId(), xxlJob.value());
                    if (!jobInfo.isEmpty()){
                        //因为是模糊查询，需要再判断一次
                        Optional<XxlJobInfo> first = jobInfo.stream()
                                .filter(xxlJobInfo -> xxlJobInfo.getExecutorHandler().equals(xxlJob.value()))
                                .findFirst();
                        if (first.isPresent()) {
                            XxlJobInfo originXxlJobInfo = first.get();
                            XxlJobInfo xxlJobInfo = createXxlJobInfo(xxlJobGroup, xxlJob, xxlRegister);
                            xxlJobInfo.setId(originXxlJobInfo.getId());
                            jobInfoService.updateJobInfo(xxlJobInfo);
                            if (GlueTypeEnum.BEAN != GlueTypeEnum.match(xxlJobInfo.getGlueType())) {
                                String glueSource = getGlueSource(xxlRegister);
                                jobInfoService.saveJobCode(xxlJobInfo, glueSource);
                            }
                            continue;
                        }
                    }

                    XxlJobInfo xxlJobInfo = createXxlJobInfo(xxlJobGroup, xxlJob, xxlRegister);
                    Integer jobInfoId = jobInfoService.addJobInfo(xxlJobInfo);
                    xxlJobInfo.setId(jobInfoId);
                    if (GlueTypeEnum.BEAN != GlueTypeEnum.match(xxlJobInfo.getGlueType())) {
                        String glueSource = getGlueSource(xxlRegister);
                        jobInfoService.saveJobCode(xxlJobInfo, glueSource);
                    }
                }
            }
        }
    }

    private String getGlueSource(XxlRegister xxlRegister) {
        if (StringUtils.isNotBlank(xxlRegister.glueSourceFilePath()) &&
                StringUtils.isBlank(xxlRegister.glueSource())) {
            FileReader fileReader = new FileReader(xxlRegister.glueSourceFilePath()
                    , "UTF-8");
            return fileReader.readString();
        } else {
            return xxlRegister.glueSource();
        }
    }

    private XxlJobInfo createXxlJobInfo(XxlJobGroup xxlJobGroup, XxlJob xxlJob, XxlRegister xxlRegister){
        XxlJobInfo xxlJobInfo=new XxlJobInfo();
        xxlJobInfo.setJobGroup(xxlJobGroup.getId());
        xxlJobInfo.setJobDesc(xxlRegister.jobDesc());
        xxlJobInfo.setAuthor(xxlRegister.author());
        xxlJobInfo.setScheduleType("CRON");
        xxlJobInfo.setScheduleConf(xxlRegister.cron());
        xxlJobInfo.setExecutorHandler(xxlJob.value());
        xxlJobInfo.setExecutorParam(xxlRegister.executorParam());
        xxlJobInfo.setExecutorRouteStrategy(xxlRegister.executorRouteStrategy());
        xxlJobInfo.setMisfireStrategy("DO_NOTHING");
        xxlJobInfo.setExecutorBlockStrategy("SERIAL_EXECUTION");
        xxlJobInfo.setExecutorTimeout(0);
        xxlJobInfo.setExecutorFailRetryCount(0);
        xxlJobInfo.setGlueRemark("GLUE代码初始化");
        xxlJobInfo.setTriggerStatus(xxlRegister.triggerStatus());
        xxlJobInfo.setAlarmEmail(xxlRegister.alarmEmail());

        xxlJobInfo.setGlueType(xxlRegister.glueType());
        xxlJobInfo.setGlueSource(xxlRegister.glueSource());
        xxlJobInfo.setGlueRemark(xxlRegister.glueRemark());

        return xxlJobInfo;
    }

}

## xxljob-autoregister-spring-boot-starter

**********************************

自动注册xxl-job执行器以及任务

## 1、打包

```
mvn clean install
```

## 2、项目中引入

```xml
<dependency>
    <groupId>com.cn.hydra</groupId>
    <artifactId>xxljob-autoregister-spring-boot-starter</artifactId>
    <version>0.0.1</version>
</dependency>
```

## 3、配置

springboot项目配置文件application.properties：

```properties
server.port=8082

# 原生xxl-job配置
xxl.job.admin.addresses=http://127.0.0.1:8080/xxl-job-admin
xxl.job.accessToken=default_token
xxl.job.executor.appname=xxl-job-executor-test
xxl.job.executor.address=
xxl.job.executor.ip=127.0.0.1
xxl.job.executor.port=9999
xxl.job.executor.logpath=/data/applogs/xxl-job/jobhandler
xxl.job.executor.logretentiondays=30

# 新增配置项，必须项
# admin用户名
xxl.job.admin.username=admin
# admin 密码
xxl.job.admin.password=123456
# 执行器名称
xxl.job.executor.title=Exe-Titl

# 新增配置项，可选项
# 执行器地址类型：0=自动注册、1=手动录入，默认为0
xxl.job.executor.addressType=1
# 在上面为1的情况下，手动录入执行器地址列表，多地址逗号分隔
xxl.job.executor.addressList=http://127.0.0.1:9999
```

`XxlJobSpringExecutor`参数配置与之前相同

## 4、添加注解
需要自动注册的方法添加注解`@XxlRegister`，不加则不会自动注册

```java
@Service
public class TestService {

    @XxlJob(value = "testJob")
    @XxlRegister(cron = "0 0 0 * * ? *",
            author = "hydra",
            jobDesc = "测试job")
    public void testJob(){
        System.out.println("#公众号：码农参上");
    }


    @XxlJob(value = "testJob222")
    @XxlRegister(cron = "59 1-2 0 * * ?",
            triggerStatus = 1)
    public void testJob2(){
        System.out.println("#作者：Hydra");
    }

    @XxlJob(value = "testJob444")
    @XxlRegister(cron = "59 59 23 * * ?")
    public void testJob4(){
        System.out.println("hello xxl job");
    }
}
```

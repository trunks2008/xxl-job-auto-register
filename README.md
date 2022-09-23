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

# 新增配置项
# admin用户名
xxl.job.admin.username=admin 
# admin 密码
xxl.job.admin.password=123456
# 执行器名称
xxl.job.executor.title=Exe-Titl
```


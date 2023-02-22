package com.xxl.job.plus.executor.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RegisterSwitch {

    private static final String SWITCH_OPEN = "OPEN";

    private static final String SWITCH_CLOSE = "CLOSE";

    @Value("${xxl.job.auto.register:CLOSE}")
    private String registerSwitch;

    public boolean isAutoRegisterOpen() {
        return registerSwitch.equals(SWITCH_OPEN);
    }
}

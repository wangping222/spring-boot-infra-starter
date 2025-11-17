package com.qbit.framework.business.service.starter.context;

import com.qbit.framework.business.service.starter.AppType;
import lombok.experimental.UtilityClass;

import java.util.concurrent.atomic.AtomicReference;

@UtilityClass
public class AppTypeContext {

    private static final AtomicReference<AppType> APP_TYPE = new AtomicReference<>();


    public static void setAppType(AppType type) {
        if (APP_TYPE.get() == null) {
            APP_TYPE.set(type);
        }
    }

    public static AppType appType() {
        return APP_TYPE.get();
    }
}

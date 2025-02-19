package com.lw.fx.client.request.feign.interceptor;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.lw.fx.client.domain.AjaxResult;
import de.saxsys.mvvmfx.MvvmFX;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.Charset;


public class OkHttpInterceptor implements Interceptor {
    private static final Charset UTF8 = Charset.forName("UTF-8");

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        Request originalRequest = chain.request();
        Response response = null;
        try {
            response = chain.proceed(originalRequest);
        } catch (IOException e) {

            throw new RuntimeException(e);
        }

        MediaType mediaType = response.body().contentType();
        String content = null;
        try {
            content = response.body().string();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        JSONObject jsonObject = JSONUtil.parseObj(content);
        int code = jsonObject.getInt(AjaxResult.CODE_TAG);
        String msg = jsonObject.getStr(AjaxResult.MSG_TAG);


        Response responseNew = response.newBuilder().
                body(ResponseBody.create(mediaType, content))
                .build();
        if (!"GET".equalsIgnoreCase(originalRequest.method())) {
            messageProcess(code, msg);
        } else {
            messageProcess(code, msg);
        }

        //生成新的response返回，网络请求的response如果取出之后，直接返回将会抛出异常
        return responseNew;
    }

    public OkHttpInterceptor() {
    }

    /**
     * @Description: 下面的注释为通过response自定义code来标示请求状态，当code返回如下情况为权限有问题，登出并返回到登录页
     * * 如通过xmlhttprequest 状态码标识 逻辑可写在下面error中
     * @param: [res]
     * @return: void
     * @auther: liwen
     * @date: 2018/11/6 12:59 PM
     */
    private void messageProcess(int code, String msg) throws IOException {


        if (code != 200) {

            MvvmFX.getNotificationCenter().publish("message", code, msg);
            if (code == 401) {
                MvvmFX.getNotificationCenter().publish("showLoginRegister");
            }
        }
    }


}
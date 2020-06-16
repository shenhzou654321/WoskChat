package com.deep.netdeep.view.dialog;

import com.deep.dpwork.annotation.DpLayout;
import com.deep.dpwork.util.DBUtil;
import com.deep.dpwork.util.Lag;
import com.deep.dpwork.util.ToastUtil;
import com.deep.netdeep.R;
import com.deep.netdeep.base.TDialogScreen;
import com.deep.netdeep.bean.UserLoginEventBean;
import com.deep.netdeep.core.CoreApp;
import com.deep.netdeep.event.LoginSuccessEvent;
import com.deep.netdeep.net.bean.BaseEn;
import com.deep.netdeep.net.bean.TokenBean;
import com.deep.netdeep.socket.WebSocketUtil;
import com.deep.netdeep.socket.WsListener;
import com.deep.netdeep.util.WebChatUtil;
import com.prohua.dove.Dove;
import com.prohua.dove.Dover;

import org.greenrobot.eventbus.EventBus;

import io.reactivex.disposables.Disposable;

@DpLayout(R.layout.login_loading_dialog_screen)
public class LoginLoadingDialogScreen extends TDialogScreen implements WsListener {

    @Override
    public void init() {

        WebSocketUtil.get().addListener(LoginLoadingDialogScreen.this);

        Dove.flyLifeOnlyNet(CoreApp.jobTask.login(((UserLoginEventBean) baseData).username, ((UserLoginEventBean) baseData).password),
                new Dover<BaseEn<TokenBean>>() {
                    @Override
                    public void don(Disposable d, BaseEn<TokenBean> loginBeanBaseEn) {
                        Lag.i(loginBeanBaseEn.msg);
                        if (loginBeanBaseEn.code == 200) {
                            CoreApp.appBean.userBean.token = loginBeanBaseEn.data.token;
                            TokenBean tokenBean = new TokenBean();
                            tokenBean.token = loginBeanBaseEn.data.token;
                            Dove.addGlobalHeader("token", CoreApp.appBean.userBean.token);
                            CoreApp.appBean.userBean.userName = ((UserLoginEventBean) baseData).username;
                            CoreApp.appBean.userBean.passWord = ((UserLoginEventBean) baseData).password;
                            DBUtil.save(CoreApp.appBean);
                            WebSocketUtil.get().connect();
                        } else {
                            ToastUtil.showError(loginBeanBaseEn.msg);
                            EventBus.getDefault().post(new LoginSuccessEvent());
                            LoginLoadingDialogScreen.this.closeEx();
                        }
                    }

                    @Override
                    public void die(Disposable d, Throwable throwable) {
                        ToastUtil.showError("NetWork is Error");
                        LoginLoadingDialogScreen.this.closeEx();
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        WebSocketUtil.get().removeListener(LoginLoadingDialogScreen.this);
    }

    @Override
    public void connected() {
        ToastUtil.showSuccess("Login success");
        WebChatUtil.put(10000, null);
    }

    @SuppressWarnings("all")
    @Override
    public void msg(String text) {
        Lag.i("接收到消息:" + text);
        WebChatUtil.get(text, new WebChatUtil.EndListener() {
            @Override
            public void end() {
                EventBus.getDefault().post(new LoginSuccessEvent());
                LoginLoadingDialogScreen.this.closeEx();
            }
        });
    }

    @Override
    public void disconnected() {
        CoreApp.appBean.userBean.token = null;
        DBUtil.save(CoreApp.appBean);
        ToastUtil.showError("Failed to connect to server");
        //EventBus.getDefault().post(new LoginSuccessEvent());
        LoginLoadingDialogScreen.this.closeEx();
    }

    @Override
    public void failed() {
        CoreApp.appBean.userBean.token = null;
        DBUtil.save(CoreApp.appBean);
        ToastUtil.showError("Failed to connect to server");
        //EventBus.getDefault().post(new LoginSuccessEvent());
        LoginLoadingDialogScreen.this.closeEx();
    }
}
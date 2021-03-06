package com.deep.netdeep.view;

import android.annotation.SuppressLint;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.deep.dpwork.annotation.DpLayout;
import com.deep.dpwork.annotation.DpStatus;
import com.deep.dpwork.util.DTimeUtil;
import com.deep.dpwork.util.SoftListenerUtil;
import com.deep.netdeep.R;
import com.deep.netdeep.base.TBaseScreen;
import com.deep.netdeep.bean.UserLoginEventBean;
import com.deep.netdeep.event.LoginSuccessEvent;
import com.deep.netdeep.util.TouchExt;
import com.deep.netdeep.view.dialog.LoginLoadingDialogScreen;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Objects;

import butterknife.BindView;

@DpStatus(blackFont = true)
@DpLayout(R.layout.login_screen)
public class LoginScreen extends TBaseScreen {

    @BindView(R.id.backLin)
    ConstraintLayout backLin;
    @BindView(R.id.centerBottom)
    LinearLayout centerBottom;
    @BindView(R.id.userEdit)
    EditText userEdit;
    @BindView(R.id.passwordEdit)
    EditText passwordEdit;
    @BindView(R.id.loginBt)
    LinearLayout loginBt;
    @BindView(R.id.signTouch)
    TextView signTouch;
    @BindView(R.id.backTouch)
    ImageView backTouch;

    private ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void init() {

        loginBt.setOnTouchListener((v, event) ->
                TouchExt.alpTouch(v, event, () ->
                        LoginLoadingDialogScreen
                                .prepare(LoginLoadingDialogScreen.class,
                                        new UserLoginEventBean(userEdit.getText().toString(), passwordEdit.getText().toString()))
                                .open(fragmentManager())));

        signTouch.setOnTouchListener((v, event) -> TouchExt.alpTouch(v, event, () -> open(SignUpScreen.class)));

        backTouch.setOnTouchListener((v, event) -> TouchExt.alpTouch(v, event, this::closeEx));

        //InputManagerUtil.showSoftInputFromWindow(Objects.requireNonNull(getActivity()), userEdit);

        onGlobalLayoutListener = SoftListenerUtil.listener(Objects.requireNonNull(getActivity()), backLin, i -> {
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) centerBottom.getLayoutParams();
            layoutParams.bottomMargin = i;
            centerBottom.setLayoutParams(layoutParams);
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        backLin.getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(LoginSuccessEvent event) {
        DTimeUtil.run(300, this::close);
    }
}

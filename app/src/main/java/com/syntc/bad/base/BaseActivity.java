package com.syntc.bad.base;

import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.syntc.bad.AppManager;
import com.syntc.bad.R;
import com.syntc.bad.dialog.DialogControl;
import com.syntc.bad.dialog.DialogHelp;
import com.umeng.analytics.MobclickAgent;

public abstract class BaseActivity extends AppCompatActivity implements DialogControl{

    protected LayoutInflater mInflater;
    public boolean _isVisible ;
    protected String pageName = getClass().getSimpleName();
    private ProgressDialog _waitDialog;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initSystemBarTint();

        AppManager.getInstance().addActivity(this);

        mInflater = getLayoutInflater();
        if (getLayoutId() != 0) {
            setContentView(getLayoutId());
        }
        initView();
        initData();

    }


    protected abstract int getLayoutId();

    protected abstract void initView();

    protected abstract void initData();

    public void initSystemBarTint() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true); //设置状态栏为透明的
            setStatusBarTintColor(R.color.colorPrimary);  //设置状态栏颜色
        }
    }

    /**
     *
     * @param toolbar
     * @param homeAsUpEnabled 同下
     * @param title 标题字符串
     */
    public void initToolBar(Toolbar toolbar, boolean homeAsUpEnabled, String title) {
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(homeAsUpEnabled);
        }
    }

    /**
     * 子类不需要重写，直接调用
     * @param toolbar
     * @param homeAsUpEnabled 给Toolbar加上一个返回的按钮，true 有小箭头，并且图标可以点击  false 没有小箭头，并且图标不可以点击
     * @param resTitle  string.xml 资源id
     */
    public void initToolBar(Toolbar toolbar, boolean homeAsUpEnabled, int resTitle) {
        initToolBar(toolbar, homeAsUpEnabled, getString(resTitle));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: //令子Activity initToolbar()之后，点击左上方按钮有效返回
                onBackPressed();
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }




    //设置状态栏为透明的
    private void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }


    // 设置状态栏颜色
    public void setStatusBarTintColor(int resId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { // 19
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintColor(getResources().getColor(resId));
        }
    }


    @Override
    public ProgressDialog showWaitDialog() {
        return showWaitDialog(R.string.loading);
    }

    @Override
    public ProgressDialog showWaitDialog(int resid) {
        return showWaitDialog(getString(resid));
    }

    @Override
    public ProgressDialog showWaitDialog(String message) {
        if (_isVisible) {
            if (_waitDialog == null) {
                _waitDialog = DialogHelp.getWaitDialog(this, message);
            }
            if (_waitDialog != null) {
                _waitDialog.setMessage(message);
                _waitDialog.show();
            }
            return _waitDialog;
        }
        return null;
    }

    @Override
    public void hideWaitDialog() {
        if (_isVisible && _waitDialog != null) {
            try {
                _waitDialog.dismiss();
                _waitDialog = null;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        _isVisible = true;
        MobclickAgent.onPageStart(pageName); //统计页面(仅有Activity的应用中SDK自动调用，不需要单独写。"SplashScreen"为页面名称，可自定义)
        MobclickAgent.onResume(this);          //统计时长

    }

    @Override
    protected void onPause() {
        super.onPause();
        _isVisible = false;

        MobclickAgent.onPageEnd(pageName); // （仅有Activity的应用中SDK自动调用，不需要单独写）保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息。"SplashScreen"为页面名称，可自定义
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppManager.getInstance().finishActivity(this);
    }
}

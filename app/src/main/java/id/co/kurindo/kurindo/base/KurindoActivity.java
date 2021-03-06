package id.co.kurindo.kurindo.base;

import android.os.Bundle;
import android.support.v7.app.ActionBar;

import id.co.kurindo.kurindo.R;
import id.co.kurindo.kurindo.helper.SQLiteHandler;
import id.co.kurindo.kurindo.helper.SessionManager;

/**
 * Created by DwiM on 12/14/2016.
 */

public abstract class KurindoActivity extends BaseActivity {
    private Class fragmentClass;
    protected SQLiteHandler db;
    protected SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayout());

        db = new SQLiteHandler(this);
        session = new SessionManager(this);
        Class classZ = getFragmentClass();
        if(classZ != null) showFragment(classZ, getBundleParams(), getContainer());
    }

    @Override
    public boolean providesActivityToolbar() {
        return true;
    }

    public int getLayout() {
        return R.layout.layout_base;
    }

    public abstract Class getFragmentClass();

    @Override
    protected ActionBar setupToolbar() {
        ActionBar ab = super.setupToolbar();
        if(ab != null) ab.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_18dp);
        return  ab;
    }

    public abstract Bundle getBundleParams();

    public int getContainer() {
        return R.id.containerView;
    }
}

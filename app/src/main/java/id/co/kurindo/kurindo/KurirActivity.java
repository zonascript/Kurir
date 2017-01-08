package id.co.kurindo.kurindo;

import android.os.Bundle;

import id.co.kurindo.kurindo.base.KurindoActivity;

/**
 * Created by DwiM on 12/12/2016.
 */

public class KurirActivity extends KurindoActivity{

    @Override
    public boolean providesActivityToolbar() {
        return true;
    }

    @Override
    public Class getFragmentClass() {
        return KurirTabFragment.class;
    }

    @Override
    public Bundle getBundleParams() {
        return getIntent().getExtras();
    }
}

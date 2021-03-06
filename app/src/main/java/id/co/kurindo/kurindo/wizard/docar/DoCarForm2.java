package id.co.kurindo.kurindo.wizard.docar;

/**
 * Created by aspire on 3/26/2017.
 */

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stepstone.stepper.Step;
import com.stepstone.stepper.VerificationError;

import id.co.kurindo.kurindo.R;
import id.co.kurindo.kurindo.wizard.BaseStepFragment;

/**
 * Created by dwim on 3/15/2017.
 */

public class DoCarForm2 extends BaseStepFragment implements Step {
    private static final String TAG = "DoCarForm1";
    VerificationError invalid = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflateAndBind(inflater, container, R.layout.fragment_docar1);
        return v;
    }

    @Override
    public int getName() {
        return R.string.docar_form;
    }

    @Override
    public VerificationError verifyStep() {
        return null;
    }

    @Override
    public void onSelected() {

    }

    @Override
    public void onError(@NonNull VerificationError error) {

    }
}
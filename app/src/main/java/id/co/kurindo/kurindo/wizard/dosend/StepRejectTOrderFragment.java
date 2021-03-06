package id.co.kurindo.kurindo.wizard.dosend;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.stepstone.stepper.Step;
import com.stepstone.stepper.VerificationError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import id.co.kurindo.kurindo.R;
import id.co.kurindo.kurindo.app.AppConfig;
import id.co.kurindo.kurindo.app.AppController;
import id.co.kurindo.kurindo.comp.ProgressDialogCustom;
import id.co.kurindo.kurindo.helper.ViewHelper;
import id.co.kurindo.kurindo.model.TOrder;
import id.co.kurindo.kurindo.util.LogUtil;
import id.co.kurindo.kurindo.wizard.BaseStepFragment;

import static android.app.Activity.RESULT_OK;

/**
 * Created by aspire on 12/15/2016.
 */

public class StepRejectTOrderFragment extends BaseStepFragment implements Step {
    private static final String TAG = "StepRejectTOrderFragment";

    @Bind(R.id.tv_formTitle)
    TextView tvformTitle;

    @Bind(R.id.layoutReject)
    LinearLayout layoutReject;
    @Bind(R.id.radio_group_reason)
    RadioGroup rdgReason;

    @Bind(R.id.input_reason)
    EditText reasonEt;
    @Bind(R.id.inlay_reason)
    TextInputLayout reasonLayout;
    TOrder order;
    Context context;
    private ProgressDialog progressBar;
    String reason;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflateAndBind(inflater, container, R.layout.fragment_accept_order);
        context = getContext();
        progressBar = new ProgressDialogCustom(context);
        //order = ((RejectOrderActivity)getActivity()).getOrder();
        order = ViewHelper.getInstance().getOrder();

        layoutReject.setVisibility(View.VISIBLE);
        tvformTitle.setText("Reject Order");

        rdgReason.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                reasonEt.setVisibility(View.GONE);
                switch (checkedId){
                    case R.id.radio_reason1:
                        reason = getString(R.string.label_reason1);
                        break;
                    case R.id.radio_reason2:
                        reason = getString(R.string.label_reason2);
                        break;
                    case R.id.radio_reason3:
                        reason = getString(R.string.label_reason3);
                        break;
                    case R.id.radio_reason4:
                        reason = getString(R.string.label_reason4);
                        break;
                    case R.id.radio_reason5:
                        reasonEt.setVisibility(View.VISIBLE);
                        reason = null;
                        break;
                }
            }
        });

        return v;
    }


    @Override
    public int getName() {
        return 0;
    }

    @Override
    public VerificationError verifyStep() {
        if(rdgReason.getCheckedRadioButtonId() == R.id.radio_reason5){
            reason = reasonEt.getText().toString();
        }
        if(reason == null || reason.isEmpty()){
            return new VerificationError("Error: Tulis alasannya.");
        }
        final VerificationError[] verify = {null};

        // make a handler that throws a runtime exception when a message is received
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message mesg) {
                throw new RuntimeException();
            }
        };

        DialogInterface.OnClickListener YesClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                verify[0] = place_order(handler);
            }
        };
        DialogInterface.OnClickListener NoClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                handler.handleMessage(null);
            }
        };
        showConfirmationDialog("Konfirmasi","Pesanan ini akan ditolak. Anda yakin?", YesClickListener, NoClickListener);

        // loop till a runtime exception is triggered.
        try { Looper.loop(); }
        catch(RuntimeException e2) {}


        return null;
    }

    private VerificationError place_order(final Handler handler) {
        String tag_string_req = "req_order_rejected";
        final VerificationError[] verify = {null};
        progressBar.show();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_TORDER_REJECT, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                LogUtil.logD(TAG, "Process_order Response: " + response.toString());
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    String message = jObj.getString("message");
                    if (!error) {
                        //success
                        order.setStatus(AppConfig.KEY_KUR999);
                        Intent intent = new Intent();
                        //intent.putExtra("order", order);
                        getActivity().setResult(RESULT_OK, intent);
                        verify[0] = null;
                    }else{
                        verify[0] = new VerificationError("Error: "+message);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    verify[0] = new VerificationError("Error: "+e.getMessage());
                }
                progressBar.dismiss();
                handler.handleMessage(null);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                LogUtil.logE(TAG, "Process_order Error: " + error.getMessage());
                verify[0] = new VerificationError("Error: "+error.getMessage());
                progressBar.dismiss();
                handler.handleMessage(null);
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                HashMap<String, String> params = new HashMap<>();

                params.put("awb",(order ==null? null : order.getAwb()));
                params.put("reason", reason);
                return params;
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return getKurindoHeaders();
            }
        };
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);

        return verify[0];
    }

    @Override
    public void onSelected() {
        rdgReason.check(R.id.radio_reason1);
    }

    @Override
    public void onError(@NonNull VerificationError error) {

    }
}

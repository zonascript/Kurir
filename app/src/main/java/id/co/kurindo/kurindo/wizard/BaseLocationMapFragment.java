package id.co.kurindo.kurindo.wizard;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.stepstone.stepper.Step;
import com.stepstone.stepper.VerificationError;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import id.co.kurindo.kurindo.R;
import id.co.kurindo.kurindo.adapter.PacketServiceAdapter;
import id.co.kurindo.kurindo.adapter.PaymentAdapter;
import id.co.kurindo.kurindo.adapter.TUserAdapter;
import id.co.kurindo.kurindo.app.AppConfig;
import id.co.kurindo.kurindo.base.RecyclerItemClickListener;
import id.co.kurindo.kurindo.helper.DoSendHelper;
import id.co.kurindo.kurindo.map.DataParser;
import id.co.kurindo.kurindo.map.MapUtils;
import id.co.kurindo.kurindo.map.PlaceArrayAdapter;
import id.co.kurindo.kurindo.model.Address;
import id.co.kurindo.kurindo.model.PacketService;
import id.co.kurindo.kurindo.model.Payment;
import id.co.kurindo.kurindo.model.Route;
import id.co.kurindo.kurindo.model.TUser;
import id.co.kurindo.kurindo.wizard.BaseStepFragment;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.widget.Toast.LENGTH_SHORT;

/**
 * Created by DwiM on 4/7/2017.
 */

public class BaseLocationMapFragment
        extends BaseStepFragment
        implements Step, OnMapReadyCallback, View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static String TAG = "MAP LOCATION";
    Context mContext;

    @Bind(R.id.locationMarkertext)
    TextView mLocationMarkerText;
    private LatLng mCenterLatLong;
    @Bind(R.id.locationMarker)
    LinearLayout locationMarkerLayout;

    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION= 2;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION= 3;
    private static final LatLngBounds BOUNDS_ID = new LatLngBounds(new LatLng(-0, 0), new LatLng(0, 0));

    @Bind(R.id.iconOrigin)
    ImageView ivIconOrigin;
    @Bind(R.id.iconDestination)
    ImageView ivIconDestination;
    TUserAdapter tUserAdapter;
    ArrayList<TUser> data = new ArrayList<>();

    @Bind(R.id.tvOrigin)
    TextView tvOrigin;
    @Bind(R.id.tvDestination)
    TextView tvDestination;
    @Bind(R.id.tvDistanceInfo)
    TextView tvDistanceInfo;
    @Bind(R.id.tvPriceInfo)
    TextView tvPriceInfo;

    @Bind(R.id.imageMarker)
    ImageView imageMarker;
    @Bind(R.id.ivAddOriginNotes)
    ImageView ivAddOriginNotes;
    @Bind(R.id.ivAddDestinationNotes)
    ImageView ivAddDestinationNotes;

    @Bind(R.id.etOriginNotes)
    protected EditText etOriginNotes;

    @Bind(R.id.etDestinationNotes)
    protected EditText etDestinationNotes;

    @Bind(R.id.destination_layout)
    LinearLayout destinationLayout;
    @Bind(R.id.origin_layout)
    LinearLayout originLayout;

    @Bind(R.id.orderInfo_layout)
    LinearLayout orderLayout;
    @Bind(R.id.service_layout)
    LinearLayout serviceLayout;
    @Bind(R.id.ivSwitchInfo)
    ImageView ivSwitchInfo;

    boolean originMode = true;
    boolean destinationMode;
    boolean addOriginNote;
    boolean addDestinationNote;

    @Bind(R.id.tvOriginAutoComplete)
    AutoCompleteTextView mOriginAutoCompleteTextView;
    @Bind(R.id.tvDestinationAutoComplete)
    AutoCompleteTextView mDestinationAutoCompleteTextView;
    PlaceArrayAdapter mPlaceArrayAdapter;

    protected Route route;
    protected TUser origin = new TUser();
    protected TUser destination = new TUser();
    Location mLastLocation;
    Marker originMarker;
    Marker destinationMarker;

    //@Bind(R.id.ButtonAddOrder)
    //AppCompatButton buttonAddOrder;

    @Bind(R.id.rvPayment)
    RecyclerView rvPayment;
    PaymentAdapter paymentAdapter;
    protected Payment payment;

    @Bind(R.id.rgDoType)
    protected RadioGroup rgDoType;
    @Bind(R.id.radio_dojek)
    protected RadioButton radioDoJek;
    @Bind(R.id.radio_dosend)
    protected RadioButton radioDoSend;
    @Bind(R.id.radio_domove)
    protected RadioButton radioDoMove;
    @Bind(R.id.radio_docar)
    protected RadioButton radioDoCar;

    protected String doType = AppConfig.KEY_DOSEND;
    protected String serviceCode = AppConfig.PACKET_SDS;
    protected double price = 0;
    protected float beratKiriman = 0;
    protected float volume = 0;


    @Bind(R.id.input_service_code)
    Spinner _serviceCodeText;
    PacketServiceAdapter packetServiceAdapter;

    SupportMapFragment mapFragment;
    protected boolean inDoSendCoverageArea = true;
    protected boolean inDoMoveCoverageArea = true;
    protected boolean inDoJekCoverageArea = true;
    protected boolean inDoCarCoverageArea = true;
    private int layout;

    public int getLayout() {
        return R.layout.fragment_maps_dosend;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflateAndBind(inflater, container, getLayout());

        if (DoSendHelper.getInstance().getPacket() != null && DoSendHelper.getInstance().getPacket().getOrigin() != null) {
            origin = DoSendHelper.getInstance().getPacket().getOrigin();
            destination = DoSendHelper.getInstance().getPacket().getDestination();
        }
        mContext = getContext();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        //SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        //mapFragment.getMapAsync(this);
        //buildGoogleApiClient();

        initialize(view);

        showAddressLayout();

        tUserAdapter = new TUserAdapter(getContext(), data);

        mPlaceArrayAdapter = new PlaceArrayAdapter(getContext(), android.R.layout.simple_list_item_1, BOUNDS_ID, null);
        mOriginAutoCompleteTextView.setAdapter(mPlaceArrayAdapter);
        mDestinationAutoCompleteTextView.setAdapter(mPlaceArrayAdapter);

        AdapterView.OnItemClickListener adapterOnItemClik = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final PlaceArrayAdapter.PlaceAutocomplete item = mPlaceArrayAdapter.getItem(position);
                final String placeId = String.valueOf(item.placeId);
                Log.i("", "Selected: " + item.description);
                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId);
                placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
                Log.i("", "Fetching details for ID: " + item.placeId);
            }
        };
        mOriginAutoCompleteTextView.setOnItemClickListener(adapterOnItemClik);
        mDestinationAutoCompleteTextView.setOnItemClickListener(adapterOnItemClik);
        paymentAdapter = new PaymentAdapter(getContext(), getPaymentData(), new PaymentAdapter.OnItemClickListener() {
            @Override
            public void onActionButtonClick(View view, int position) {
                Payment p = paymentAdapter.getItem(position);
                if (p.getAction().equalsIgnoreCase(AppConfig.ISI_SALDO)) {
                    Toast.makeText(getContext(), "TODO : " + AppConfig.ISI_SALDO, LENGTH_SHORT).show();
                }
            }
        });
        paymentAdapter.selected(1);
        payment = paymentAdapter.getItem(1);
        rvPayment.setAdapter(paymentAdapter);
        rvPayment.setLayoutManager(new GridLayoutManager(getContext(), 1) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        rvPayment.setHasFixedSize(true);
        rvPayment.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Payment selected = paymentAdapter.getItem(position);
                if (selected.getCredit() == 0) {
                    Toast.makeText(getContext(), "Saldo tidak mencukupi. Silahkan isi ulang saldo anda.", LENGTH_SHORT).show();
                    position = paymentAdapter.getSelectedPosition();
                }
                paymentAdapter.selected(position);
                payment = paymentAdapter.getItem(position);
            }
        }));

        rgDoType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (group.getCheckedRadioButtonId()) {
                    case R.id.radio_dosend:
                        doType = AppConfig.KEY_DOSEND;
                        break;
                    case R.id.radio_dojek:
                        doType = AppConfig.KEY_DOJEK;
                        break;
                    case R.id.radio_domove:
                        doType = AppConfig.KEY_DOMOVE;
                        break;
                    case R.id.radio_docar:
                        doType = AppConfig.KEY_DOCAR;
                        break;
                }
                if (canDrawRoute()) requestprice();
            }
        });

        packetServiceAdapter = new PacketServiceAdapter(getContext(), AppConfig.getPacketServiceList(), 1);
        _serviceCodeText.setAdapter(packetServiceAdapter);
        _serviceCodeText.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                serviceCode = ((PacketService) parent.getItemAtPosition(position)).getCode();
                if (canDrawRoute()) requestprice();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        afterOnCreateView();
        //mResultReceiver = new MapsActivity.AddressResultReceiver(new Handler());

        if (checkPlayServices()) {
            // If this check succeeds, proceed with normal processing.
            // Otherwise, prompt user to get valid Play Services APK.
            if (!MapUtils.isLocationEnabled(mContext)) {
                // notify user
                AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
                dialog.setMessage("Location not enabled!");
                dialog.setPositiveButton("Open location settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                });
                dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        // TODO Auto-generated method stub

                    }
                });
                dialog.show();
            }
        } else {
            Toast.makeText(mContext, "Location not supported in this device", LENGTH_SHORT).show();
        }

        return view;

    }

    private void initialize(View view) {

        if(tvOrigin == null){
            mLocationMarkerText = (TextView) view.findViewById(R.id.locationMarkertext);
            locationMarkerLayout = (LinearLayout) view.findViewById(R.id.locationMarker);
            ivIconOrigin = (ImageView) view.findViewById(R.id.iconOrigin);
            ivIconDestination = (ImageView) view.findViewById(R.id.iconDestination);

            tvOrigin = (TextView) view.findViewById(R.id.tvOrigin);
            tvDestination = (TextView) view.findViewById(R.id.tvDestination);
            tvDistanceInfo = (TextView) view.findViewById(R.id.tvDistanceInfo);
            tvPriceInfo = (TextView) view.findViewById(R.id.tvPriceInfo);

            imageMarker = (ImageView) view.findViewById(R.id.imageMarker);
            ivAddOriginNotes = (ImageView) view.findViewById(R.id.ivAddOriginNotes);
            ivAddDestinationNotes = (ImageView) view.findViewById(R.id.ivAddDestinationNotes);

            etOriginNotes = (EditText) view.findViewById(R.id.etOriginNotes);
            etDestinationNotes = (EditText) view.findViewById(R.id.etDestinationNotes);

            destinationLayout = (LinearLayout) view.findViewById(R.id.destination_layout);
            originLayout = (LinearLayout) view.findViewById(R.id.origin_layout);
            orderLayout = (LinearLayout) view.findViewById(R.id.orderInfo_layout);
            serviceLayout = (LinearLayout) view.findViewById(R.id.service_layout);
            ivSwitchInfo = (ImageView) view.findViewById(R.id.ivSwitchInfo);
            mOriginAutoCompleteTextView = (AutoCompleteTextView) view.findViewById(R.id.tvOriginAutoComplete);
            mDestinationAutoCompleteTextView = (AutoCompleteTextView) view.findViewById(R.id.tvDestinationAutoComplete);
            rvPayment = (RecyclerView) view.findViewById(R.id.rvPayment);
            rgDoType = (RadioGroup) view.findViewById(R.id.rgDoType);
            radioDoJek = (RadioButton) view.findViewById(R.id.radio_dojek);
            radioDoSend = (RadioButton) view.findViewById(R.id.radio_dosend);
            radioDoMove = (RadioButton) view.findViewById(R.id.radio_domove);
            radioDoCar = (RadioButton) view.findViewById(R.id.radio_docar);
            _serviceCodeText = (Spinner) view.findViewById(R.id.input_service_code);
        }

    }

    protected void afterOnCreateView() {

    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FragmentManager fm = getChildFragmentManager();
        mapFragment = (SupportMapFragment) fm.findFragmentById(R.id.map);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.map, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);
        buildGoogleApiClient();

    }

    private List<Payment> getPaymentData() {
        List<Payment> data = new ArrayList<>();
        Payment payment = new Payment("DO-PAY (Rp 0)", "Get 20% discount by  using DO-PAY", AppConfig.ISI_SALDO);
        //payment.setCredit(10000);
        data.add(payment);
        payment = new Payment("Tunai");
        payment.setCredit(1);
        data.add(payment);
        return data;
    }

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                Log.e("ResultCallback", "Place query did not complete. Error: " +places.getStatus().toString());
                return;
            }


            // Selecting the first object buffer.
            final Place place = places.get(0);
            CharSequence attributions = places.getAttributions();

            String name = place.getName().toString();
            String address = place.getAddress().toString();
            if(originMode){
                tvOrigin.setText(address);
                etOriginNotes.setText(name);
                etOriginNotes.setFocusable(false);
                etOriginNotes.setVisibility(View.VISIBLE);
                addOriginNote = true;
                requestAddress(place.getLatLng());

                Address addr = origin.getAddress();
                addr.setAlamat(name);
                //addr.setFormattedAddress(address);
                addr.setLocation( place.getLatLng());
                //if(destination.getAddress().getLocation() != null) originMode = false;
            }
            if(destinationMode){
                tvDestination.setText(address);
                etDestinationNotes.setText(name);
                etDestinationNotes.setFocusable(false);
                etDestinationNotes.setVisibility(View.VISIBLE);
                addDestinationNote= true;

                requestAddress(place.getLatLng());

                Address addr = destination.getAddress();
                addr.setAlamat(name);
                //addr.setFormattedAddress(address);
                addr.setLocation( place.getLatLng() );
                //if(origin.getAddress().getLocation() != null) destinationMode = false;
            }
            inDoSendCoverageArea = true;
            inDoMoveCoverageArea = true;
            //showAddressLayout();
            //refreshMap();
            reDrawMarker();
            //if(!canDrawRoute())
            moveCameraToLocation( place.getLatLng());



        }
    };

    protected boolean canDrawRoute() {
        return (origin.getAddress().getLocation() != null && destination.getAddress().getLocation() != null);
    }

    public void resetAll() {
        route = null;

        origin = new TUser();
        destination = new TUser();
        DoSendHelper.getInstance().clearAll();
    }

    public boolean handleBackPressed(){
        if(originMode){
            originMode = false;
            showAddressLayout();
            if(origin != null && origin.getAddress().getLocation() != null)
                moveCameraToLocation(origin.getAddress().getLocation());
            showMap();
            return true;
        }
        if(destinationMode){
            destinationMode = false;
            showAddressLayout();
            if(destination.getAddress().getLocation() == null) moveCameraToLocation(origin.getAddress().getLocation());
            else moveCameraToLocation(destination.getAddress().getLocation());
            showMap();

            return true;
        }
        if(canDrawRoute()){
            resetDestination();
            showMap();
            showAddressLayout();
            moveCameraToLocation(origin.getAddress().getLocation());
            return true;
        }
        resetAll();
        return false;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().finish();
    }

    private void resetDestination(){
        destination = new TUser();
        route = null;
        tvDestination.setText("Set Tujuan Anda");
        etDestinationNotes.setText("");
        etDestinationNotes.setVisibility(View.GONE);
        destinationMode = true;
        //buttonAddOrder.setVisibility(View.GONE);
    }
    private void showAddressLayout() {
        tvOrigin.setVisibility(View.VISIBLE);
        ivAddOriginNotes.setVisibility(View.VISIBLE);
        mOriginAutoCompleteTextView.setVisibility(View.GONE);
        destinationLayout.setVisibility(View.VISIBLE);

        tvDestination.setVisibility(View.VISIBLE);
        ivAddDestinationNotes.setVisibility(View.VISIBLE);
        mDestinationAutoCompleteTextView.setVisibility(View.GONE);
        originLayout.setVisibility(View.VISIBLE);
        locationMarkerLayout.setVisibility(View.GONE);
    }

    private void changeMarkerIcon(){
        locationMarkerLayout.setVisibility(View.VISIBLE);
        imageMarker.setImageResource(destinationMode ? R.drawable.destination_pin : R.drawable.origin_pin );
        mLocationMarkerText.setVisibility(View.VISIBLE);
        mLocationMarkerText.setText("Set "+(destinationMode? "Tujuan Anda" : "Lokasi Anda"));
    }
    public void moveCameraToLocation(LatLng location, float zoom){
        CameraPosition cameraPosition = new CameraPosition.Builder().target(location).zoom(zoom).tilt(AppConfig.DEFAULT_TILT_MAP).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }
    public void moveCameraToLocation(LatLng location){
        moveCameraToLocation(location, AppConfig.DEFAULT_ZOOM_MAP);
    }
    public void onClick(View v) {
        //TODO
    }

    @OnClick(R.id.iconOrigin)
    public void onClick_IconOrigin(){
        originMode = true;
        destinationMode = !originMode;
        showPopupWindow("Daftar Lokasi", R.drawable.origin_pin);

    }
    @OnClick(R.id.iconDestination)
    public void onClick_IconDestination(){
        destinationMode = true;
        originMode = !destinationMode;
        showPopupWindow("Daftar Lokasi", R.drawable.destination_pin);

    }

    @OnClick(R.id.locationMarkertext)
    public void onClick_mLocationMarkerText(){
        if(destinationMode){
            destination.getAddress().setLocation( new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()) );
            //tvDestination.setText("Lat. "+ mLastLocation.getLatitude()+ ", "+mLastLocation.getLongitude());
            etDestinationNotes.setText(null);
            etDestinationNotes.setVisibility(View.GONE);
            addDestinationNote= false;
        }else {
            origin.getAddress().setLocation( new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()) );
            //tvOrigin.setText("Lat. "+ mLastLocation.getLatitude()+ ", "+mLastLocation.getLongitude());
            etOriginNotes.setText(null);
            etOriginNotes.setVisibility(View.GONE);
            addOriginNote= false;
        }
        inDoSendCoverageArea = true;
        inDoMoveCoverageArea = true;
        requestAddress(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));

        mLocationMarkerText.setVisibility(View.GONE);

        showAddressLayout();

        //refreshMap();
        //startIntentService(mLastLocation);

    }
    @OnClick(R.id.ButtonAddOrder)
    public void onClick_buttonAddOrder(){
        String onotes = etOriginNotes.getText().toString();
        String dnotes = etDestinationNotes.getText().toString();
        origin.getAddress().setNotes(onotes);
        destination.getAddress().setNotes(dnotes);
        if( doType.equalsIgnoreCase(AppConfig.KEY_DOSEND)){
            DoSendHelper.getInstance().setPacketRoute(origin, destination);
            DoSendHelper.getInstance().addDoSendOrder(payment.getText(), serviceCode, route.getDistance().getValue(), price);
            //showActivity( DoSendOrderActivity.class );
            //finish();
        }else{
            Toast.makeText(getContext(), doType+" : available soon.", LENGTH_SHORT).show();
        }
    }

    private void showMap() {
        reDrawMarker();
        drawRoute();
    }
    private void refreshMap() {
        reDrawMarker();
        reDrawRoute();
    }

    @OnClick(R.id.ivSwitchInfo)
    public void OnClick_ivSwitchInfo(){
        showOrderpanel(serviceLayout.isShown());
        orderLayout.setVisibility(View.VISIBLE );
        //serviceLayout.setMinimumHeight(100);
        //buttonAddOrder.setVisibility(View.VISIBLE );
    }
    private void showOrderpanel(boolean show){
        orderLayout.setVisibility((show ? View.VISIBLE : View.GONE ));
        rvPayment.setVisibility((show ? View.VISIBLE : View.GONE ));
        ivSwitchInfo.setImageResource(show? R.drawable.ic_expand_less_black_18dp: R.drawable.ic_expand_more_black_18dp );
        serviceLayout.setVisibility(show ? View.GONE : View.VISIBLE );
    }

    private void hidepanel(boolean hide) {
        orderLayout.setVisibility((hide ? View.GONE : View.VISIBLE));
        rvPayment.setVisibility((hide ? View.GONE: View.VISIBLE));
        serviceLayout.setVisibility((hide ? View.GONE : View.VISIBLE));
        //buttonAddOrder.setVisibility((hide ? View.GONE : View.VISIBLE));
    }


    private void requestAddress(LatLng latLng) {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message mesg) {
                throw new RuntimeException("RuntimeException");
            }
        };
        requestAddress_inBackground(latLng);
        //requestAddress(latLng, handler);
        //try { Looper.loop(); } catch(RuntimeException e2) { }
    }

    private void requestAddress_inBackground(LatLng latLng) {
        String url = MapUtils.getGeocodeUrl(latLng);
        addRequest("request_geocode_address", Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                List<List<HashMap<String, String>>> routes = null;
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean OK = "OK".equalsIgnoreCase(jObj.getString("status"));
                    if(OK){
                        DataParser parser = new DataParser();
                        Address address = parser.parseAddress(jObj);
                        if(originMode){
                            tvOrigin.setText(address.getFormattedAddress());
                            address.setLocation(origin.getAddress().getLocation());
                            origin.setAddress(  address );
                            //origin = address.getLocation();
                            if(destination.getAddress().getLocation() != null) originMode= false;
                            //originMode= false;
                        }else if(destinationMode){
                            tvDestination.setText(address.getFormattedAddress());
                            address.setLocation(destination.getAddress().getLocation());
                            destination.setAddress( address );
                            //destination = address.getLocation();
                            if(origin.getAddress().getLocation() != null) destinationMode = false;
                            //destinationMode= false;
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    if(originMode){
                        if(destination.getAddress().getLocation() != null) originMode= false;
                    }else if(destinationMode){
                        if(origin.getAddress().getLocation() != null) destinationMode = false;
                    }
                }
                refreshMap();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                volleyError.printStackTrace();
                if(originMode){
                    if(destination.getAddress().getLocation() != null) originMode= false;
                }else if(destinationMode){
                    if(origin.getAddress().getLocation() != null) destinationMode = false;
                }
            }
        }, null, null);
    }

    private void requestAddress(LatLng latLng, final Handler handler) {
        String url = MapUtils.getGeocodeUrl(latLng);
        addRequest("request_geocode_address", Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Log.d(TAG, "requestAddress Response: " + response.toString());
                List<List<HashMap<String, String>>> routes = null;
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean OK = "OK".equalsIgnoreCase(jObj.getString("status"));
                    if(OK){
                        DataParser parser = new DataParser();
                        Address address = parser.parseAddress(jObj);
                        if(originMode){
                            tvOrigin.setText(address.getFormattedAddress());
                            address.setLocation(origin.getAddress().getLocation());
                            origin.setAddress(  address );
                            //origin = address.getLocation();
                            if(destination.getAddress().getLocation() != null) originMode= false;
                            //originMode= false;
                        }else if(destinationMode){
                            tvDestination.setText(address.getFormattedAddress());
                            address.setLocation(destination.getAddress().getLocation());
                            destination.setAddress( address );
                            //destination = address.getLocation();
                            if(origin.getAddress().getLocation() != null) destinationMode = false;
                            //destinationMode= false;
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    if(originMode){
                        if(destination.getAddress().getLocation() != null) originMode= false;
                    }else if(destinationMode){
                        if(origin.getAddress().getLocation() != null) destinationMode = false;
                    }
                }
                handler.handleMessage(null);
                refreshMap();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                volleyError.printStackTrace();
                if(originMode){
                    if(destination.getAddress().getLocation() != null) originMode= false;
                }else if(destinationMode){
                    if(origin.getAddress().getLocation() != null) destinationMode = false;
                }
                handler.handleMessage(null);
            }
        }, null, null);
    }
    private void drawRoute() {
        if(route != null){
            DoSendHelper.getInstance().setPacketRoute(origin, destination);

            DataParser parser = new DataParser();
            String snippet = originMarker.getSnippet();
            originMarker.setSnippet(snippet + "\nEst. Distance :"+ route.getDistance().getText());
            tvDistanceInfo.setText("Harga ( "+route.getDistance().getText()+" ) : ");

            handleNext();

            PolylineOptions lineOptions = parser.drawRoutes(route.getRoutes());

            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions != null) {
                mMap.addPolyline(lineOptions);

                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                /*
                List<LatLng> points = lineOptions.getPoints();
                for (int i = 0; i < points.size(); i++) {
                    builder.include(points.get(i));
                }*/
                builder.include(origin.getAddress().getLocation());
                if(destination.getAddress().getLocation() != null) builder.include(destination.getAddress().getLocation());

                LatLngBounds bounds = builder.build();

                int width = getResources().getDisplayMetrics().widthPixels;
                int padding = (int) (width * 0.40); // offset from edges of the map 12% of screen

                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                mMap.animateCamera(cu);
                //moveCameraToLocation(bounds.getCenter(), 16f);

            }else {
                Log.d("drawRoutes","without Polylines drawn");
            }
        }
        if(canDrawRoute() && route != null) {
            /*Location lo = new Location("Origin");
            lo.setLatitude(origin.getAddress().getLocation().latitude);
            lo.setLongitude(origin.getAddress().getLocation().longitude);

            Location ld = new Location("Destination");
            ld.setLatitude(destination.getAddress().getLocation().latitude);
            ld.setLongitude(destination.getAddress().getLocation().longitude);

            tvDistanceInfo.setText("Harga ( "+route.getDistance().getText()+" ) : ");
            //tvPriceInfo.setText("");

            Toast.makeText(getContext(), "Calculate distance : " + lo.distanceTo(ld), LENGTH_SHORT).show();
            //buttonAddOrder.setVisibility(View.VISIBLE );
            */
            showOrderpanel(true);
        }else {
            showOrderpanel(false);
            //buttonAddOrder.setVisibility(View.GONE);
        }

    }

    public void handleNext(){
        int dist = 0;
        try{
            dist = Integer.parseInt(route.getDistance().getValue());
        }catch (Exception e){}
        if(dist > AppConfig.MAX_DOSEND_COVERAGE_KM ){
            if(doType.equalsIgnoreCase(AppConfig.KEY_DOSEND)) {
                //showErrorDialog("Distance Limited.", "Jarak terlalu jauh. Silahkan menggunakan jasa DO-MOVE.");
                Toast.makeText(getContext(), "( " + route.getDistance().getText() + " ): Jarak terlalu jauh. Silahkan menggunakan jasa DO-MOVE.", LENGTH_SHORT).show();
                inDoSendCoverageArea = false;
            }else if(doType.equalsIgnoreCase(AppConfig.KEY_DOMOVE)){
                if(dist > AppConfig.MAX_DOMOVE_COVERAGE_KM ){
                    Toast.makeText(getContext(), "( " + route.getDistance().getText() + " ): Jarak terlalu jauh. Tidak ada layanan.", LENGTH_SHORT).show();
                    inDoMoveCoverageArea = false;
                }
            }else if(doType.equalsIgnoreCase(AppConfig.KEY_DOJEK)){
                if(dist > AppConfig.MAX_DOJEK_COVERAGE_KM ){
                    Toast.makeText(getContext(), "( " + route.getDistance().getText() + " ): Jarak terlalu jauh. Silahkan menggunakan jasa DO-CAR.", LENGTH_SHORT).show();
                    inDoJekCoverageArea = false;
                }
            }else if(doType.equalsIgnoreCase(AppConfig.KEY_DOCAR)){
                if(dist > AppConfig.MAX_DOCAR_COVERAGE_KM ){
                    Toast.makeText(getContext(), "( " + route.getDistance().getText() + " ): Jarak terlalu jauh. Tidak ada layanan.", LENGTH_SHORT).show();
                    inDoCarCoverageArea = false;
                }
            }else{
                if(dist > AppConfig.MAX_DOMOVE_COVERAGE_KM ){
                    Toast.makeText(getContext(), "( " + route.getDistance().getText() + " ): Jarak terlalu jauh. Tidak ada layanan.", LENGTH_SHORT).show();
                    inDoMoveCoverageArea = false;
                }
            }
            showOrderpanel(false);
        }else{
            requestprice();
        }
    }
    private void reDrawRoute() {

        if(canDrawRoute()){

            String url = MapUtils.getDirectionUrl(origin.getAddress().getLocation(), destination.getAddress().getLocation());
            addRequest("request_direction_route", Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    //Log.d(TAG, "drawRoute Response: " + response.toString());
                    try {
                        JSONObject jObj = new JSONObject(response);
                        boolean OK = "OK".equalsIgnoreCase(jObj.getString("status"));
                        if(OK){
                            DataParser parser = new DataParser();
                            route = parser.parseRoutes(jObj);
                            DoSendHelper.getInstance().addRoute(route);
                            drawRoute();
                            //handleNext();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    Log.e(TAG, "request_direction_route Error: " + volleyError.getMessage());
                    volleyError.printStackTrace();
                }
            }, null, null);

        }
    }

    protected void requestprice() {
        HashMap<String, String> params = new HashMap();
        params.put("distance", (route ==null || route.getDistance()==null? "1" : route.getDistance().getValue()));
        params.put("origin", origin.getAddress().getKecamatan());//TODO json origin
        params.put("destination", destination.getAddress().getKecamatan()); //TODO json destination
        params.put("service_code", serviceCode);
        params.put("do_type", doType);
        params.put("berat_kiriman", ""+beratKiriman);
        params.put("volume", ""+volume);

        addRequest("request_price_route", Request.Method.POST, AppConfig.URL_CALC_PRICE_KM, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    Log.d(TAG, "requestprice Response: " + response.toString());
                    JSONObject jObj = new JSONObject(response);
                    boolean OK = "OK".equalsIgnoreCase(jObj.getString("status"));
                    if(OK){
                        double tariff = jObj.getDouble("tarif");
                        price = tariff;
                        tvPriceInfo.setText(AppConfig.formatCurrency(tariff));
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                volleyError.printStackTrace();
            }
        }, params, getKurindoHeaders());

    }

    private void requestDistance() {
        String url = MapUtils.getDistancematrixUrl(origin.getAddress().getLocation(), destination.getAddress().getLocation());
        addRequest("request_distance_route", Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Log.d(TAG, "drawRoute Response: " + response.toString());

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean OK = "OK".equalsIgnoreCase(jObj.getString("status"));
                    if(OK){
                        String destinationTxt = "";
                        String originTxt = "";
                        String distanceTxt = " ";
                        int distance = 0;
                        int duration = 0;
                        String durationTxt = "";

                        JSONArray originArr = jObj.getJSONArray("origin_addresses");
                        JSONArray destinationArr = jObj.getJSONArray("destination_addresses");
                        JSONArray rows = jObj.getJSONArray("rows");
                        for (int i = 0; i < originArr.length(); i++) {
                            if(i >0) {
                                originTxt +=",";
                                destinationTxt +=",";
                            }
                            originTxt += originArr.getString(i);
                            destinationTxt += destinationArr.getString(i);
                            JSONObject rowObj = rows.getJSONObject(i);
                            JSONArray elementsArr = rowObj.getJSONArray("elements");
                            for (int j = 0; j < elementsArr .length(); j++) {
                                JSONObject elementObj = elementsArr.getJSONObject(j);
                                JSONObject distanceObj = elementObj.getJSONObject("distance");
                                JSONObject durationObj = elementObj.getJSONObject("duration");
                                distance += distanceObj.getInt("value");
                                distanceTxt = distanceObj.getString("text");
                                duration += durationObj.getInt("value");
                            }
                        }
                        /*GsonBuilder builder = new GsonBuilder();
                        builder.setPrettyPrinting();
                        Gson gson = builder.create();
                        DistanceMatrixResponse mtx = gson.fromJson(response, DistanceMatrixResponse.class);
                        */
                        tvOrigin.setText(originTxt);
                        tvDestination.setText(destinationTxt);
                        originMarker.setSnippet("Est. Distance :"+distanceTxt );
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                volleyError.printStackTrace();
            }
        }, null, null);
    }


    public void reDrawMarker(){
        mMap.clear();
        if(destination != null && destination.getAddress() != null && destination.getAddress().getLocation() != null){
            String title = "Destination";
            String snippet = (destination.getName() != null ? destination.toStringFormatted() : "");
            destinationMarker = mMap.addMarker(
                    new MarkerOptions()
                            .position(destination.getAddress().getLocation())
                            .title(title)
                            .snippet(snippet)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_pin)));
        }
        if(origin != null && origin.getAddress() != null && origin.getAddress().getLocation() != null){
            String title = "Origin";
            String snippet = (origin.getName() != null ? origin.toStringFormatted() : "");
            originMarker = mMap.addMarker(
                    new MarkerOptions()
                            .position(origin.getAddress().getLocation())
                            .title(title)
                            .snippet(snippet)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.origin_pin)));
        }

    }


    @OnClick(R.id.tvOrigin)
    public void onClick_tvOrigin(){
        originMode= !originMode;
        destinationMode = false;
        tvOrigin.setVisibility(originMode ? View.GONE : View.VISIBLE);
        ivAddOriginNotes.setVisibility(originMode ? View.GONE : View.VISIBLE);
        destinationLayout.setVisibility(originMode ? View.GONE : View.VISIBLE);
        locationMarkerLayout.setVisibility(View.VISIBLE);
        mOriginAutoCompleteTextView.setVisibility(originMode ? View.VISIBLE : View.GONE);
        mOriginAutoCompleteTextView.setText("");
        if(origin.getAddress().getLocation() != null) moveCameraToLocation(origin.getAddress().getLocation());
        changeMarkerIcon();
        hidepanel(true);
    }

    @OnClick(R.id.tvDestination)
    public void onClick_tvDestination(){
        destinationMode= !destinationMode;
        originMode = false;
        tvDestination.setVisibility(destinationMode? View.GONE : View.VISIBLE);
        ivAddDestinationNotes.setVisibility(destinationMode? View.GONE : View.VISIBLE);
        originLayout.setVisibility(destinationMode ? View.GONE : View.VISIBLE);
        locationMarkerLayout.setVisibility(View.VISIBLE);
        mDestinationAutoCompleteTextView.setVisibility(destinationMode ? View.VISIBLE : View.GONE);
        mDestinationAutoCompleteTextView.setText("");
        if(destination.getAddress().getLocation()!= null) moveCameraToLocation(destination.getAddress().getLocation());
        changeMarkerIcon();
        hidepanel(true);
    }
    @OnClick(R.id.ivAddOriginNotes)
    public void onClick_ivAddOriginNotes(){
        addOriginNote =!addOriginNote;
        etOriginNotes.setVisibility(addOriginNote? View.VISIBLE : View.GONE);
    }
    @OnClick(R.id.ivAddDestinationNotes)
    public void ivAddDestinationNotes(){
        addDestinationNote =!addDestinationNote;
        etDestinationNotes.setVisibility(addDestinationNote? View.VISIBLE : View.GONE);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "OnMapReady");
        mMap = googleMap;
        mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int i) {
                if(!canDrawRoute() || originMode || destinationMode){
                    reDrawMarker();
                }
            }
        });
        mMap.setOnCameraMoveCanceledListener(new GoogleMap.OnCameraMoveCanceledListener() {
            @Override
            public void onCameraMoveCanceled() {
                reDrawMarker();
            }
        });
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                Log.d("Camera position change" + "", cameraPosition + "");
                mCenterLatLong = cameraPosition.target;

                //mMap.clear();

                try {
                    Location mLocation = new Location("");
                    mLocation.setLatitude(mCenterLatLong.latitude);
                    mLocation.setLongitude(mCenterLatLong.longitude);
                    mLastLocation = mLocation;

                    //startIntentService(mLocation);
                    //mLocationMarkerText.setText("Lat : " + mCenterLatLong.latitude + "," + "Long : " + mCenterLatLong.longitude);
                    if(!canDrawRoute()) changeMarkerIcon();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{ Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

            return;
        }
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            Log.d(TAG, "ON connected ");
            if(canDrawRoute()) {
                drawRoute();
            }else{
                if(origin.getAddress().getLocation() == null) {
                    origin.getAddress().setLocation( new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()) );
                    originMode= true;
                    requestAddress_inBackground(origin.getAddress().getLocation());
                }
                changeMap(mLastLocation);
            }
            reDrawMarker();
            showAddressLayout();
            this.mLastLocation = mLastLocation;

        } else
            try {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        try {
            LocationRequest mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(10000);
            mLocationRequest.setFastestInterval(5000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        } catch (Exception e) {
            e.printStackTrace();
        }

        mPlaceArrayAdapter.setGoogleApiClient(mGoogleApiClient);

    }


    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        try {
            if (location != null){
                if(canDrawRoute()){
                    drawRoute();
                }else{
                    changeMap(location);
                }
            }

            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }


    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .enableAutoManage(getActivity(), 0 /* clientId */, this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .build();
    }

    @Override
    public void onStart() {
        try {
            mGoogleApiClient.connect();

        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onStart();
    }

    @Override
    public void onStop() {
        try {

        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getContext());
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(),
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                //finish();
            }
            return false;
        }
        return true;
    }

    private void changeMap(Location location) {

        Log.d(TAG, "Reaching map" + mMap);


        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{ Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
            return;
        }

        // check if map is created successfully or not
        if (mMap != null) {
            mMap.getUiSettings().setZoomControlsEnabled(false);
            LatLng latLong;

            latLong = new LatLng(location.getLatitude(), location.getLongitude());

            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            moveCameraToLocation(latLong);

            //mLocationMarkerText.setText("Set "+ (originMode ? "Lokasi Jemput":"Tujuan Anda >"));
            //startIntentService(location);

        } else {
            Toast.makeText(getContext(),
                    "Sorry! unable to create maps", LENGTH_SHORT)
                    .show();
        }

    }

    public void setDoType(String doType) {
        this.doType = doType;
    }


    /**
     * Called after the autocomplete activity has finished to return its result.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check that the result was from the autocomplete widget.
        if (requestCode == REQUEST_CODE_AUTOCOMPLETE) {
            if (resultCode == RESULT_OK) {
                // Get the user's selected place from the Intent.
                Place place = PlaceAutocomplete.getPlace(mContext, data);

                // TODO call location based filter


                LatLng latLong;


                latLong = place.getLatLng();

                //mLocationText.setText(place.getName() + "");
                if(originMode)
                    tvOrigin.setText(place.getAddress());
                if(destinationMode)
                    tvDestination.setText(place.getAddress());

                moveCameraToLocation(latLong);

            }


        } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
            Status status = PlaceAutocomplete.getStatus(mContext, data);
        } else if (resultCode == RESULT_CANCELED) {
            // Indicates that the activity closed before a selection was made. For example if
            // the user pressed the back button.
        }
    }


    protected void showPopupWindow(String title, int imageResourceId) {
        data.clear();
        data.addAll(db.getAddressList());

        // Create custom dialog object
        final Dialog dialog = new Dialog(getContext());
        // Include dialog.xml file
        dialog.setContentView(R.layout.popup_list);
        // Set dialog title
        dialog.setTitle("Popup Dialog");

        RecyclerView list = (RecyclerView) dialog.findViewById(R.id.popupList);
        list.setLayoutManager(new GridLayoutManager(getContext(), 1));
        list.setHasFixedSize(true);
        list.setAdapter(tUserAdapter);
        list.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                TUser p = data.get(position);
                showAddressLayout();
                if(originMode){
                    origin = p;
                    tvOrigin.setText(p.getAddress().toStringFormatted());
                    if(p.getAddress().getNotes() != null && !p.getAddress().getNotes().isEmpty()) {
                        etOriginNotes.setText(p.getAddress().getNotes());
                        etOriginNotes.setVisibility(View.VISIBLE);
                    }
                    originMode = false;
                }
                if(destinationMode){
                    destination = p;
                    tvDestination.setText(p.getAddress().toStringFormatted());
                    if(p.getAddress().getNotes() != null && !p.getAddress().getNotes().isEmpty()) {
                        etDestinationNotes.setText(p.getAddress().getNotes());
                        etDestinationNotes.setVisibility(View.VISIBLE);
                    }
                    destinationMode = false;
                }
                dialog.dismiss();
                refreshMap();
            }
        }));

        // set values for custom dialog components - text, image and button
        //TextView text = (TextView) dialog.findViewById(R.id.textDialog);
        //text.setText(content);
        ImageView image = (ImageView) dialog.findViewById(R.id.imageDialog);
        if(imageResourceId == 0) imageResourceId  = R.drawable.icon_syarat_ketentuan;
        image.setImageResource(imageResourceId);
        TextView textTitleDialog = (TextView) dialog.findViewById(R.id.textTitleDialog);
        if(title != null) textTitleDialog.setText(title);

        dialog.show();

        ImageButton declineButton = (ImageButton) dialog.findViewById(R.id.btncancelcat);
        // if decline button is clicked, close the custom dialog
        declineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close dialog
                dialog.dismiss();
                destinationMode = false;
                originMode = false;
            }
        });
    }


    @Override
    public int getName() {
        return 0;
    }

    public VerificationError verifyStep() {
        if(!inDoSendCoverageArea){
            return new VerificationError("Jarak terlalu jauh untuk DOSEND. Gunakan DOMOVE sebagai alternatif.");
        }
        if(!inDoMoveCoverageArea){
            return new VerificationError("Jarak terlalu jauh. Tidak ada layanan.");
        }

        String onotes = etOriginNotes.getText().toString();
        String dnotes = etDestinationNotes.getText().toString();
        origin.getAddress().setNotes(onotes);
        destination.getAddress().setNotes(dnotes);
        if( doType.equalsIgnoreCase(AppConfig.KEY_DOSEND)) {
            if (!canDrawRoute()) {
                return new VerificationError("Pilih rute lokasi anda.");
            }
            //TODO : check location based service / kedua rute masih dalam 1 kabupaten (dojek, dosend) atau propinsi (domove, docar) dan exception propinsi kecil2

            DoSendHelper.getInstance().setPacketRoute(origin, destination);
            DoSendHelper.getInstance().addDoSendOrder(payment.getText(), serviceCode, (route == null || route.getDistance() == null ? "" : route.getDistance().getValue()), price);
            //showActivity( DoSendOrderActivity.class );
            //finish();
        }else{
            //Toast.makeText(getContext(), "Not Available", LENGTH_SHORT).show();
            return new VerificationError(doType+" Not Available");
        }
        return null;
    }

    @Override
    public void onSelected() {
        updateUI();
    }

    protected void updateUI() {

        radioDoSend.setVisibility(View.VISIBLE);
        radioDoMove.setVisibility(View.VISIBLE);
        radioDoJek.setVisibility(View.GONE);
        radioDoCar.setVisibility(View.GONE);

        switch (this.doType){
            case AppConfig.KEY_DOSEND:
                rgDoType.check(R.id.radio_dosend);
                break;
            case AppConfig.KEY_DOMOVE:
                rgDoType.check(R.id.radio_domove);
                break;
        }
    }

    @Override
    public void onError(@NonNull VerificationError error) {

    }

}
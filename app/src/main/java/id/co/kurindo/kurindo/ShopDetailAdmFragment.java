package id.co.kurindo.kurindo;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import id.co.kurindo.kurindo.adapter.EndlessRecyclerViewScrollListener;
import id.co.kurindo.kurindo.adapter.ProductGridAdapter;
import id.co.kurindo.kurindo.adapter.ProductGridAdmAdapter;
import id.co.kurindo.kurindo.app.AppConfig;
import id.co.kurindo.kurindo.base.BaseActivity;
import id.co.kurindo.kurindo.base.BaseFragment;
import id.co.kurindo.kurindo.base.RecyclerItemClickListener;
import id.co.kurindo.kurindo.helper.ShopAdmHelper;
import id.co.kurindo.kurindo.helper.ViewHelper;
import id.co.kurindo.kurindo.model.Address;
import id.co.kurindo.kurindo.model.Product;
import id.co.kurindo.kurindo.model.Shop;
import id.co.kurindo.kurindo.task.ListenableAsyncTask;
import id.co.kurindo.kurindo.task.LoadProductTask;
import id.co.kurindo.kurindo.util.DummyContent;
import id.co.kurindo.kurindo.util.LogUtil;
import id.co.kurindo.kurindo.wizard.shopadm.AddProductActivity;
import id.co.kurindo.kurindo.wizard.shopadm.AddShopActivity;

/**
 * Shows the quote detail page.
 *
 * Created by Andreas Schrade on 14.12.2015.
 */
public class ShopDetailAdmFragment extends BaseFragment {

    /**
     * The argument represents the dummy item ID of this fragment.
     */
    public static final String ARG_ITEM_ID = "item_id";
    private static final int REQUEST_ADD_PRODUCT = 1;

    /**
     * The dummy content of this fragment.
     */
    private DummyContent.DummyItem dummyItem;

    @Bind(R.id.quote)
    TextView quote;

    @Bind(R.id.author)
    TextView author;

    @Bind(R.id.backdrop)
    ImageView backdropImg;

    @Bind(R.id.tvOpenStatus)
    TextView tvOpenStatus;
    @Bind(R.id.ivOpenStatus)
    ImageView openStatusImg;

    @Bind(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbar;

    @Bind(R.id.appbar)
    AppBarLayout appBarLayout;

    private Shop shop;
    ProductGridAdmAdapter mAdapter;
    RecyclerView mRecyclerView;
    private ListenableAsyncTask loadProducttask;
    private ListenableAsyncTask loadNewsTask;
    static List<Product> products = new ArrayList<>();
    private EndlessRecyclerViewScrollListener loadMoreListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*if (getArguments().containsKey(ARG_ITEM_ID)) {
            // load dummy item by using the passed item ID.
            shop = DummyContent.SHOP_MAP.get(getArguments().getInt(ARG_ITEM_ID));
        }*/
        //Bundle bundle = getArguments().getBundle("data");
        //shop = bundle.getParcelable("shop");
        if(getArguments()==null){
            shop = DummyContent.shop;
        }
        if(shop == null){
            shop = ViewHelper.getInstance().getShop();
            //shop = getArguments().getParcelable("shop");

            //shop = (Shop) getArguments().getSerializable("shop");
            //products = DummyContent.PRODUCT_MAP.get(shop.getId());
            //shop.setProducts(products);
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflateAndBind(inflater, container, R.layout.fragment_shop_detail_adm);

/*        if (!((BaseDrawerActivity) getActivity()).providesActivityToolbar()) {
            // No Toolbar present. Set include_toolbar:
            ((BaseDrawerActivity) getActivity()).setToolbar((Toolbar) rootView.findViewById(R.id.toolbar));
        }
*/
        if (dummyItem != null) {
            loadBackdrop();
            collapsingToolbar.setTitle(dummyItem.title);
            author.setText(dummyItem.author);
            quote.setText(dummyItem.content);
        }

        if(shop != null){
            loadBackdropImage();
            collapsingToolbar.setTitle(" ");
            if(shop.getPic() != null){
                author.setText(shop.getPic().getAddress().toStringFormatted());
                quote.setText( shop.getPic().getPhone()==null? "": shop.getPic().getPhone()) ;
            }
            tvOpenStatus.setText(shop.getStatus());
            if(shop.getStatus() != null  && shop.getStatus().equalsIgnoreCase(AppConfig.OPEN)) {
                openStatusImg.setImageResource(R.drawable.open_icon);
                tvOpenStatus.setTextColor(Color.GREEN);
            }else{
                openStatusImg.setImageResource(R.drawable.closed_icon);
                tvOpenStatus.setTextColor(Color.RED);
            }
            appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                boolean isShow = false;
                int scrollRange = -1;
                @Override
                public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                    if (scrollRange == -1) {
                        scrollRange = appBarLayout.getTotalScrollRange();
                    }
                    if (scrollRange + verticalOffset == 0) {
                        collapsingToolbar.setTitle(shop.getName());
                        isShow = true;
                    } else if(isShow) {
                        collapsingToolbar.setTitle(" ");//carefull there should a space between double quote otherwise it wont work
                        isShow = false;
                    }
                }
            });

            products = shop.getProducts();
            if(products == null ) products = new ArrayList<>();
            logging();
        }
        setupList(rootView);
        return rootView;
    }

    private void logging() {
        HashMap<String, String > params = new HashMap<>();
        params.put("form-user", db.getUserPhone());
        params.put("form-type", "SHOP-ADM");
        params.put("form-tag", shop.getCode());
        params.put("form-activity", "View "+shop.getName());
        Address addr = ViewHelper.getInstance().getLastAddress();
        params.put("form-lat", (addr==null || addr.getLocation()==null? "0" : ""+addr.getLocation().latitude) );
        params.put("form-lng", (addr==null || addr.getLocation() == null ? "0" : ""+addr.getLocation().longitude));
        addRequest("req_logger", Request.Method.POST, AppConfig.URL_LOGGING, new Response.Listener() {
            @Override
            public void onResponse(Object o) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        }, params, getKurindoHeaders());
    }

    private void setupList(View rootView) {

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.list);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        mRecyclerView.setHasFixedSize(true);

        mAdapter = new ProductGridAdmAdapter(getActivity(), products, new ProductGridAdmAdapter.OnItemClickListener() {
            @Override
            public void onViewButtonClick(View view, int position) {
                Product p = products.get(position);
                Bundle bundle = new Bundle();
                ViewHelper.getInstance().setProduct(p);
                bundle.putBoolean("editMode", true);
                bundle.putBoolean("viewOnly", true);
                ((BaseActivity)getActivity()).showActivity(ProductActivity.class, bundle);
            }

            @Override
            public void onEditButtonClick(View view, int position) {
                Bundle bundle = new Bundle();
                bundle.putBoolean("editMode", true);
                ShopAdmHelper.getInstance().setProduct(products.get(position));
                ((BaseActivity)getActivity()).showActivity(AddProductActivity.class, bundle);
            }

            @Override
            public void onDeleteButtonClick(View view, int position) {
                Toast.makeText(getActivity(),"Delete Product action", Toast.LENGTH_SHORT).show();
            }
        });
        mRecyclerView.setAdapter(mAdapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        loadMoreListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                loadNextDataFromApi(page);
            }
        };
        mRecyclerView.addOnScrollListener(loadMoreListener);
        loadMoreListener.resetState();
/*
        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(),
                new RecyclerItemClickListener.OnItemClickListener() {

                    @Override
                    public void onItemClick(View view, int position) {
                        Product p = products.get(position);
                        //DummyContent.product = p;
                        Bundle bundle = new Bundle();
                        //bundle.putParcelable("product",p);
                        //bundle.putParcelable("shop",shop);
                        ViewHelper.getInstance().setProduct(p);
                        ((ShopActivity)getActivity()).showActivity(ProductActivity.class, bundle);
                    }
                }));
*/
        loadProducts();
    }

    private void loadNextDataFromApi(int page) {
        loadProducttask.execute(shop.getId(), page);
    }

    private void loadProducts() {
        loadProducttask = (ListenableAsyncTask) new LoadProductTask(getActivity());
        loadProducttask.listenWith(new ListenableAsyncTask.AsyncTaskListener() {
            @Override
            public void onPostExecute(Object o) {
                List<Product> dataList = (List<Product>)o;
                LogUtil.logI("loadProducttask","newsList size:"+dataList.size());
                products.clear();
                if(dataList != null && dataList.size() > 0){
                    for (int i = 0; i < dataList.size(); i++) {
                        Product p = dataList.get(i);
                        p.setShopid(shop.getId());
                        products.add(p);
                    }
                }/*else{

                    for (int i = 0; i < 6; i++) {
                        Product p = new Product(i, "Product "+i, new BigDecimal(1000*i), "Description "+i, null);
                        p.setDrawable(i == 0 ? R.drawable.daun1 : i % 2 == 0 ? R.drawable.daun2 : R.drawable.daun3);
                        products.add(p);
                    }
                //}*/
                mAdapter.notifyDataSetChanged();
            }
        });

        if(shop != null) loadProducttask.execute(shop.getId(), "1");
    }
    /*private void loadNews() {
        loadNewsTask = (ListenableAsyncTask) new LoadNewsTask(getActivity());
        loadNewsTask.listenWith(new ListenableAsyncTask.AsyncTaskListener() {
            @Override
            public void onPostExecute(Object o) {
                List<News> newsList = (List<News>)o;
                Log.i("loadNewsTask","newsList size:"+newsList.size());
                products.clear();
                if(newsList != null && newsList.size() > 0){
                    for (int i = 0; i < newsList.size(); i++) {
                        News n = newsList.get(i);

                        Product p = new Product(i, "Product "+i, new BigDecimal(1000*i), "Description "+i, n.getImg());
                        //p.setDrawable(R.drawable.cover_kurindo);
                        products.add(p);

                    }
                }else{
                    for (int i = 0; i < 6; i++) {
                        Product p = new Product(i, "Product "+i, new BigDecimal(1000*i), "Description "+i, null);
                        p.setDrawable(i == 0 ? R.drawable.daun1 : i % 2 == 0 ? R.drawable.daun2 : R.drawable.daun3);
                        products.add(p);
                    }
                }
                mAdapter.notifyDataSetChanged();
            }
        });
    }*/

    private void loadBackdrop() {
        Glide.with(this).load(dummyItem.photoId).centerCrop().into(backdropImg);
    }

    private void loadBackdropImage() {
        if(shop.getBackdrop() == null){
            Glide.with(this).load(R.drawable.placeholder).placeholder(R.drawable.placeholder).centerCrop().into(backdropImg);
        }
        else{
            int resId = Resources.getSystem().getIdentifier(shop.getBackdrop().substring(0, shop.getBackdrop().length()-4),"drawable", getActivity().getPackageName());
            if(resId == 0){
                Glide.with(this).load(AppConfig.urlShopImage(shop.getBackdrop())).placeholder(R.drawable.placeholder).thumbnail(.5f).diskCacheStrategy(DiskCacheStrategy.SOURCE).centerCrop().into(backdropImg);
            }else{
                backdropImg.setImageResource(resId);
                //Glide.with(this).load(resId).placeholder(R.drawable.placeholder).centerCrop().into(backdropImg);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }


    public static ShopDetailAdmFragment newInstance(Shop shop) {
        ShopDetailAdmFragment fragment = new ShopDetailAdmFragment();
        //products = DummyContent.PRODUCT_MAP.get(shop.getId());
        //shop.setProducts(products);
        //fragment.shop = shop;


        Bundle args = new Bundle();
        //args.putInt(ARG_ITEM_ID, shop.getId());
        //args.putSerializable("shop",shop);
        args.putParcelable("shop", shop);
        fragment.setArguments(args);
        return fragment;
    }

    public ShopDetailAdmFragment() {}

    @OnClick(R.id.btnAddProduct)
    public void btnAddProduct_onClick(){
        Intent intent = new Intent(getContext(), AddProductActivity.class);
        startActivityForResult(intent, REQUEST_ADD_PRODUCT);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ADD_PRODUCT) {
            if (resultCode == getActivity().RESULT_OK) {
                if(ShopAdmHelper.getInstance().getProduct() != null){
                    products.add(ShopAdmHelper.getInstance().getProduct());
                    mAdapter.notifyDataSetChanged();
                }
                ShopAdmHelper.getInstance().setProduct(null);
            }
        }
    }
}

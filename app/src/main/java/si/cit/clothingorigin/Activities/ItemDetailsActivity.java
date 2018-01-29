package si.cit.clothingorigin.Activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import si.cit.clothingorigin.CitApp;
import si.cit.clothingorigin.Interfaces.ContractResultListener;
import si.cit.clothingorigin.Interfaces.ObjectDataChangeListener;
import si.cit.clothingorigin.Interfaces.RecyclerAdapterItemClickListener;
import si.cit.clothingorigin.Objects.Product;
import si.cit.clothingorigin.ProductionChainAdapter;
import si.cit.clothingorigin.R;
import si.cit.clothingorigin.views.FontView;
import timber.log.Timber;

/**
 * Created by Anže Kožar on 22.1.2018.
 * nzkozar@gmail.com
 */

public class ItemDetailsActivity extends BaseActivity implements RecyclerAdapterItemClickListener {

    public static final int ACTIVITY_REQUEST_CODE_PRODUCT = 2;

    @BindView(R.id.product_picture)
    ImageView productPicture;

    @BindView(R.id.product_title)
    FontView productTitle;

    @BindView(R.id.product_price)
    FontView productPrice;

    @BindView(R.id.product_size)
    FontView productSize;

    @BindView(R.id.product_color)
    FontView productColor;

    @BindView(R.id.product_materials)
    FontView productMaterials;

    @BindView(R.id.product_soldBy)
    FontView productSeler;

    @BindView(R.id.production_chain_list)
    RecyclerView productionChainRecyclerView;

    @BindView(R.id.product_eco_score)
    FontView productEcoScore;

    @BindView(R.id.product_eco_reward)
    FontView productEcoReward;

    @BindView(R.id.product_score_recycle)
    ImageView productEmojiRecycle;

    @BindView(R.id.product_score_air)
    ImageView productEmojiAir;

    @BindView(R.id.product_score_water)
    ImageView productEmojiWater;

    ProductionChainAdapter productionChainAdapter = null;

    private Product product = Product.fakeProduct0();
    private boolean fromScan = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_details);

        ButterKnife.bind(this);

        final Long productId = getIntent().getLongExtra("product_id",0);
        fromScan = getIntent().getBooleanExtra("fromScan",true);

        if(productId>0){
            //get real product data from BC
            CitApp.getInstance().getBlockchainConnector().getProduct(productId, new ContractResultListener() {
                @Override
                public void onContractResult(Object resultObject, boolean success) {
                    product = (Product)resultObject;
                    Gson gson = new Gson();
                    Timber.i("Proudct id: "+productId+" "+gson.toJson(product));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            init();
                        }
                    });
                    product.fetchProductionChain(new ObjectDataChangeListener() {
                        @Override
                        public void onDataChange(Object object) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    initProductChainList();
                                }
                            });
                        }
                    });
                }
            });
        }else{
            init();
            initProductChainList();
        }
    }

    private void init(){
        Picasso picasso = new Picasso.Builder(getApplicationContext())
                .downloader(new OkHttp3Downloader(getApplicationContext()))
                .build();

        picasso.load(product.picture_url)
                .into(productPicture);

        productTitle.setText(product.title);
        productPrice.setText(product.price+" €");
        productSize.setText(product.size);
        productColor.setText(product.color);
        productMaterials.setText(product.materials);
        productSeler.setText(product.sold_by);
    }

    private void initProductChainList(){
        if(fromScan)updateRecentScanList();

        productSeler.setText(product.sold_by);

        productEcoScore.setText(String.valueOf(product.productionScore)+"/30");
        productEcoReward.setText("ECO reward: "+product.productionScore+" tokens");

        int score = product.productionScore;

        int high = 10;
        if(score<10)high=score;
        int recycleVal = random(0,high);
        score -= recycleVal;

        high = 10;
        if(score<10)high=score;
        int airVal = random(0,high);
        score -= recycleVal;

        high = 10;
        if(score<10)high=score;
        int waterVal = random(0,high);

        if(recycleVal<3){
            productEmojiRecycle.setImageResource(R.drawable.ic_emoji_bad);
        }else if(recycleVal>6){
            productEmojiRecycle.setImageResource(R.drawable.ic_emoji_neutral);
        }else {
            productEmojiRecycle.setImageResource(R.drawable.ic_emoji_good);
        }

        if(airVal<3){
            productEmojiAir.setImageResource(R.drawable.ic_emoji_bad);
        }else if(airVal>6){
            productEmojiAir.setImageResource(R.drawable.ic_emoji_neutral);
        }else {
            productEmojiAir.setImageResource(R.drawable.ic_emoji_good);
        }

        if(waterVal<3){
            productEmojiWater.setImageResource(R.drawable.ic_emoji_bad);
        }else if(waterVal>6){
            productEmojiWater.setImageResource(R.drawable.ic_emoji_neutral);
        }else {
            productEmojiWater.setImageResource(R.drawable.ic_emoji_good);
        }

        Timber.i("Initializing Product chain list");
        productionChainRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,
                false));
        if(productionChainAdapter==null){
            productionChainAdapter = new ProductionChainAdapter(this,product.production_chain,"chainAdapter");
            productionChainRecyclerView.setAdapter(productionChainAdapter);
            productionChainAdapter.setOnItemClickListener(this);
        }
    }

    private int random(int low, int high){
        if(high>low) {
            Random r = new Random();
            return r.nextInt(high - low) + low;
        }
        return 0;
    }

    private void updateRecentScanList(){
        if(product.id>0) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    SharedPreferences sp = getSharedPreferences(getString(R.string.preferences_value_store), MODE_PRIVATE);
                    String historyJson = sp.getString(getString(R.string.value_scan_history), "");
                    List<Product> history = new ArrayList<>();
                    Gson gson = new Gson();
                    if (historyJson.length() == 0) {
                        Timber.i("Clean scan history");
                        history.add(product);
                    } else {
                        history = gson.fromJson(historyJson, new TypeToken<List<Product>>(){}.getType());
                        if (history != null) {
                            Timber.i("Scan history length: " + history.size());
                            if(history.contains(product)){
                                history.remove(product);
                            }
                            history.add(product);
                        } else {
                            Timber.e("Failed to retrieve scan history!");
                        }
                    }

                    historyJson = gson.toJson(history,new TypeToken<List<Product>>(){}.getType());
                    sp.edit().putString(getString(R.string.value_scan_history), historyJson).apply();
                }
            }).start();
        }
    }

    @Override
    public void onListItemClick(String adapterTag, int position) {
        //TODO show producer details
    }
}

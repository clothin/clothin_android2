package si.cit.clothingorigin;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import si.cit.clothingorigin.Objects.Product;
import si.cit.clothingorigin.views.FontView;
import timber.log.Timber;

/**
 *
 */

public class RecentScansAdapter extends RecyclerViewAdapter<RecyclerView.ViewHolder>{

    //Context to use when retrieving resources such as drawables,...
    private Context mContext;
    //Dataset
    private List<Product> mDataset = new ArrayList<>();

    private Picasso picasso;

    //This tag is used to identify the adapter in interface callbacks
    // where multiple adapters are reporting to a single callback method
    public String adapterTAG;

    /** Provide a reference to the views for each data item
     *  Complex data items may need more than one view per item, and
     *  you provide access to all the views for a data item in a view holder
     */
    private static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        View mView;
        FontView title_text, soldBy_text, reward_text;
        ImageView photo_image;
        ViewHolder(View v) {
            super(v);
            mView = v;
            title_text = v.findViewById(R.id.product_title);
            soldBy_text = v.findViewById(R.id.product_soldBy);
            reward_text = v.findViewById(R.id.product_eco_reward);
            photo_image = v.findViewById(R.id.product_photo);
        }
    }

    /**
     * Construct a new adapter
     * @param context Context for resource retrieval
     * @param products Items to populate the list with
     * @param tag String TAG to identify the adapter in interface callbacks
     */
    public RecentScansAdapter(Context context, List<Product> products, String tag) {
        mDataset = products;
        mContext = context;
        adapterTAG = tag;

        picasso = new Picasso.Builder(mContext.getApplicationContext())
                .downloader(new OkHttp3Downloader(mContext.getApplicationContext()))
                .build();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_row_product, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if(holder instanceof ViewHolder) {
            final ViewHolder vh = (ViewHolder)holder;
            final Product product = mDataset.get(position);

            //Gson gson = new Gson();
            //Timber.i(gson.toJson(product));

            vh.title_text.setText(product.title);
            vh.soldBy_text.setText(product.sold_by);
            vh.reward_text.setText("Reward: "+product.productionScore+" CIT");

            picasso.load(product.picture_url)
                    .into(vh.photo_image);

            if(this.onItemClickListener !=null){
                vh.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onItemClickListener.onListItemClick(adapterTAG,position);
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
            return mDataset.size();
    }
}
package com.app.worki.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.app.worki.R;
import com.app.worki.model.FeedbackModel;
import com.app.worki.util.IntentUtil;
import com.app.worki.util.Utils;
import java.util.ArrayList;
import butterknife.BindView;
import butterknife.ButterKnife;

public class FeedbackAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<FeedbackModel> mList;
    private LayoutInflater mLayoutInflater = null;

    public FeedbackAdapter(Context context, ArrayList<FeedbackModel> list) {
        mContext = context;
        mList = list;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int pos) {
        return mList.get(pos);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        CompleteListViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater li = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = li.inflate(R.layout.feedback_item, null);
            viewHolder = new CompleteListViewHolder(v);
            v.setTag(viewHolder);
        } else {
            viewHolder = (CompleteListViewHolder) v.getTag();
        }

        FeedbackModel model = mList.get(position);

        // set views data here
        viewHolder.title.setText(model.getTitle());
        if(model.getMessage().length() < 100){
            viewHolder.message.setText(model.getMessage());
        }
        else{
            viewHolder.message.setText(model.getMessage().substring(0, 100)+"...");
        }

        if(model.getEmail().isEmpty()){
            viewHolder.email.setVisibility(View.GONE);
        }
        else{
            viewHolder.email.setVisibility(View.VISIBLE);
            viewHolder.email.setText(model.getEmail());
            viewHolder.email.setOnClickListener(v1 -> {
                Utils.clickEffect(v1);
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", model.getEmail(), null));
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{model.getEmail()});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "");
                mContext.startActivity(Intent.createChooser(emailIntent, "Send email"));
            });
        }
        viewHolder.time.setText(Utils.getTimeAgo((System.currentTimeMillis() - Long.parseLong(model.getTime()))/1000));

        return v;
    }

    static class CompleteListViewHolder {
        // declare views here
        @BindView(R.id.title)
        TextView title;
        @BindView(R.id.time)
        TextView time;
        @BindView(R.id.message)
        TextView message;
        @BindView(R.id.email)
        TextView email;
        public CompleteListViewHolder(View base) {
            //initialize views here
            ButterKnife.bind(this, base);
        }
    }
}

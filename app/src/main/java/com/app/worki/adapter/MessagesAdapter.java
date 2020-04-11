package com.app.worki.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.app.worki.R;
import com.app.worki.model.MessageModel;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MessagesAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<MessageModel> mList;
    private LayoutInflater mLayoutInflater = null;

    public MessagesAdapter(Context context, ArrayList<MessageModel> list) {
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
            v = li.inflate(R.layout.message_item, null);
            viewHolder = new CompleteListViewHolder(v);
            v.setTag(viewHolder);
        } else {
            viewHolder = (CompleteListViewHolder) v.getTag();
        }

        MessageModel model = mList.get(position);

        // set views data here

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
        @BindView(R.id.url)
        TextView url;
        public CompleteListViewHolder(View base) {
            //initialize views here
            ButterKnife.bind(this, base);
        }
    }
}

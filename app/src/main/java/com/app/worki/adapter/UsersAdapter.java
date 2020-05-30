package com.app.worki.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.app.worki.R;
import com.app.worki.model.UserModel;
import com.app.worki.util.FirebaseStorageUtil;
import java.util.ArrayList;
import java.util.Date;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class UsersAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<UserModel> mList;
    private LayoutInflater mLayoutInflater = null;

    public UsersAdapter(Context context, ArrayList<UserModel> list) {
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
            v = li.inflate(R.layout.user_item, null);
            viewHolder = new CompleteListViewHolder(v);
            v.setTag(viewHolder);
        } else {
            viewHolder = (CompleteListViewHolder) v.getTag();
        }

        UserModel model = mList.get(position);

        // set views data here
        viewHolder.name.setText(model.getUsername());

        if(model.getStatus_time()!=null){
            if(!model.getStatus_time().isEmpty()){
                long time = Long.parseLong(model.getStatus_time());
                Date date = new Date();
                date.setTime(time);
                long offset = System.currentTimeMillis() - date.getTime();
                if(offset < 0)
                    offset = 0;
                if(offset/(1000*60*60) > 2){
                    showInActive(viewHolder);
                }
                else{
                    if(model.getStatus() == 1){
                        showActive(viewHolder);
                    }
                    else{
                        showInActive(viewHolder);
                    }
                }
            }
            else{
                showInActive(viewHolder);
            }
        }
        else{
            showInActive(viewHolder);
        }

        loadPhoto(viewHolder, model);

//        if(!model.getPhoto().isEmpty()) {
//            LogUtil.loge("photo: "+position);
//            viewHolder.photo.setImageBitmap(null);
//            loadPhoto(viewHolder, model);
//        }
//        else{
//            LogUtil.loge("no photo: "+position);
//            viewHolder.photo.setImageBitmap(null);
//            viewHolder.photo.setImageResource(R.drawable.profile);
//            //viewHolder.photo.setImageDrawable(mContext.getResources().getDrawable(R.drawable.profile));
//        }

        return v;
    }

    private void showInActive(CompleteListViewHolder viewHolder) {
        viewHolder.status.setText(mContext.getResources().getString(R.string.inactive));
        viewHolder.status.setTextColor(mContext.getResources().getColor(R.color.red));
    }

    private void showActive(CompleteListViewHolder viewHolder) {
        viewHolder.status.setText(mContext.getResources().getString(R.string.active));
        viewHolder.status.setTextColor(mContext.getResources().getColor(R.color.green));
    }

    private void loadPhoto(CompleteListViewHolder viewHolder, UserModel model) {
        if(!model.getPhoto().equals("photo.png")){
            FirebaseStorageUtil.showImage(
                    (Activity) mContext,
                    viewHolder.photo,
                    FirebaseStorageUtil.getStorageReference(new String[]{model.getUsername(), model.getPhoto()})
            );
        }
        else{
            FirebaseStorageUtil.showImage(
                    (Activity) mContext,
                    viewHolder.photo,
                    FirebaseStorageUtil.getStorageReference(new String[]{model.getPhoto()})
            );
        }
    }

    static class CompleteListViewHolder {
        // declare views here
        @BindView(R.id.photo)
        CircleImageView photo;
        @BindView(R.id.name)
        TextView name;
        @BindView(R.id.status)
        TextView status;

        public CompleteListViewHolder(View base) {
            //initialize views here
            ButterKnife.bind(this, base);
        }
    }

}

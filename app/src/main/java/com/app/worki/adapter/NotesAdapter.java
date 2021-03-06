package com.app.worki.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.app.worki.R;
import com.app.worki.UserProfile;
import com.app.worki.model.NoteModel;
import com.app.worki.util.FirestoreUtil;
import com.app.worki.util.LogUtil;
import com.app.worki.util.PopupUtil;
import com.bumptech.glide.load.resource.file.FileResource;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NotesAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<NoteModel> mList;
    private LayoutInflater mLayoutInflater = null;

    public NotesAdapter(Context context, ArrayList<NoteModel> list) {
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
            v = li.inflate(R.layout.note_item, null);
            viewHolder = new CompleteListViewHolder(v);
            v.setTag(viewHolder);
        } else {
            viewHolder = (CompleteListViewHolder) v.getTag();
        }

        NoteModel model = mList.get(position);
        viewHolder.note.setText(model.getNote());
        Date date = new Date();
        date.setTime(Long.parseLong(model.getTime()));
        viewHolder.date.setText(new SimpleDateFormat("HH:mm dd MMM").format(date));

        // set views data here
        viewHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupUtil.showAlertPopup((Activity) mContext, "Delete Note", "Are you sure you want to delete this note?", new String[]{"OK","Cancel"}, new PopupUtil.AlertPopup() {
                    @Override
                    public void positive(DialogInterface dialog) {
                        dialog.dismiss();
                        FirestoreUtil.deleteDocument(FirestoreUtil.notes, model.getId(), new FirestoreUtil.DeleteResult() {
                            @Override
                            public void success() {
                                LogUtil.loge("deleted");
                                ((UserProfile) mContext).loadNotes();
                            }
                            @Override
                            public void fail(String error) {
                            }
                        });
                    }
                    @Override
                    public void negative(DialogInterface dialog) {
                        dialog.dismiss();
                    }
                });
            }
        });

        return v;
    }

    static class CompleteListViewHolder {
        // declare views here
        @BindView(R.id.date)
        TextView date;
        @BindView(R.id.delete)
        TextView delete;
        @BindView(R.id.note)
        TextView note;
        public CompleteListViewHolder(View base) {
            //initialize views here
            ButterKnife.bind(this, base);
        }
    }
}

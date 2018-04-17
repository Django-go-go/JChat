package com.jkingone.jchat.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.promeg.pinyinhelper.Pinyin;
import com.jkingone.jchat.R;
import com.jkingone.jchat.bean.Selection;
import com.jkingone.jchat.bean.User;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by Administrator on 2017/9/6.
 */

public class FriendAdapter extends RecyclerView.Adapter {

    private static final String TAG = "FriendAdapter";

    public static final int TYPE_HEAD = 1;
    public static final int TYPE_FOOT = 2;
    public static final int TYPE_DIVIDER = 3;
    public static final int TYPE_DEFAULT = 4;

    private LinearLayout footer;
    private LinearLayout header;

    private Context mContext;
    private LayoutInflater mInflater;

    private List<Selection> selections;

    private OnClickListener mListener;

    public FriendAdapter(Context context, List<Selection> data) {
        mContext = context;
        this.selections = data;
        mInflater = LayoutInflater.from(mContext);

        if (header == null) {
            header = new LinearLayout(mContext);
            header.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,
                    RecyclerView.LayoutParams.WRAP_CONTENT));
            header.setVisibility(View.INVISIBLE);
        }

        if (footer == null) {
            footer = new LinearLayout(mContext);
            footer.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,
                    RecyclerView.LayoutParams.WRAP_CONTENT));
            footer.setVisibility(View.INVISIBLE);
        }

        Log.i(TAG, "FriendAdapter: ");

    }



    public void addHeader(View view) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT
                , LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER;
        view.setLayoutParams(lp);
        header.addView(view);
        header.setVisibility(View.VISIBLE);
    }

    public void addFooter(View view) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT
                , LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER;
        view.setLayoutParams(lp);
        footer.addView(view);
        footer.setVisibility(View.VISIBLE);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = null;
        switch (viewType){
            case TYPE_DEFAULT:
                holder = new MyViewHolder(mInflater.inflate(R.layout.item_friend_recycle,
                        parent, false));
                break;
            case TYPE_HEAD:
                holder = new HeadViewHolder(header);
                break;
            case TYPE_FOOT:
                holder = new FootViewHolder(footer);
                break;
            case TYPE_DIVIDER:
                holder = new DividerViewHolder(mInflater.inflate(R.layout.item_recycle_divider,
                        parent, false));
                break;
            default:
                holder = null;
                break;
        }
        Log.i(TAG, "onCreateViewHolder: ");
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof MyViewHolder) {
                MyViewHolder hold = (MyViewHolder) holder;
                if (position == 1) {
                    hold.mTextView.setText("新朋友");
                    hold.mImageView.setBackgroundResource(R.drawable.fragment_newfriend);

                } else {
                    int pos = position - 2;
                    Selection selection = selections.get(pos);
                    Log.i(TAG, "MyViewHolder:   " + position);
                    hold.mImageView.setBackgroundResource(R.drawable.default_useravatar);
                    hold.mTextView.setText(selection.getUser().getUsername());
                }
                hold.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mListener != null) {
                            mListener.onClick(position - 2);
                        }
                    }
                });
            }
            if (holder instanceof DividerViewHolder) {
                DividerViewHolder hold = (DividerViewHolder) holder;
                Log.i(TAG, "DividerViewHolder:   " + position);
                hold.mTextView.setText(selections.get(position - 2).getCharacter());
            }

    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0){
            return TYPE_HEAD;
        }

        if (position == selections.size() + 1 + 1){
            return TYPE_FOOT;
        }

        if (position == 1) {
            return TYPE_DEFAULT;
        }

        if (selections.size() > 0) {
            if (selections.get(position - 2).isLabel()) {
                return TYPE_DIVIDER;
            } else {
                return TYPE_DEFAULT;
            }
        } else {
            return TYPE_DEFAULT;
        }
    }

    @Override
    public int getItemCount() {
        return selections.size() + 3;
    }


    static class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_friendname)
        TextView mTextView;
        @BindView(R.id.im_friendpic)
        ImageView mImageView;

        MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    private static class FootViewHolder extends RecyclerView.ViewHolder {
        FootViewHolder(View itemView) {
            super(itemView);
        }
    }

    private static class HeadViewHolder extends RecyclerView.ViewHolder {
        HeadViewHolder(View itemView) {
            super(itemView);
        }
    }

    static class DividerViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_divider)
        TextView mTextView;

        DividerViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public void setListener(OnClickListener listener) {
        mListener = listener;
    }

    public interface OnClickListener {
        void onClick(int pos);
    }

}
